package com.cyberlogitec.freight9.ui.trademarket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.BorList
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeableRecyclerViewTouchListener
import com.cyberlogitec.freight9.ui.inventory.InventoryViewModel
import com.github.mikephil.charting.data.OfferEntry
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.bottom_sheet_trademarket_offerlist.*
import kotlinx.android.synthetic.main.item_market_offer_split.view.*
import timber.log.Timber

class MarketCounterOfferListFragment constructor(val viewModel: BaseViewModel): RxFragment(), SwipeableRecyclerViewTouchListener.SwipeListener {

    private var offerType:OfferEntry.OfferType = OfferEntry.OfferType.BUY_OFFER

    override fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        if(viewModel is InventoryViewModel)
            viewModel.inPuts.swipeToOfferDetail(recyclerView.id.toLong())
    }

    override fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        if(viewModel is InventoryViewModel)
            viewModel.inPuts.swipeToOfferRoute(recyclerView.id.toLong())
    }

    override fun canSwipeLeft(position: Int): Boolean {
//        if(viewModel is InventoryViewModel)
//            viewModel.inPuts.requestOfferDetail(adapter.datas[position])
        return true
    }

    override fun canSwipeRight(position: Int): Boolean {
        return true
    }

    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    if(viewModel is MarketViewModel) {
                        onClickBuyOffer = {
                            viewModel.inPuts.clickBuyOfferItem(it)
                        }
                        onClickSellOffer = {
                            viewModel.inPuts.clickSellOfferItem(it)
                        }
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.bottom_sheet_trademarket_offerlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        ll_period.visibility = View.GONE

        cb_trade_offerlist_viewperiod.setOnCheckedChangeListener { cb_trade_offerlist_viewperiod, isChecked ->
            adapter.setViewPeriodMode(isChecked)
            adapter.notifyDataSetChanged()
        }

        if(viewModel is MarketViewModel) {

            viewModel.outPuts.onSuccessRequestBuyOfferLists()
                    .bindToLifecycle(this)
                    .subscribe {
                        Timber.d("f9: view onSuccessRequestBuyOfferInfos")
                        offerType = OfferEntry.OfferType.BUY_OFFER
                        refreshList(it)
                    }
            viewModel.outPuts.onSuccessRequestSellOfferLists()
                    .bindToLifecycle(this)
                    .subscribe {
                        Timber.d("f9: view onSuccessRequestSellOfferInfos")
                        offerType = OfferEntry.OfferType.SELL_OFFER
                        refreshList(it)
                    }

            viewModel.outPuts.onSuccessRequestOfferDetail()
                    .bindToLifecycle(this)
                    .subscribe {
                        Timber.d("f9: view onSuccessRequestBuyOfferInfos \n $it")
                    }
        }
        recyclerViewInit()


        val list: MutableList<BorList> = ArrayList()
        list.add(BorList())
        list.add(BorList())
        list.add(BorList())
        list.add(BorList())
        list.add(BorList())
        refreshList(list)

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
            adapter = this@MarketCounterOfferListFragment.adapter
        }
        val touchListener = SwipeableRecyclerViewTouchListener(recycler_view_market_offer_list, this@MarketCounterOfferListFragment)
        recycler_view_market_offer_list.addOnItemTouchListener(touchListener)
    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<BorList>()
        var viewPeroidMode : Boolean = false
        private var offerType = OfferEntry.OfferType.SELL_OFFER

        var onClickBuyOffer: (BorList) -> Unit = {}
        var onClickSellOffer: (BorList) -> Unit = {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_market_counter_offer_split, parent, false))

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                ll_volume.visibility = View.GONE
                /*val data = datas[position]

                carrier_logo.setImageResource(data.cryrCd!!.getCarrierIcon())
                tv_carrier_name.text = data.cryrCd
                tv_pol_name.text = data.locPolCd
                tv_pol_cnt.text = "+${data.locPolCnt.toString()}"
                tv_pol_detail.text = data.locPolNm

                tv_pod_name.text = data.locPodCd
                tv_pod_cnt.text = "+${data.locPodCnt.toString()}"
                tv_pod_detail.text = data.locPodNm

                tv_PartialOrFull.text = if (data.weekPrtlYn.equals("P")) "Partial" else "Full"

                if(!viewPeroidMode){
                    holder.itemView.tv_twk_value.text = data.weekPrice.toString()
                    holder.itemView.tv_weeks.text = "${data.weekQty}T"
                }else {
                    holder.itemView.tv_twk_value.text = "$${data.maxPrice}-${data.minPrice}"
                    holder.itemView.tv_weeks.text = "${context.getWeek(data.fromWeek)}-${context.getWeek(data.toWeek)}"
                }
                setOnClickListener {

                    if(offerType == OfferEntry.OfferType.BUY_OFFER)
                        onClickBuyOffer(data)
                    else
                        onClickSellOffer(data)
                }*/
            }
        }

        fun setViewPeriodMode(periodMode:Boolean) {
            viewPeroidMode = periodMode
        }

        fun setOfferType(type:OfferEntry.OfferType) {
            offerType = type
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketCounterOfferListFragment {
            return MarketCounterOfferListFragment(viewModel)
        }
    }
}