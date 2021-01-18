package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.youroffers.YourOffersActivity.Companion.yourOfferType
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.body_pol_pod_card.iv_carrier_logo
import kotlinx.android.synthetic.main.body_your_offers_swipe_preview.*
import kotlinx.android.synthetic.main.body_your_offers_swipe_preview.btn_view_detail
import kotlinx.android.synthetic.main.item_your_offers_swipe_left.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


class YourOffersSwipePreviewFragment constructor(val viewModel: YourOffersSwipeViewModel,
                                                 private val tradeOfferWrapper: TradeOfferWrapper)
    : RxFragment() {

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    /**
     * offer 주차별 리스트의 adapter
     */
    private val adapter by lazy {
        RecyclerAdapter()
                .apply {
                    // Do nothing
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.body_your_offers_swipe_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        setRxOutputs()
        initData()
        initView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("f9: onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")

    }

    private fun setRxOutputs() {

    }

    /**
     * fragment data init
     */
    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
        setRecyclerData()
    }

    /**
     * fragment view init
     */
    private fun initView() {
        setLayout()
        recyclerViewInit()
        setListener()
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        btn_view_detail.setSafeOnClickListener{
            Timber.d("f9: btn_view_detail click")
            viewModel.inPuts.clickToDetail(tradeOfferWrapper)
        }
    }

    /**
     * fragment layout init
     */
    private fun setLayout() {
        tv_title_value.text = if (yourOfferType == OFFER_TYPE_CODE_BUY) {
            activity!!.getString(R.string.cost_value)
        } else {
            activity!!.getString(R.string.sales_value)
        }

        with(tradeOfferWrapper.borList) {
            iv_carrier_logo.setImageResource(cryrCd.getCarrierIcon(false))
            tv_carrier_name.text = activity!!.getCarrierCode(cryrCd)
            tv_carrier_count.text = carrierCount.getCodeCount()
            tv_pol_name.text = locPolCd
            tv_pol_count.text = locPolCnt.getCodeCount()
            tv_pol_desc.text = locPolNm
            tv_pod_name.text = locPodCd
            tv_pod_count.text = locPodCnt.getCodeCount()
            tv_pod_desc.text = locPodNm
        }
    }

    /**
     * offer detail screen : RecyclerView <-> Adapter
     */
    private fun recyclerViewInit() {
        recycler.apply {
            Timber.d("f9: recyclerViewInit")
            layoutManager = LinearLayoutManager(this@YourOffersSwipePreviewFragment.context)
            adapter = this@YourOffersSwipePreviewFragment.adapter
        }
    }

    /**
     * weeks data 를 adapter 에 설정
     */
    private fun setRecyclerData() {
        tradeOfferWrapper.cellLineItems.let { lineItems ->
            adapter.setData(yourOfferType, lineItems.sortedBy { it.baseYearWeek })
            adapter.notifyDataSetChanged()

            // sumBy(leftQty * leftPrice) : Buy, Sell 동일
            tv_bottom_value.text = currencyFormat.format(lineItems.sumByLong {
                (it.leftQty * it.leftPrice).toLong()
            })
        }
    }

    /**
     * offer detail screen 의 recyclerview adapter
     */
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        lateinit var context: Context
        val data = mutableListOf<Dashboard.Cell.LineItem>()
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        private var yourOfferType: String = OFFER_TYPE_CODE_BUY

        fun setData(yourOfferType: String, data: List<Dashboard.Cell.LineItem>) {
            this.yourOfferType = yourOfferType
            this.data.clear()
            this.data.addAll(data)
        }

        override fun getItemCount(): Int = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            currencyFormat.minimumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_your_offers_swipe_left, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                with(data[position]) {
                    tv_period.text = context.getWeek(baseYearWeek)
                    tv_price.text = currencyFormat.format(leftPrice)
                    tv_dealt.text = "${dealQty}T"
                    tv_left.text = "${leftQty}T"
                    // leftQty * leftPrice : Buy, Sell 동일
                    tv_value.text = currencyFormat.format((leftQty * leftPrice).toLong())
                }
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: YourOffersSwipeViewModel, tradeOfferWrapper: TradeOfferWrapper)
                : YourOffersSwipePreviewFragment {
            return YourOffersSwipePreviewFragment(viewModel, tradeOfferWrapper)
        }
    }
}