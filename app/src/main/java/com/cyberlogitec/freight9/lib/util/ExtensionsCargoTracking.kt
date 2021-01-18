package com.cyberlogitec.freight9.lib.util

import android.content.Context
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode.VESSEL_ARRIVAL
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode.VESSEL_GOING
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode.VESSEL_NOT_READY
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode.VESSEL_READY
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingItem
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingStatusItem
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingStatusDetail
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingTimeLineResoource
import com.cyberlogitec.freight9.lib.ui.enums.CargoTrackingStatus


fun getCargoTrackingDetailData() : MutableList<CargoTrackingStatusItem> {

    val cargoTrackingStatusData = mutableListOf<CargoTrackingStatusItem>()

    for (x in 1..20) {
        val cargoTrackingStatusItem = CargoTrackingStatusItem()
        with(cargoTrackingStatusItem) {
            if (x < 10) {
                containerNo = "CAIU94634" + String.format("%02d", x)
            } else {
                containerNo = "CAIU94634$x"
            }
            isSelected = x == 1
            with(statusDetailList) {
                for (y in 1..4) {
                    val cargoStatusDetail = CargoTrackingStatusDetail()
                    with(cargoStatusDetail) {
                        when(y) {
                            1 -> {
                                status = CargoTrackingStatus.STATUS_ORIGIN_DEPARTURE
                                if (x == 1) {
                                    vesselStatus = VESSEL_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_GOING
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                            }
                            2 -> {
                                status = CargoTrackingStatus.STATUS_VESSEL_DEPARTURE
                                if (x == 1) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_READY
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_GOING
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                            }
                            3 -> {
                                status = CargoTrackingStatus.STATUS_VESSEL_ARRIVAL
                                if (x == 1) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_READY
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_GOING
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                            }
                            else -> {
                                status = CargoTrackingStatus.STATUS_DESTINATION_ARRIVAL
                                if (x == 1) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_READY
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                            }
                        }
                        portCode = "CNLYG"
                        portName = "Lianyungang, China"
                        dateTime = "2020-06-30 15:00"
                    }
                    add(cargoStatusDetail)
                }
            }
        }
        cargoTrackingStatusData.add(cargoTrackingStatusItem)
    }
    return cargoTrackingStatusData
}

fun getCargoTrackingItemData() : MutableList<CargoTrackingItem> {
    val cargoTrackingItemData = mutableListOf<CargoTrackingItem>()
    for (x in 1..20) {
        val cargoTrackingItem = CargoTrackingItem()
        with(cargoTrackingItem) {
            if (x < 10) {
                bookingNo = "AL00059895" + String.format("%02d", x)
            } else {
                bookingNo = "AL00059895$x"
            }
            carrierCode = "ONE"
            vvdDigitCode = "SPI MAERSK GUATEMALA 017W"
            with(statusDetailList) {
                for (y in 1..4) {
                    val cargoStatusDetail = CargoTrackingStatusDetail()
                    with(cargoStatusDetail) {
                        when(y) {
                            1 -> {
                                status = CargoTrackingStatus.STATUS_ORIGIN_DEPARTURE
                                if (x == 1) {
                                    vesselStatus = VESSEL_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_GOING
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                                portCode = "Innispree Corporation"
                                portName = Constant.EmptyString
                            }
                            2 -> {
                                status = CargoTrackingStatus.STATUS_VESSEL_DEPARTURE
                                if (x == 1) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_READY
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_GOING
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                                portCode = "CNSHA"
                                portName = "Shanghai, China"
                            }
                            3 -> {
                                status = CargoTrackingStatus.STATUS_VESSEL_ARRIVAL
                                if (x == 1) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_READY
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_GOING
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_ARRIVAL
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                                portCode = "USCHI"
                                portName = "Chicago IL, US"
                            }
                            else -> {
                                status = CargoTrackingStatus.STATUS_DESTINATION_ARRIVAL
                                if (x == 1) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 2) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 3) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 4) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 5) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 6) {
                                    vesselStatus = VESSEL_NOT_READY
                                } else if (x == 7) {
                                    vesselStatus = VESSEL_READY
                                } else {
                                    vesselStatus = VESSEL_NOT_READY
                                }
                                portCode = "AMOREPACIFIC SINGAPOR"
                                portName = Constant.EmptyString
                            }
                        }
                        dateTime = "2020-06-30 15:00"
                    }
                    add(cargoStatusDetail)
                }
            }
        }
        cargoTrackingItemData.add(cargoTrackingItem)
    }
    return cargoTrackingItemData
}

fun getCargoTrackingTimeSymbol(context: Context,
                               position: Int,
                               cargoTrackingStatusDetail: CargoTrackingStatusDetail
) : String {
    var timeSymbol = Constant.EmptyString
    with(cargoTrackingStatusDetail) {
        when (status) {
            // ETD or ATD
            CargoTrackingStatus.STATUS_VESSEL_DEPARTURE -> {
                if (vesselStatus == VESSEL_NOT_READY) {
                    timeSymbol = context.getString(R.string.cargo_tracking_time_symbol_etd)
                } else {
                    timeSymbol = context.getString(R.string.cargo_tracking_time_symbol_atd)
                }
            }
            // ETA or ATA
            CargoTrackingStatus.STATUS_VESSEL_ARRIVAL -> {
                if (vesselStatus == VESSEL_NOT_READY) {
                    timeSymbol = context.getString(R.string.cargo_tracking_time_symbol_eta)
                } else {
                    timeSymbol = context.getString(R.string.cargo_tracking_time_symbol_ata)
                }
            }
            else -> {  }
        }
    }
    return timeSymbol
}

fun getCargoTrackingTimeLineResoource(
        position: Int,
        datas: List<CargoTrackingStatusDetail>,
        isBackgroundPaleGray: Boolean = true
) : CargoTrackingTimeLineResoource {

    val cargoTrackingTimeLineResoource = CargoTrackingTimeLineResoource()

    with(cargoTrackingTimeLineResoource) {
        circleDrawable = if (isBackgroundPaleGray) {
            R.drawable.pale_grey_circle_12_12
        } else {
            R.drawable.white_grey_circle_12_12
        }

        val cargoStatusEnable = datas[position].vesselStatus != VESSEL_NOT_READY
        val nextPosition = position + 1
        val isNextNotReady = nextPosition < datas.size
                && datas[nextPosition].vesselStatus != VESSEL_NOT_READY

        // 현재 position 기준
        val isLine1Gradient = datas[position].vesselStatus == VESSEL_READY
        // Next Position 기준
        val isLine23Gradient = nextPosition < datas.size
                && datas[nextPosition].vesselStatus == VESSEL_READY

        if (cargoStatusEnable) {
            if (isNextNotReady) {
                circleDrawable = if (isBackgroundPaleGray) {
                    R.drawable.pale_greysh_circle_12_12
                } else {
                    R.drawable.white_greysh_circle_12_12
                }
                statusDrawable = R.drawable.bg_round_corner_13_454545
            } else {
                circleDrawable = if (isBackgroundPaleGray) {
                    R.drawable.pale_blue_circle_12_12
                } else {
                    R.drawable.white_blue_circle_12_12
                }
                statusDrawable = R.drawable.bg_round_corner_13_4a00e0
            }

            with(datas[position]) {
                when (vesselStatus) {
                    VESSEL_READY -> {
                        firstLineColor = R.color.color_454545
                    }
                    VESSEL_GOING -> {
                        firstLineColor = R.color.color_454545
                        secondLineColor = if (isNextNotReady) {
                            R.color.color_454545
                        } else {
                            R.color.color_4a00e0
                        }
                    }
                    VESSEL_ARRIVAL -> {
                        firstLineColor = R.color.color_454545
                        secondLineColor = if (isNextNotReady) {
                            R.color.color_454545
                        } else {
                            R.color.color_4a00e0
                        }
                        thirdLineColor = if (isNextNotReady) {
                            R.color.color_454545
                        } else {
                            R.color.color_4a00e0
                        }
                    }
                    else -> { }
                }
            }
        }

        if (isLine1Gradient) {
            firstLineColor = R.drawable.cargo_tracking_line1
        }

        if (isLine23Gradient) {
            secondLineColor = R.drawable.cargo_tracking_line2
            thirdLineColor = R.drawable.cargo_tracking_line3
        }

        isLineGradient = isLine1Gradient || isLine23Gradient
    }

    return cargoTrackingTimeLineResoource
}