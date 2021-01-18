package com.cyberlogitec.freight9.ui.trademarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Bor
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.getWeek
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchViewModel
import com.google.android.material.appbar.AppBarLayout
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_market_popup_prev.*
import kotlinx.android.synthetic.main.bottom_sheet_trademarket_offerlist_prev.*
import kotlinx.android.synthetic.main.item_market_offer_price.view.*
import kotlinx.android.synthetic.main.item_market_offer_price_footer.view.*
import kotlinx.android.synthetic.main.item_market_offer_split.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class MarketOfferPreviewFragment constructor(val viewModel: BaseViewModel): RxFragment(), AppBarLayout.OnOffsetChangedListener {

    private var requestWeek: String = ""
    private lateinit var requestItem: Bor
    private val adapter by lazy {

        RecyclerAdapter()
                .apply {

                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.bottom_sheet_trademarket_offerlist_prev, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        when(viewModel) {
            is MarketViewModel -> {
                viewModel.outPuts.onSuccessRequestOfferDetail()
                        .bindToLifecycle(this)
                        .subscribe {
                            Timber.d("f9: view onSuccessRequestBuyOfferInfos \n $it")
                            updateUi(it)
                            requestItem = it
                        }
                viewModel.outPuts.refreshSplitPopupDeatil()
                        .bindToLifecycle(this)
                        .subscribe {
                            Timber.d("f9: viewSplitPopupDetail to view ")
                            changeUi(false)
                            updateUi(it)
                            requestItem = it
                        }
            }
            is MarketWatchViewModel -> {
                viewModel.outPuts.onSuccessRequestOfferDetail()
                        .bindToLifecycle(this)
                        .subscribe {
                            Timber.d("f9: view onSuccessRequestBuyOfferInfos \n $it")
                            updateUi(it)
                            requestItem = it
                        }
                viewModel.outPuts.refreshSplitPopupDeatil()
                        .bindToLifecycle(this)
                        .subscribe {
                            Timber.d("f9: viewSplitPopupDetail to view ")
                            changeUi(false)
                        }
            }
        }

        ib_more.visibility = View.GONE
        tv_twk_value.visibility = View.GONE
        tv_twk.visibility = View.GONE
        tv_weeks.visibility = View.GONE
        ll_volume.visibility = View.GONE
        recyclerViewInit()
        btn_goto_order.setSafeOnClickListener {
            when(viewModel) {
                is MarketViewModel -> {
                    if (requestItem.item.offerTypeCode.equals("S"))
                        viewModel.inPuts.clickSellOfferItem((requestItem.item))
                    else
                        viewModel.inPuts.clickBuyOfferItem(requestItem.item)
                }
                is MarketWatchViewModel -> {
                    if (requestItem.item.offerTypeCode.equals("S"))
                        viewModel.inPuts.clickSellOfferItem((requestItem.item))
                    else
                        viewModel.inPuts.clickBuyOfferItem(requestItem.item)
                }
            }
        }

    }

    fun setRequestWeek(week: String) {
        requestWeek = week
    }
    private fun changeUi(isShow: Boolean) {
        if(isShow.not()) {
            //view disable
            adapter.itemList.clear()
            adapter.notifyDataSetChanged()
            cl_header.visibility = View.INVISIBLE
            appbar_market_popup_prev.visibility = View.INVISIBLE
            recycler_view_offer_week.visibility = View.INVISIBLE
            ns_contents.fullScroll(View.FOCUS_UP)

            appbar_market_popup_prev.setExpanded(true)


        }else {
            //view visible
            cl_header.visibility = View.VISIBLE
            appbar_market_popup_prev.visibility = View.VISIBLE
            recycler_view_offer_week.scrollToPosition(0)
            recycler_view_offer_week.visibility = View.VISIBLE

        }

    }
    @SuppressLint("SetTextI18n")
    private fun updateUi(data: Bor) {

        if(data.item.cryrCd.isNullOrBlank())
            data.item.cryrCd = ""
        carrier_logo.setImageResource(data.item.cryrCd!!.getCarrierIcon())
        if(data.item.cryrCd.isNullOrBlank())
            tv_carrier_name.text = getString(R.string.all_carriers)
        else
            tv_carrier_name.text = data.item.cryrCd
        if(data.item.carrierCount!!.toInt() > 0) {
            tv_carrier_cnt.visibility = View.VISIBLE
            tv_carrier_cnt.text = "+${data.item.carrierCount}"
        }else{
            tv_carrier_cnt.visibility = View.INVISIBLE
        }
        tv_pol_name.text = data.item.locPolCd
        if(data.item.locPolCnt!!.toInt() > 0) {
            tv_pol_cnt.visibility = View.VISIBLE
            tv_pol_cnt.text = "+${data.item.locPolCnt}"
        }else {
            tv_pol_cnt.visibility = View.INVISIBLE
        }
        tv_pol_detail.text = data.item.locPolNm

        tv_pod_name.text = data.item.locPodCd
        if(data.item.locPodCnt!!.toInt() > 0){
            tv_pod_cnt.visibility = View.VISIBLE
            tv_pod_cnt.text = "+${data.item.locPodCnt}"
        }else{
            tv_pod_cnt.visibility = View.INVISIBLE
        }
        tv_pod_detail.text = data.item.locPodNm

        val ret = ArrayList<RecyclerAdapter.Item>()

        // content values
        for(contentValueList in data.getWeekPriceVolumeList()){

            val content = RecyclerAdapter.Item.Builder()
                    .type(RecyclerAdapter.CONTENT)
                    .content(contentValueList)
                    .build()
            ret.add(content)
        }

        val content = RecyclerAdapter.Item.Builder()
                .type(RecyclerAdapter.FOOTER)
                .build()
        ret.add(content)

        adapter.itemList.clear()
        adapter.notifyDataSetChanged()
        adapter.itemList.addAll(ret)
        adapter.setCurrentWeek(requestWeek)
        adapter.setOfferType(data.item.offerTypeCode!!)
        adapter.notifyDataSetChanged()

        if(data.item.offerTypeCode.equals("S"))
            btn_goto_order.text = getString(R.string.market_want_buy)
        else
            btn_goto_order.text = getString(R.string.market_want_sell)
        val share = SharedPreferenceManager(context)
        if(data.item.ownerCompanyCode!! == share.name) {
            btn_goto_order.isEnabled = false
            btn_goto_order.text = getString(R.string.market_btn_this_is_your_offer)
        } else {
            btn_goto_order.isEnabled = true
        }
        changeUi(true)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("f9: onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")

    }

    private fun recyclerViewInit() {
        recycler_view_offer_week.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketOfferPreviewFragment.adapter
        }
        appbar_market_popup_prev.addOnOffsetChangedListener(this)
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var itemList: MutableList<Item> = ArrayList()
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var currentWeek: String = ""
        private lateinit var offerType: String

        companion object {
            //const val HEADER = 0
            const val CONTENT = 1
            const val FOOTER = 2
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            var holder: RecyclerView.ViewHolder? = null

            when(viewType) {
                CONTENT -> holder =
                        ContentViewHolder(
                                inflater.inflate(R.layout.item_market_offer_price, parent, false)
                        )
                FOOTER -> holder =
                        FooterViewHolder(
                                inflater.inflate(R.layout.item_market_offer_price_footer, parent, false)
                        )
            }
            return holder ?: throw IllegalStateException("Item type unspecified.")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            currencyFormat.minimumFractionDigits = 0
            if(holder.adapterPosition != RecyclerView.NO_POSITION) {
                val item = itemList[holder.adapterPosition]
                when(item.type) {
                    CONTENT -> {
                        bindContent(holder as ContentViewHolder, item)
                    }
                    FOOTER -> {
                        bindFooter(holder as FooterViewHolder)
                    }
                }
            }
        }

        override fun getItemCount(): Int = itemList.size

        override fun getItemViewType(position: Int): Int {
            return itemList[position].type
        }

        @SuppressLint("SetTextI18n")
        private fun bindContent(holder: ContentViewHolder, item: Item) {
            with(holder.itemView) {
                if(currentWeek == item.content?.period){
                    cl_market_offer_price_item.background= getDrawable(context, R.color.color_333333)
                }else {
                    cl_market_offer_price_item.background = getDrawable(context, R.color.color_0d0d0d)
                }
                tv_week.text = context.getWeek(item.content?.period)
                tv_price.text = currencyFormat.format(item.content?.price?.toInt())
                tv_volume.text = "${item.content?.volume.toString()}T"
                val value = item.content?.volume?.times(item.content.price)
                tv_value.text = currencyFormat.format(value?.toInt())

            }
        }
        private fun bindFooter(holder: FooterViewHolder) {
            with(holder.itemView) {
                var total = 0f
                for(item in itemList) {
                    if(item.type == CONTENT){
                        total += (item.content?.volume?.times(item.content.price)!!)
                    }
                }
                tv_total_value.text = currencyFormat.format(total.toInt())
                if(offerType == "S")
                    btn_make_new_offer.text = context.getString(R.string.buy_order_select_new_sell_offer)
                else
                    btn_make_new_offer.text = context.getString(R.string.buy_order_select_new_buy_offer)
            }
        }

        class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        class Item(internal val type: Int,
                   internal val content: Bor.OfferWeekVolume?) {

            data class Builder(
                    private var type: Int = 0,
                    private var content: Bor.OfferWeekVolume? = null) {

                fun type(type: Int) = apply { this.type = type }
                fun content(content: Bor.OfferWeekVolume?) = apply { this.content = content }
                fun build() = Item(
                        type,
                        content
                )
            }
        }

        fun setOfferType(type: String) {
            offerType = type
        }

        fun setCurrentWeek(week: String){
            currentWeek = week
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketOfferPreviewFragment {
            return MarketOfferPreviewFragment(viewModel)
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if(abs(verticalOffset) == appBarLayout?.height) cl_header.background = context?.let { getDrawable(it, R.color.color_0d0d0d) }
        else cl_header.background = context?.let { getDrawable(it, R.drawable.bg_round_corner_8_0d0d0d_top) }
    }
}