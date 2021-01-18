package com.cyberlogitec.freight9.ui.trademarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Bor
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.route.RouteGridView
import com.cyberlogitec.freight9.lib.util.SharedPreferenceManager
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.marketwatch.MarketWatchViewModel
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.bottom_sheet_trademarket_offerlist_route.*
import kotlinx.android.synthetic.main.item_market_offer_condition_container.view.*
import timber.log.Timber

class MarketOfferRouteFragment constructor(val viewModel: BaseViewModel): RxFragment() {
    private var portDataList: RouteDataList = RouteDataList()
    private lateinit var requestItem: Bor


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.bottom_sheet_trademarket_offerlist_route, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        when(viewModel) {
            is MarketViewModel -> {

                viewModel.outPuts.onSuccessRequestOfferDetail()
                        .bindToLifecycle(this)
                        .subscribe {
                            Timber.d("f9: view onSuccessRequestOfferDetail")
                            updateUi(it)
                            updateConditionUi(it)
                            requestItem = it
                        }
                viewModel.outPuts.refreshSplitPopupDeatil ()
                        .bindToLifecycle(this)
                        .subscribe {
                            clearUi()
                        }
            }
            is MarketWatchViewModel -> {
                viewModel.outPuts.onSuccessRequestOfferDetail()
                        .bindToLifecycle(this)
                        .subscribe {
                            Timber.d("f9: view onSuccessRequestOfferDetail")
                            updateUi(it)
                            updateConditionUi(it)
                            requestItem = it
                        }
                viewModel.outPuts.refreshSplitPopupDeatil ()
                        .bindToLifecycle(this)
                        .subscribe {
                            clearUi()
                        }
            }

        }

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

    private fun clearUi() {
        ns_contents.scrollTo(0,0)
        route_grid_view.resetView()
        route_grid_view.invalidate()
        //carrier
        iv_carrier_logo.setImageResource("".getCarrierIcon())
        tv_carrier_name.text = ""
        //rdterm
        tv_rdterm.text = ""
        //containger size
        ll_container.removeAllViewsInLayout()
        //payplan
        tv_payplan_value.text = ""
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun updateConditionUi(bor: Bor) {
        //rdterm
        //계약에 포함된 offer 들의 rdterm rcvTrmCd, delTrmCd 를 확인하여 누적 및 중복은 제거
        if(ll_container.childCount > 0) {
            ll_container.removeAllViewsInLayout()
        }

        when(bor.detailList.offerRdTermCode) {
            "01" -> {tv_rdterm.text = resources.getString(R.string.rd_term_type_cycy)}
            "02" -> {tv_rdterm.text = resources.getString(R.string.rd_term_type_cydoor)}
            "03" -> {tv_rdterm.text = resources.getString(R.string.rd_term_type_doorcy)}
            "04" -> {tv_rdterm.text = resources.getString(R.string.rd_term_type_doordoor)}
        }


        var payData = bor.detailList.offerPaymentTermCode
        if(bor.item.offerTypeCode.equals("S"))
            tv_pay_plan_title.text = getString(R.string.buy_order_payplan_popup_title)
        else
            tv_pay_plan_title.text = getString(R.string.buy_order_pay_plan)

        when(bor.detailList.offerPaymentTermCode) {
            "01" -> {tv_payplan_value.text = resources.getString(R.string.payplan_prepaid)}
            "02" -> {tv_payplan_value.text = resources.getString(R.string.payplan_collect)}
        }
        tv_payplan_value.text = "${tv_payplan_value.text} (${(bor.detailList.offerLineItems[0].firstPaymentRatio*100).toInt()}% - ${(bor.detailList.offerLineItems[0].middlePaymentRatio*100).toInt()}% - ${(bor.detailList.offerLineItems[0].balancedPaymentRatio*100).toInt()}%)"

        var contents: String
        val containerList = bor.detailList.offerLineItems[0].offerPrices
        for(data in bor.detailList.offerLineItems[0].offerPrices.distinctBy { it.containerTypeCode }.sortedBy { it.containerTypeCode }){
            //view를 만들고 string을 만들어서 layout에 add 한다...
            contents = ""

            containerList.any { it.containerTypeCode == data.containerTypeCode && it.containerSizeCode == "01" }
                    .let { if(it) {if(contents.isNotEmpty())
                        contents += ", "
                        contents += "20'"} }
            containerList.any { it.containerTypeCode == data.containerTypeCode && it.containerSizeCode == "02" }
                    .let { if(it) {if(contents.isNotEmpty())
                        contents += ", "
                        contents += "40'"} }
            containerList.any { it.containerTypeCode == data.containerTypeCode && it.containerSizeCode == "03" }
                    .let { if(it) {if(contents.isNotEmpty())
                        contents += ", "
                        contents += "40'HC"} }
            containerList.any { it.containerTypeCode == data.containerTypeCode && it.containerSizeCode == "04" }
                    .let { if(it) {if(contents.isNotEmpty())
                        contents += ", "
                        contents += "45'HC"} }

            //view에 넣어 값을 넣어준다
            val view = layoutInflater.inflate(R.layout.item_market_offer_condition_container, null)
            when(data.containerTypeCode){
                "01" -> {view.tv_container_type.text = resources.getString(R.string.full_container_simple)}
                "02" -> {view.tv_container_type.text = resources.getString(R.string.rf_container_simple)}
                "03" -> {view.tv_container_type.text = resources.getString(R.string.empty_container_simple)}
                "04" -> {view.tv_container_type.text = resources.getString(R.string.soc_container_simple)}
            }
            view.tv_container_value.text = contents
            ll_container.addView(view)

        }

        iv_carrier_logo.setImageResource(bor.item.cryrCd!!.getCarrierIcon())
        //carrier
        if(bor.item.cryrCd.isNullOrBlank())
            tv_carrier_name.text = getString(R.string.all_carriers)
        else
            tv_carrier_name.text = bor.item.cryrCd
        //carrier name detail
        tv_carrier_name_detail.text = bor.item.cryrName

        if(bor.item.offerTypeCode.equals("S"))
            btn_make_new_offer.text = getString(R.string.buy_order_select_new_sell_offer)
        else
            btn_make_new_offer.text = getString(R.string.buy_order_select_new_buy_offer)
    }

    private fun updateUi(bor: Bor) {
        portDataList.clear()

        portDataList = bor.getRouteList()

        route_grid_view.resetView()
        route_grid_view.mPol = bor.item.locPolCd!!
        route_grid_view.mPod = bor.item.locPodCd!!

        route_grid_view.mViewType = RouteGridView.GridViewType.MARKET_POPUP
        route_grid_view.setData(portDataList)

        if(bor.item.offerTypeCode.equals("S"))
            btn_goto_order.text = getString(R.string.market_want_buy)
        else
            btn_goto_order.text = getString(R.string.market_want_sell)

        val share = SharedPreferenceManager(context)
        if(bor.item.ownerCompanyCode!! == share.name) {
            btn_goto_order.isEnabled = false
            btn_goto_order.text = getString(R.string.market_btn_this_is_your_offer)
        } else {
            btn_goto_order.isEnabled = true
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("f9: onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")

    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketOfferRouteFragment {
            return MarketOfferRouteFragment(viewModel)
        }
    }
}