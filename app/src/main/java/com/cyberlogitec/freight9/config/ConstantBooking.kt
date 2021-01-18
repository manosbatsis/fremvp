package com.cyberlogitec.freight9.config

/**
 * Filter - Date
 * ALL / POL ETD / POD ETA
 */
object FilterDate {
    const val FILTER_DATE_ALL = 0
    const val FILTER_DATE_POL_ETD = 1
    const val FILTER_DATE_POD_ETA = 2
}

object FilterDatePeriod {
    const val FILTER_DATE_PERIOD_CUSTOM = 0
    const val FILTER_DATE_PERIOD_1M = 1
    const val FILTER_DATE_PERIOD_3M = 2
    const val FILTER_DATE_PERIOD_6M = 3
}

object FilterSpinType {
    const val FILTER_SPIN_POL = 0
    const val FILTER_SPIN_POD = 1
}

object FilterDateType {
    const val FILTER_DATE_START = 0
    const val FILTER_DATE_END = 1
}

object FilterMoveType {
    const val FILTER_SCROLL_TO_TOP = 0
    const val FILTER_SCROLL_TO_ROUTE = 1
    const val FILTER_SCROLL_TO_DATE = 2
}

object BookingDashboardWeightTypeCode {
    const val CODE_UNIT_KGM = 0
    const val CODE_UNIT_LBR = 1

}