package com.cyberlogitec.freight9.lib.ui.enums

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.CargoTrackingStatusCode.CODE_DESTINATION_ARRIVAL
import com.cyberlogitec.freight9.config.CargoTrackingStatusCode.CODE_ORIGIN_DEPARTURE
import com.cyberlogitec.freight9.config.CargoTrackingStatusCode.CODE_VESSEL_ARRIVAL
import com.cyberlogitec.freight9.config.CargoTrackingStatusCode.CODE_VESSEL_BERTHING
import com.cyberlogitec.freight9.config.CargoTrackingStatusCode.CODE_VESSEL_DEPARTURE

enum class CargoTrackingStatus constructor(
        val code: Int,
        val id: Int
) {
    STATUS_ORIGIN_DEPARTURE(CODE_ORIGIN_DEPARTURE, R.string.cargo_tracking_status_origin_departure),
    STATUS_VESSEL_DEPARTURE(CODE_VESSEL_DEPARTURE, R.string.cargo_tracking_status_vessel_departure),
    STATUS_VESSEL_ARRIVAL(CODE_VESSEL_ARRIVAL, R.string.cargo_tracking_status_vessel_arrival),
    STATUS_VESSEL_BERTHING(CODE_VESSEL_BERTHING, R.string.cargo_tracking_status_vessel_berthing),
    STATUS_DESTINATION_ARRIVAL(CODE_DESTINATION_ARRIVAL, R.string.cargo_tracking_status_destination_arrival);
    companion object {
        fun getCargoTrackingStatus(code: Int): CargoTrackingStatus {
            for (cargoTrackingStatus in values()) {
                if (cargoTrackingStatus.code == code) {
                    return cargoTrackingStatus
                }
            }
            return STATUS_ORIGIN_DEPARTURE
        }
    }
}