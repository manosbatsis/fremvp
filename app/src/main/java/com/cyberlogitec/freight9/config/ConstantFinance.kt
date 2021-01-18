package com.cyberlogitec.freight9.config

/**
 * Filter - Date
 * ALL / POL ETD / POD ETA
 */
object FinanceFilterDate {
    const val FILTER_DATE_DEAL = 0
    const val FILTER_DATE_DUE = 1
    const val FILTER_DATE_POL_ETD = 2
    const val FILTER_DATE_POD_ETA = 3
}

object FinanceFilterDatePeriod {
    const val FILTER_DATE_PERIOD_CUSTOM = 0
    const val FILTER_DATE_PERIOD_1M = 1
    const val FILTER_DATE_PERIOD_3M = 2
    const val FILTER_DATE_PERIOD_6M = 3
}

object FinanceFilterSpinType {
    const val FILTER_SPIN_POL = 0
    const val FILTER_SPIN_POD = 1
}

object FinanceFilterDateType {
    const val FILTER_DATE_START = 0
    const val FILTER_DATE_END = 1
}

object FinanceFilterMoveType {
    const val FILTER_SCROLL_TO_TOP = 0
    const val FILTER_SCROLL_TO_STATUS = 1
    const val FILTER_SCROLL_TO_ROUTE = 2
    const val FILTER_SCROLL_TO_DATE = 3
    const val FILTER_SCROLL_TO_COLLECT_PLAN = 4
    const val FILTER_SCROLL_TO_TRANSACTION_TYPE = 5
    const val FILTER_SCROLL_TO_CASES = 6
    const val FILTER_SCROLL_TO_INVOICE_TRANSACTION_TYPE = 7
}