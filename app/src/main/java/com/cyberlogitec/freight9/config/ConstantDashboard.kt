package com.cyberlogitec.freight9.config

object OfferType {
    const val OFFER_BUY = "B"
    const val OFFER_SELL = "S"
}

object FinanceType {
    const val FINANCE_TYPE_NONE = "N"
    const val FINANCE_TYPE_PAY_COLLECT_PLAN = "P"
    const val FINANCE_TYPE_TRANSACTION_STATEMENT = "T"
    const val FINANCE_TYPE_INVOICE = "I"
}

// "01"=offerPlaced, "02"=dealt, "03"=weekExpired,  "04"=allDealt, "05"=offerClosed, "05"=offerCanceled
object EventCode {
    const val EVENT_CODE_OFFER_PLACED = "01"
    const val EVENT_CODE_DEALT = "02"
    const val EVENT_CODE_WEEK_EXPIRED = "03"
    const val EVENT_CODE_ALL_DEALT = "04"
    const val EVENT_CODE_OFFER_CLOSED = "05"
    const val EVENT_CODE_OFFER_CANCELED = "06"
}

object StatusType {
    const val STATUS_MARKET_TYPE = "01"
    const val STATUS_MARKET_CLOSED_TYPE = "02"
    const val STATUS_MARKET_DRAFT_TYPE = "03"
    const val STATUS_DRAFT_TYPE = "04"
    const val STATUS_DRAFT_CLOSED_TYPE = "05"
    const val STATUS_DRAFT_MARKET_TYPE = "06"
    const val STATUS_DRAFT_CLOSING_TYPE = "07"
}

object FilterType {
    const val FILTER_PERIOD = "filter_period"
    const val FILTER_EVENT_TYPE = "filter_event_type"
    const val FILTER_DATE = "filter_date"
    const val FILTER_DATE_FROM = "filter_date_from"
    const val FILTER_DATE_TO = "filter_date_to"
}

object FilterOnMarket {
    const val FILTER_ONMARKET = "1"
    const val FILTER_CLOSED = "0"
}

object AllYn {
    const val ALL_YN_WHOLE = "1"
    const val ALL_YN_PARTIAL = "0"
}

object EventInitial {
    const val EVENT_INITIAL_PRODUCT = "F"
    const val EVENT_INITIAL_DEALT = "D"
}
