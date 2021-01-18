package com.cyberlogitec.freight9.lib.model.cargotracking

import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_ALL
import com.cyberlogitec.freight9.config.CargoTrackingFilterDatePeriod.FILTER_DATE_PERIOD_CUSTOM
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_TOP
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.lib.ui.enums.CargoTrackingNumberType
import java.io.Serializable

data class CargoTrackingFilter (
        // Number type
        var numberType: CargoTrackingNumberType = CargoTrackingNumberType.TYPE_BL_NO,
        var numberValue: String = Constant.EmptyString,
        // Route
        var routePolCode: String = Constant.EmptyString,   // defalut : ""(All POLs)
        var routePolList: MutableList<Route> = mutableListOf(),
        var routePodCode: String = Constant.EmptyString,   // default : ""(All PODs)
        var routePodList: MutableList<Route> = mutableListOf(),
        // Date
        var dateType: Int = FILTER_DATE_ALL,                    // defalut : FILTER_DATE_ALL
        var datePeriodType: Int = FILTER_DATE_PERIOD_CUSTOM,    // defalut : FILTER_DATE_PERIOD_CUSTOM
        var dateStarts: Date = Date(),
        var dateEnds: Date = Date(),
        // For Scroll To XXX
        var scrollType: Int = FILTER_SCROLL_TO_TOP
): Serializable
{
    data class Route(
            var code: String = Constant.EmptyString,
            var name: String = Constant.EmptyString,
            var isSelected: Boolean = false
    ): Serializable

    data class Date(
            var month: Int = 0,        // 1 ~ 12
            var day: Int = 0,          // 1 ~
            var year: Int = 0          // 1 ~
    ): Serializable
}

data class CargoTrackingSelectValue(
        var index: Int = 0,
        var moveType: Int = FILTER_SCROLL_TO_TOP,
        var value: String = Constant.EmptyString,
        var detail: String = Constant.EmptyString
)