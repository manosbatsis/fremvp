package com.cyberlogitec.freight9.lib.ui.enums

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_BL_NO
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_BOOKING_NO
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_CONSIGNEE_NAME
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_CONTAINER_NO
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_SHIPPER_NAME
import com.cyberlogitec.freight9.config.CargoTrackingNumberTypeCode.CODE_VESSEL_VOYAGE_NO

enum class CargoTrackingNumberType constructor(
        val code: Int,
        val id: Int
) {
    TYPE_BOOKING_NO(CODE_BOOKING_NO, R.string.cargo_tracking_search_booking_no),
    TYPE_CONTAINER_NO(CODE_CONTAINER_NO, R.string.cargo_tracking_search_container_no),
    TYPE_BL_NO(CODE_BL_NO, R.string.cargo_tracking_search_bl_no),
    TYPE_VESSEL_NO(CODE_VESSEL_VOYAGE_NO, R.string.cargo_tracking_search_vessel_no),
    TYPE_CONSIGNEE_NAME(CODE_CONSIGNEE_NAME, R.string.cargo_tracking_search_consignee),
    TYPE_SHIPPER_NAME(CODE_SHIPPER_NAME, R.string.cargo_tracking_search_shipper);
    companion object {
        fun getCargoTrackingType(code: Int): CargoTrackingNumberType {
            for (cargoTrackingNumberType in values()) {
                if (cargoTrackingNumberType.code == code) {
                    return cargoTrackingNumberType
                }
            }
            return TYPE_BOOKING_NO
        }
    }
}