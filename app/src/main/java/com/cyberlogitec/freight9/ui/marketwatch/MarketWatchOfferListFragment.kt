package com.cyberlogitec.freight9.ui.marketwatch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.BorList
import com.cyberlogitec.freight9.lib.ui.dialog.NormalOneBtnDialog
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeableRecyclerViewTouchListener
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.getWeek
import com.github.mikephil.charting.utils.Utils
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.bottom_sheet_trademarket_offerlist.*
import kotlinx.android.synthetic.main.item_market_offer_split.view.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class MarketWatchOfferListFragment constructor(val viewModel: MarketWatchViewModel): RxFragment(), SwipeableRecyclerViewTouchListener.SwipeListener {

    private lateinit var responseData: List<BorList>
    private var offerType:String = ConstantTradeOffer.OFFER_TYPE_CODE_BUY

    override fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        viewModel.inPuts.requestOfferDetail(adapter.datas[reverseSortedPositions[0]])
        viewModel.inPuts.swipeToOfferDetail(recyclerView.id.toLong())
    }

    override fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        viewModel.inPuts.requestOfferDetail(adapter.datas[reverseSortedPositions[0]])
        viewModel.inPuts.swipeToOfferRoute(recyclerView.id.toLong())
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
                    onClickOffer = {
                        if(checkMyOffer(it))
                            showOwnDialog()
                        else{

                            when(offerType) {
                                ConstantTradeOffer.OFFER_TYPE_CODE_BUY -> { viewModel.inPuts.clickBuyOfferItem(it) }
                                ConstantTradeOffer.OFFER_TYPE_CODE_SELL -> { viewModel.inPuts.clickSellOfferItem(it) }
                            }

                        }
                    }
                }
    }

    private fun checkMyOffer(it: BorList): Boolean {
        val share = SharedPreferenceManager(context)
        if(it.ownerCompanyCode == share.name)
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
            View = inflater.inflate(R.layout.bottom_sheet_marketwatch_offerlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        cb_trade_offerlist_viewperiod.setOnCheckedChangeListener { cb_trade_offerlist_viewperiod, isChecked ->
            //context!!.showToast(resources.getString(R.string.working_in_progress))
            adapter.setViewPeriodMode(isChecked)
            adapter.notifyDataSetChanged()
            if(isChecked) {
                tv_view_period.setTextColor(resources.getColor(R.color.very_light_pink, null))
            }else {
                tv_view_period.setTextColor(resources.getColor(R.color.greyish_brown, null))
            }

        }


        when(offerType) {
            ConstantTradeOffer.OFFER_TYPE_CODE_SELL -> {

                viewModel.outPuts.onSuccessRequestWeekAsk()
                        .bindToLifecycle(this)
                        .subscribe {
                            responseData = it
                            updateList(it)
                        }
            }
            ConstantTradeOffer.OFFER_TYPE_CODE_BUY -> {
                viewModel.outPuts.onSuccessRequestWeekBid()
                        .bindToLifecycle(this)
                        .subscribe {
                            responseData = it
                            updateList(it)
                        }
            }
        }
        recyclerViewInit()


    }
    private fun updateList(it: List<BorList>) {
        adapter.datas.clear()
        adapter.datas.addAll(it)
        adapter.notifyDataSetChanged()
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
            adapter = this@MarketWatchOfferListFragment.adapter
        }
        val touchListener = SwipeableRecyclerViewTouchListener(recycler_view_market_offer_list, this@MarketWatchOfferListFragment)
        recycler_view_market_offer_list.addOnItemTouchListener(touchListener)

    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<BorList>()
        var viewPeroidMode : Boolean = false

        var onClickOffer: (BorList) -> Unit = {}

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        init {
            currencyFormat.minimumFractionDigits = 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            currencyFormat.minimumFractionDigits = 0
            currencyFormat.maximumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_market_offer_split, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val data = datas[position]

                if(data.cryrCd == null)
                    data.cryrCd = ""
                carrier_logo.setImageResource(data.cryrCd.getCarrierIcon())
                if(data.cryrCd.isNullOrBlank())
                    tv_carrier_name.text = context.getString(R.string.all_carriers)
                else
                    tv_carrier_name.text = data.cryrCd

                if(data.carrierCount!! > 0) {
                    tv_carrier_cnt.text = "+${DecimalFormat("#,###").format(data.carrierCount!!)}"
                } else {
                    tv_carrier_cnt.text = ""
                }

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
                    tv_volume.text = if (data.remainderQty!! >= 0) DecimalFormat("#,###").format(data.remainderQty!!) else ""
                }else {
                    ll_volume.visibility = View.GONE
                    tv_weeks.visibility = View.VISIBLE
                    tv_twk_value.text = "${currencyFormat.format(data.minPrice)}-${DecimalFormat("#,###").format(data.maxPrice)}"
                    tv_weeks.text = "${data.minYearWeek?.let { context.getWeek(data.minYearWeek) } ?: ""}-${data.maxYearWeek?.let { context.getWeek(data.maxYearWeek) } ?:""}"
                }
                setOnClickListener {
                    onClickOffer(data)
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

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: MarketWatchViewModel) : MarketWatchOfferListFragment {
            return MarketWatchOfferListFragment(viewModel)
        }
    }
}