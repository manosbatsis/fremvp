package com.cyberlogitec.freight9.lib.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.BorList
import com.cyberlogitec.freight9.lib.model.OrderData
import com.cyberlogitec.freight9.lib.ui.enums.PayPlanEntry
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.ui.buyorder.*
import com.cyberlogitec.freight9.ui.inventory.InventoryContractPreviewPopup
import com.cyberlogitec.freight9.ui.inventory.InventoryDetailActivity
import com.cyberlogitec.freight9.ui.inventory.InventoryValuationPopup
import com.cyberlogitec.freight9.ui.inventory.RouteFilterPopup
import kotlinx.android.synthetic.main.popup_order_terms.view.*



/**
 * inventory 의 from, all, to Filter 조회 시 보여지는 Popup
 */
fun Context.showRouteSelectPopup(
        routePair: Pair<RouteFilterPopup.RouteFromTo, List<RouteFilterPopup.RouteAdapterData>>,
        onRouteSelectClick: ((Int, RouteFilterPopup.RouteFromTo, RouteFilterPopup.RouteAdapterData) -> Unit)) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_route_filter, null)
    val popupWindow = RouteFilterPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true, onRouteSelectClick)
    popupWindow.initValue(routePair.first, routePair.second)
    popupWindow.showAtLocation(view, Gravity.TOP, 0, 0 )
}

/**
 * buy/sell order, inventory detail, your offers detail, your offers history detail 에서 사용되는
 * "condition detail" 조회시 보여지는 popupp
 */
fun Context.showDetailConditionPopup(
        parentView: View,
        viewModel: Any?,
        data: Any? = null,
        isOfferNoSelect: Boolean = false,
        isEntryInventory: Boolean = false
) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_order_detail_condition, null)
    val popupWindow = DetailConditionPopup(view, viewModel, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true)
    popupWindow.initValue(data, isOfferNoSelect, isEntryInventory)
    popupWindow.show(parentView)
    popupWindow.isOutsideTouchable = true
}

/**
 * buy/sell order, inventory detail, your offers detail, your offers history detail 에서 사용되는
 * "whole route" 조회시 보여지는 popupp
 */
fun Context.showWholeRoutePopup(
        parentView: View,
        routeDataList: RouteDataList? = null,
        borList: BorList? = null
) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_order_whole_route, null)
    val popupWindow = WholeRoutePopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true)
    popupWindow.initValue(routeDataList, borList)
    popupWindow.show(parentView)
    popupWindow.isOutsideTouchable = true
}

/**
 * buy/sell order, inventory detail, your offers detail, your offers history detail 에서 사용되는
 * "price table" 조회시 보여지는 popupp
 */
fun Context.showPriceTablePopup(
        parentView: View,
        data: Any?,
        isOfferNoSelect: Boolean,
        isOffersEntry: Boolean = false
) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_order_price_table, null)
    val popupWindow = PriceTablePopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true)
    popupWindow.initValue(data, isOfferNoSelect, isOffersEntry)
    popupWindow.show(parentView)
    popupWindow.isOutsideTouchable = true
}

/**
 * buy/sell order, buy/sell offer 에서 사용되는
 * "Terms and Conditions > more" 조회시 보여지는 popupp
 */
fun Context.showTermsMorePopup(
        parentView: View
) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_order_terms, null)
    val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    popupWindow.isFocusable = true
    popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    popupWindow.isOutsideTouchable = true
    view.iv_order_terms_close.setSafeOnClickListener {
        popupWindow.dismiss()
    }
}

/**
 * buy/sell order 에서 사용되는
 * "Pay Plan/Collect Plan >" 조회시 보여지는 popupp
 */
fun Context.showPayPlanPopup(
        parentView: View,
        viewModel: Any?,
        data: Any?,
        payPlanEntry: PayPlanEntry = PayPlanEntry.PP_BuyOrder
) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_order_pay_plan, null)
    val popupWindow = PayPlanPopup(view, viewModel, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true)
    popupWindow.initValue(getString(if(payPlanEntry == PayPlanEntry.PP_BuyOrder) {
        R.string.buy_order_condition_popup_pay_plan
    } else {
        R.string.buy_order_payplan_popup_title
    }), data, payPlanEntry)
    popupWindow.show(parentView)
    popupWindow.isOutsideTouchable = true
}

/**
 * buy/sell order 에서 사용되는
 * "Period & Volume >" 조회시 보여지는 popupp
 */
fun Context.showPeriodAndVolumePopup(
        parentView: View,
        containerName: String,
        data: List<OrderData>,
        onPeriodAndVolumeEdit: (() -> Unit),
        is40Ft: Boolean = false) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_order_period_and_volume, null)
    val popupWindow = PeriodAndVolumePopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true, onPeriodAndVolumeEdit, is40Ft)
    popupWindow.initValue(containerName, data)
    popupWindow.show(parentView)
    popupWindow.isOutsideTouchable = true
}

/**
 * not used
 * inventory detail 에서 사용되는
 * "view detail" 조회시 보여지는 popupp
 */
fun Context.showInventoryValuationPopup(
        parentView: View,
        inventoryDetailList: MutableList<InventoryDetailActivity.InventoryDetailItem>) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_inventory_valuation, null)
    val popupWindow = InventoryValuationPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true)
    popupWindow.initValue(inventoryDetailList)
    popupWindow.show(parentView)
    popupWindow.isOutsideTouchable = true
}

/**
 * not used
 * inventory filtered list 의 item 에서
 * left, right swipe 시 보여지는 popupp
 */
fun Context.showContractPreviewPopup(
        isLeft: Boolean,
        data: Any? = null) {
    val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(R.layout.popup_inventory_contract_preview, null)
    val popupWindow = InventoryContractPreviewPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, true)
    popupWindow.initValue(isLeft, null)
    popupWindow.showAtLocation(view, Gravity.TOP, 0, 0 )
    popupWindow.isOutsideTouchable = true
}