package com.cyberlogitec.freight9.lib.model

import com.cyberlogitec.freight9.lib.ui.enums.RdTermItemTypes
import java.io.Serializable

//----------------------------------------------------------------------------------------------
/**
 * Recycler View 에서 사용될 data class
 */
data class OrderData(
        var blink: Boolean = false,
        var focused: Boolean = false,
        var check: Boolean = false,
        var bseYw: String = "",           // yyyyww
        var price: Int = 0,          // Container spin 변경 없음
        var volume: Int = 0,              // T, F 구분하지 않음.(사용자 입력값)
        var offerWeekQty: Int = 0,        // T, F 구분하지 않음.
        var subTotal: Int = 0,
        var isT: Boolean = true,          // T or F
        var inventoryValue: Int = 0      // user in Sell Order
)

//----------------------------------------------------------------------------------------------
/**
 * Wrapper data class
 */
data class TradeOfferWrapper(
        var borList: BorList,
        var orderTradeOfferDetail: OrderTradeOfferDetail,
        var datas: MutableList<OrderData>,
        var cellLineItems: MutableList<Dashboard.Cell.LineItem>
): Serializable

//----------------------------------------------------------------------------------------------
/**
 * Container Info
 */
data class ContainerSimpleInfo (
        var rdTerm: RdTermItemTypes,
        var containerTypeCode: String
)

data class PriceItem (
        var bseYw: String,
        var price: String,
        var volumeBase: String
)

//----------------------------------------------------------------------------------------------
/**
 * Whole Rounte 에서 사용될 data class
 */
data class WholeRoutesWrapper(
        var laneNum: String,
        var wholeRoutes: MutableList<WholeRoute>
)

data class WholeRoute(
        var portCd: String = "",
        var portName: String = "",
        var arriveDate: String = "",
        var arriveTime: String = "",
        var departureDate: String = "",     // yyyy-mm-dd
        var departureTime: String = "",     // HH:MM
        var isSearchedLane: Boolean = false,
        var isOceanSection: Boolean = false,
        var isSelected: Boolean = false,
        var portKind: PortKind = PortKind.PORT_NONE,
        var relativeIndex: Int = -1
)

enum class PortKind {
    PORT_NONE,
    PORT_POL,
    PORT_POR,
    PORT_POD,
    PORT_DEL
}