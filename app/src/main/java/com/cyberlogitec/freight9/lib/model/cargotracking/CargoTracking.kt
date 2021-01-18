package com.cyberlogitec.freight9.lib.model.cargotracking

import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode.VESSEL_NOT_READY
import com.cyberlogitec.freight9.lib.ui.enums.CargoTrackingStatus
import java.io.Serializable

/*
 * Cargo Tracking Detail
 */
data class CargoTrackingStatusItem (
        var containerNo: String = "",
        var isSelected: Boolean = false,
        var statusDetailList: MutableList<CargoTrackingStatusDetail> = mutableListOf()
): Serializable

data class CargoTrackingStatusDetail(
        var status: CargoTrackingStatus = CargoTrackingStatus.STATUS_ORIGIN_DEPARTURE,
        var portCode: String = "",
        var portName: String = "",
        var dateTime: String = "",
        var vesselStatus: Int = VESSEL_NOT_READY
): Serializable

/*
 * Cargo Tracking List
 */
data class CargoTrackingItem (
        var isExpanded: Boolean = false,
        var bookingNo: String = "",
        var blNo: String = "",
        var hblNo: String = "",
        var mblNo: String = "",
        var containerNo: String = "",
        var consigneeName: String = "",
        var shipperName: String = "",
        var carrierCode: String = "",
        var vvdDigitCode: String = "",
        var statusDetailList: MutableList<CargoTrackingStatusDetail> = mutableListOf()
)

data class CargoTrackingTimeLineResoource (
        var isLineGradient: Boolean = false,
        var firstLineColor: Int = R.color.color_bfbfbf,
        var secondLineColor: Int = R.color.color_bfbfbf,
        var thirdLineColor: Int = R.color.color_bfbfbf,
        var circleDrawable: Int = R.drawable.pale_grey_circle_12_12,
        var statusDrawable: Int = R.drawable.bg_round_corner_13_bfbfbf
)