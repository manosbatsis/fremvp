package com.cyberlogitec.freight9.lib.model.booking

import com.cyberlogitec.freight9.config.FilterDate.FILTER_DATE_ALL
import com.cyberlogitec.freight9.config.FilterDatePeriod.FILTER_DATE_PERIOD_CUSTOM
import com.cyberlogitec.freight9.config.FilterMoveType.FILTER_SCROLL_TO_TOP
import com.cyberlogitec.freight9.lib.ui.enums.BookingDashboardNumberType
import java.io.Serializable

data class BookingFilter (

        // Number type
        var numberType: BookingDashboardNumberType = BookingDashboardNumberType.TYPE_BOOKING_NO,
        var numberValue: String = "",

        // Status
        var isStatusBookingDraft: Boolean = false,
        var isStatusBookingConfirmed: Boolean = false,
        var isStatusPolArrived: Boolean = false,
        var isStatusBdr: Boolean = false,
        var isStatusPodArrived: Boolean = false,
        // Route
        var routePolCode: String = "",   // defalut : ""(All POLs)
        var routePolList: MutableList<Route> = mutableListOf(),
        var routePodCode: String = "",   // default : ""(All PODs)
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
            var code: String = "",
            var name: String = "",
            var isSelected: Boolean = false
    ): Serializable

    data class Date(
            var month: Int = 0,        // 1 ~ 12
            var day: Int = 0,          // 1 ~
            var year: Int = 0          // 1 ~
    ): Serializable
}

data class BookingSelectValue(
        var index: Int = 0,
        var moveType: Int = FILTER_SCROLL_TO_TOP,
        var value: String = "",
        var detail: String = ""
)