package com.cyberlogitec.freight9.ui.youroffers

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_20FT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_40FT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_40FTHC
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_SIZE_CODE_45FTHC
import com.cyberlogitec.freight9.config.ConstantTradeOffer.CONTAINER_TYPE_CODE_DRY
import com.cyberlogitec.freight9.lib.model.OrderTradeOfferDetail
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.ui.enums.ContainerName
import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import com.cyberlogitec.freight9.lib.ui.route.RouteGridView
import com.cyberlogitec.freight9.lib.util.*
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_your_offers_swipe_route.*
import timber.log.Timber


class YourOffersSwipeRouteFragment constructor(val viewModel: YourOffersSwipeViewModel,
                                               private val tradeOfferWrapper: TradeOfferWrapper)
    : RxFragment() {

    private var isGridView = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.body_your_offers_swipe_route, container, false)

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

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRequestCarrierName()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { carrierName ->
                        tv_carrier_desc.text = carrierName
                    }
                }
    }

    /**
     * fragment data init
     */
    private fun initData() {

        setUiData()
    }

    /**
     * fragment view init
     */
    private fun initView() {

        setListener()
    }

    /**
     * reference : DetailConditionPopup.kt
     */
    private fun setUiData() {

        with(tradeOfferWrapper) {
            /**
             * Routes
             */
            if (!isGridView()) {
                route_grid_view.resetView()   //재사용시 view 초기화 시켜줘야함
                route_grid_view.mPol = borList.locPolCd ?: "CNSHAL"
                route_grid_view.mPod = borList.locPodCd ?: "CNSHAD"
                route_grid_view.mViewType = RouteGridView.GridViewType.SELL_OFFER
                route_grid_view.setData(makeRouteDataList(orderTradeOfferDetail.offerRoutes))
                setGridView(true)
            }

            /**
             * Carriers
             * iv_carrier_logo,
             * tv_carrier,
             * tv_carrier_desc : Load from Db
             */
            val carrierCode = borList.cryrCd
            iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
            tv_carrier.text = activity!!.getCarrierCode(carrierCode)
            if (!carrierCode.isNullOrEmpty()) {
                viewModel.inPuts.requestCarrierName(carrierCode)
            }

            /**
             * Receive & Delivery Term
             * tv_rdterm
             */
            tv_rdterm.text = getString(RdTermItemTypes.getRdTermItemType(borList.rdTermCode!!)!!.rdNameId)

            /**
             * Container Type & Size
             * ll_containertype : item height = 30dp / center_vertical
             */
            ll_containertype.removeAllViews()
            ll_containertype.addView(makeContainerTypeLayout(this))

            /**
             * Pay Plan
             * tv_pay_plan : Load from Server
             */
            val offerLineItem = orderTradeOfferDetail.offerLineItems[0]
            val pymtType =
                    if (orderTradeOfferDetail.offerPaymentTermCode == ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD) {
                        getString(R.string.payplan_prepaid)
                    } else {
                        getString(R.string.payplan_collect)
                    }
            val payPlan = getString(R.string.payplan_rate_full_display, pymtType,
                    (offerLineItem.firstPaymentRatio * 100).toInt().toString(),
                    (offerLineItem.middlePaymentRatio * 100).toInt().toString(),
                    (offerLineItem.balancedPaymentRatio * 100).toInt().toString())
            tv_pay_plan.text = payPlan
        }
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

    private fun makeContainerTypeLayout(tradeOfferWrapper: TradeOfferWrapper): LinearLayout {
        val llList = LinearLayout(context)
        llList.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llList.orientation = LinearLayout.VERTICAL
        llList.gravity = Gravity.CENTER_VERTICAL

        val groupByContainerTypeCode =
                with(tradeOfferWrapper.orderTradeOfferDetail.offerLineItems[0]) {
                    if (offerPrices.isNotEmpty()) {
                        offerPrices.sortedBy { it.containerSizeCode }
                                .groupBy { it.containerTypeCode }
                                .toMutableMap()
                    } else {
                        // TODO : offerPrices size가 0인 경우 Dry 20' 만 표시
                        mapOf(CONTAINER_SIZE_CODE_20FT to listOf(OrderTradeOfferDetail.OfferLineItem.OfferPrice(
                                containerTypeOrder = 0,
                                containerSizeOrder = 0,
                                offerNumber = offerNumber,
                                offerChangeSeq = offerChangeSeq,
                                offerItemSeq = offerItemSeq,
                                deleteYn = deleteYn,
                                containerTypeCode = CONTAINER_TYPE_CODE_DRY,
                                containerSizeCode = CONTAINER_SIZE_CODE_20FT,
                                offerPrice = offerPrice
                        )))
                    }
                }

        for ((key, values) in groupByContainerTypeCode) {
            val llSub = LinearLayout(context)
            llSub.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            llSub.orientation = LinearLayout.HORIZONTAL
            llSub.gravity = Gravity.CENTER_VERTICAL
            val margin = 4.toDp().toInt()
            val params = llSub.layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, margin, 0, margin)
            llSub.layoutParams = params

            // Left TextView
            llSub.addView(makeContainerNameView(key))
            // Right TextView
            val containerSizeView = makeContainerSizeView()

            // Size 구성
            var rightContent = ""
            for ((index, value) in values.withIndex()) {
                var id = R.string.container_size_20_abbrev
                when (value.containerSizeCode) {
                    CONTAINER_SIZE_CODE_20FT -> {
                        id = R.string.container_size_20_abbrev
                    }
                    CONTAINER_SIZE_CODE_40FT -> {
                        id = R.string.container_size_40_abbrev
                    }
                    CONTAINER_SIZE_CODE_40FTHC -> {
                        id = R.string.container_size_40hc_abbrev
                    }
                    CONTAINER_SIZE_CODE_45FTHC -> {
                        id = R.string.container_size_45hc_abbrev
                    }
                }
                val nameShort = getString(id)
                rightContent += (if (index > -1 && index < values.size - 1) ("$nameShort, ") else nameShort)
            }
            containerSizeView.text = rightContent

            llSub.addView(containerSizeView)
            llList.addView(llSub)
        }

        return llList
    }

    private fun makeContainerNameView(containerType: String): TextView {
        // 86dp
        val textviewLeft = TextView(context)
        textviewLeft.layoutParams = LinearLayout.LayoutParams(86.toDp().toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        textviewLeft.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        textviewLeft.includeFontPadding = false
        textviewLeft.setTextAppearance(R.style.txt_opensans_r_15_595959)
        textviewLeft.text = getString(ContainerName.getContainerName(containerType)!!.nameMiddleId)
        return textviewLeft
    }

    private fun makeContainerSizeView(): TextView {
        // 0dp (weight 1)
        val textviewRight = TextView(context)
        textviewRight.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        val params = textviewRight.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        textviewRight.layoutParams = params
        textviewRight.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        textviewRight.includeFontPadding = false
        textviewRight.setTextAppearance(R.style.txt_opensans_r_15_595959)
        return textviewRight
    }

    private fun isGridView(): Boolean {
        return isGridView
    }

    private fun setGridView(isGridView: Boolean) {
        this.isGridView = isGridView
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: YourOffersSwipeViewModel, tradeOfferWrapper: TradeOfferWrapper)
                : YourOffersSwipeRouteFragment {
            return YourOffersSwipeRouteFragment(viewModel, tradeOfferWrapper)
        }
    }
}