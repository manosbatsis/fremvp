package com.cyberlogitec.freight9.config

/**
 * Number type code
 */
object CargoTrackingNumberTypeCode {
    const val CODE_BOOKING_NO = 0
    const val CODE_CONTAINER_NO = 1
    const val CODE_BL_NO = 2
    const val CODE_VESSEL_VOYAGE_NO = 3
    const val CODE_CONSIGNEE_NAME = 4
    const val CODE_SHIPPER_NAME = 5
}

object CargoTrackingStatusCode {
    const val CODE_ORIGIN_DEPARTURE = 0
    const val CODE_VESSEL_DEPARTURE = 1
    const val CODE_VESSEL_ARRIVAL = 2
    const val CODE_VESSEL_BERTHING = 3
    const val CODE_DESTINATION_ARRIVAL = 4
}

object CargoTrackingVesselStatusCode {
    const val VESSEL_NOT_READY = 0        // grey circle, full grey line
    const val VESSEL_READY = 1            // blue circle
    const val VESSEL_GOING = 2            // blue circle, mid blue line
    /*
     * blue circle, full blue line
     * VESSEL_ARRIVAL 인 경우 Next port의 status 는 VESSEL_READY
     */
    const val VESSEL_ARRIVAL = 3
}

/**
 * Filter - Date
 * ALL / POL ETD / POD ETA
 */
object CargoTrackingFilterDate {
    const val FILTER_DATE_ALL = 0
    const val FILTER_DATE_POL_ETD = 1
    const val FILTER_DATE_POD_ETA = 2
}

object CargoTrackingFilterDatePeriod {
    const val FILTER_DATE_PERIOD_CUSTOM = 0
    const val FILTER_DATE_PERIOD_1M = 1
    const val FILTER_DATE_PERIOD_3M = 2
    const val FILTER_DATE_PERIOD_6M = 3
}

object CargoTrackingFilterSpinType {
    const val FILTER_SPIN_POL = 0
    const val FILTER_SPIN_POD = 1
}

object CargoTrackingFilterDateType {
    const val FILTER_DATE_START = 0
    const val FILTER_DATE_END = 1
}

object CargoTrackingFilterMoveType {
    const val FILTER_SCROLL_TO_TOP = 0
    const val FILTER_SCROLL_TO_ROUTE = 1
    const val FILTER_SCROLL_TO_DATE = 2
}