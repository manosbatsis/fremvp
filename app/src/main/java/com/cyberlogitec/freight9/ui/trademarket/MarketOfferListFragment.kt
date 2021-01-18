package com.cyberlogitec.freight9.ui.trademarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.BorList
import com.cyberlogitec.freight9.lib.ui.dialog.NormalOneBtnDialog
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeableRecyclerViewTouchListener
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchViewModel
import com.github.mikephil.charting.utils.Utils
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.bottom_sheet_trademarket_offerlist.*
import kotlinx.android.synthetic.main.item_market_offer_split.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class MarketOfferListFragment constructor(val viewModel: BaseViewModel): RxFragment(), SwipeableRecyclerViewTouchListener.SwipeListener {

    private var offerType:String = ConstantTradeOffer.OFFER_TYPE_CODE_BUY

    override fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        when(viewModel) {
            is MarketViewModel -> {
                viewModel.inPuts.requestOfferDetail(adapter.datas[reverseSortedPositions[0]])
                viewModel.inPuts.swipeToOfferDetail(recyclerView.id.toLong())
            }
            is MarketWatchViewModel -> {
                viewModel.inPuts.requestOfferDetail(adapter.datas[reverseSortedPositions[0]])
                viewModel.inPuts.swipeToOfferDetail(recyclerView.id.toLong())
            }
        }
    }

    override fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        when(viewModel) {
            is MarketViewModel ->{
                viewModel.inPuts.requestOfferDetail(adapter.datas[reverseSortedPositions[0]])
                viewModel.inPuts.swipeToOfferRoute(recyclerView.id.toLong())
            }
            is MarketWatchViewModel -> {
                viewModel.inPuts.requestOfferDetail(adapter.datas[reverseSortedPositions[0]])
                viewModel.inPuts.swipeToOfferRoute(recyclerView.id.toLong())
            }
        }
    }

    override fun canSwipeLeft(position: Int): Boolean {
        return true
    }

    override fun canSwipeRight(position: Int): Boolean {
        return true
    }

    fun setOfferUiType(type: String) {
        offerType = type
    }

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    if(viewModel is MarketViewModel) {
                        onClickBuyOffer = {
                            if(checkMyOffer(it))
                                showOwnDialog()
                            else
                                viewModel.inPuts.clickBuyOfferItem(it)
                        }
                        onClickSellOffer = {
                            if(checkMyOffer(it))
                                showOwnDialog()
                            else
                                viewModel.inPuts.clickSellOfferItem(it)
                        }
                    }
                }
    }

    private fun checkMyOffer(it: BorList): Boolean {
        val share = SharedPreferenceManager(context)
        if(it.ownerCompanyCode.equals(share.name))
            return true

        return false
    }
    private fun showOwnDialog() {
        val dialog = NormalOneBtnDialog(getString(R.string.market_popup_order_unavailable), getString(R.string.market_popup_order_unavailable_desc),
                getString(R.string.ok))
        dialog.isCancelable = false
        dialog.setOnClickListener(View.OnClickListener {
            it?.let {
                dialog.dismiss()
            }
        })
        dialog.show(this.parentFragmentManager, dialog.className)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.bottom_sheet_trademarket_offerlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        cb_trade_offerlist_viewperiod.setOnCheckedChangeListener { cb_trade_offerlist_viewperiod, isChecked ->
            context!!.showToast(resources.getString(R.string.working_in_progress))
            /*adapter.setViewPeriodMode(isChecked)
            adapter.notifyDataSetChanged()
            if(isChecked) {
                tv_view_period.setTextColor(resources.getColor(R.color.very_light_pink))
            }else {
                tv_view_period.setTextColor(resources.getColor(R.color.greyish_brown))
            }*/

        }

        if(viewModel is MarketViewModel) {

            viewModel.outPuts.onSuccessRequestBuyOfferLists()
                    .bindToLifecycle(this)
                    .subscribe {
                        Timber.d("f9: view onSuccessRequestBuyOfferInfos")
                        refreshList(it)
                    }
            viewModel.outPuts.onSuccessRequestSellOfferLists()
                    .bindToLifecycle(this)
                    .subscribe {
                        Timber.d("f9: view onSuccessRequestSellOfferInfos")
                        offerType = ConstantTradeOffer.OFFER_TYPE_CODE_SELL
                        refreshList(it)
                    }

            viewModel.outPuts.onSuccessRequestOfferDetail()
                    .bindToLifecycle(this)
                    .subscribe {
                        Timber.d("f9: view onSuccessRequestBuyOfferInfos \n $it")
                    }
            viewModel.outPuts.onRefrshOfferLists()
                    .bindToLifecycle(this)
                    .subscribe {
                        refreshList(ArrayList())
                    }
        }
        recyclerViewInit()


    }
    private fun refreshList(list: List<BorList>) {
        adapter.datas.clear()
        adapter.datas.addAll(list)
        adapter.notifyDataSetChanged()
        adapter.setOfferType(offerType)
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

        recycler_view_market_offer_list.apply {
            layoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@MarketOfferListFragment.adapter
        }
        val touchListener = SwipeableRecyclerViewTouchListener(recycler_view_market_offer_list, this@MarketOfferListFragment)
        recycler_view_market_offer_list.addOnItemTouchListener(touchListener)

        if(viewModel is MarketWatchViewModel) {
            adapter.datas.add(BorList(cryrCd = "ONE"))
        }
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<BorList>()
        var viewPeroidMode : Boolean = false
        private var offerType :String = ConstantTradeOffer.OFFER_TYPE_CODE_SELL

        var onClickBuyOffer: (BorList) -> Unit = {}
        var onClickSellOffer: (BorList) -> Unit = {}

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        init {
            currencyFormat.minimumFractionDigits = 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_market_offer_split, parent, false))

        override fun getItemCount(): Int = datas.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]

                carrier_logo.setImageResource(data.cryrCd!!.getCarrierIcon())
                if(data.cryrCd.isNullOrBlank())
                    tv_carrier_name.text = context.getString(R.string.all_carriers)
                else
                    tv_carrier_name.text = data.cryrCd

                if(data.carrierCount!! > 0)
                    tv_carrier_cnt.text = "+${data.carrierCount}"
                else
                    tv_carrier_cnt.text = ""
                tv_pol_name.text = data.locPolCd
                tv_pol_cnt.text = if(data.locPolCnt!! > 0) "+${data.locPolCnt!!}" else ""
                tv_pol_detail.text = data.locPolNm

                tv_pod_name.text = data.locPodCd
                tv_pod_cnt.text = if(data.locPodCnt!! > 0) "+${data.locPodCnt!!}" else ""
                tv_pod_detail.text = data.locPodNm

                if (data.wholeYn == ConstantTradeOffer.ALL_YN_PARTIAL)
                    ll_whole.visibility = View.GONE
                else
                    ll_whole.visibility = View.VISIBLE

                if(!viewPeroidMode){
                    ll_volume.visibility = View.VISIBLE
                    tv_weeks.visibility = View.GONE

                    tv_twk_value.text = currencyFormat.format(data.price)
                    tv_volume.text = "${data.remainderQty}"
                }else {
                    ll_volume.visibility = View.GONE
                    tv_weeks.visibility = View.VISIBLE
                    currencyFormat.roundingMode
//                    tv_twk_value.text = "${currencyFormat.format(data.maxPrice)}-${String.format("%,d",data.minPrice)}"
//                    tv_weeks.text = "${context.getWeek(data.fromWeek)}-${context.getWeek(data.toWeek)}"
                }
                setOnClickListener {

                    if(data.offerTypeCode.equals(ConstantTradeOffer.OFFER_TYPE_CODE_BUY))
                        onClickBuyOffer(data)
                    else
                        onClickSellOffer(data)
                }
            }
            if (position == datas.lastIndex){
                val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                params.bottomMargin = Utils.convertDpToPixel(24f).toInt()
                holder.itemView.layoutParams = params
            }else{
                val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
                params.bottomMargin = 0
                holder.itemView.layoutParams = params
            }
        }

        fun setViewPeriodMode(periodMode:Boolean) {
            viewPeroidMode = periodMode
        }

        fun setOfferType(type:String) {
            offerType = type
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketOfferListFragment {
            return MarketOfferListFragment(viewModel)
        }
    }
}