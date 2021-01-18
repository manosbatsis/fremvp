package com.cyberlogitec.freight9.lib.ui.enums

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.BookingDashboardWeightTypeCode.CODE_UNIT_KGM
import com.cyberlogitec.freight9.config.BookingDashboardWeightTypeCode.CODE_UNIT_LBR
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_BL_NO
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_BOOKING_NO
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_CONSIGNEE_NAME
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_CONTAINER_NO
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_SHIPPER_NAME
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_VESSEL_VOYAGE_NO

enum class BookingDashboardNumberType constructor(
        val code: Int,
        val id: Int
) {
    TYPE_BOOKING_NO(CODE_BOOKING_NO, R.string.cargo_tracking_search_booking_no),
    TYPE_BL_NO(CODE_BL_NO, R.string.booking_dashboard_search_bl_no),
    TYPE_VESSEL_NO(CODE_VESSEL_VOYAGE_NO, R.string.cargo_tracking_search_vessel_no),
    TYPE_CONTAINER_NO(CODE_CONTAINER_NO, R.string.cargo_tracking_search_container_no),
    TYPE_CONSIGNEE_NAME(CODE_CONSIGNEE_NAME, R.string.cargo_tracking_search_consignee);
    companion object {
        fun getBookingDashboardType(code: Int): BookingDashboardNumberType {
            for (bookingDashboardNumberType in values()) {
                if (bookingDashboardNumberType.code == code) {
                    return bookingDashboardNumberType
                }
            }
            return TYPE_BOOKING_NO
        }
    }
}

enum class BookingDashboardWeightType constructor(
        val code: Int,
        val id: Int
) {
    TYPE_KGM(CODE_UNIT_KGM, R.string.booking_dashboard_vgm_unit_kg),
    TYPE_LBR(CODE_UNIT_LBR, R.string.booking_dashboard_vgm_unit_pound);
    companion object {
        fun getBookingDashboardType(code: Int): BookingDashboardWeightType {
            for (bookingDashboardWeightType in values()) {
                if (bookingDashboardWeightType.code == code) {
                    return bookingDashboardWeightType
                }
            }
            return TYPE_KGM
        }
    }
}