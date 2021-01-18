package com.cyberlogitec.freight9.ui.cargotracking

import android.content.Context
import android.view.View
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingStatusDetail
import com.cyberlogitec.freight9.lib.util.getCargoTrackingTimeLineResoource
import com.cyberlogitec.freight9.lib.util.getCargoTrackingTimeSymbol
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule_small.view.*

object CargoTrackingScheduleItem {

    fun setScheduleItem(context: Context,
                view: View,
                index: Int,
                statusDetailList: List<CargoTrackingStatusDetail>) {

        with(view) {
            when (index) {
                0 -> {
                    view_status_first_line.visibility = View.INVISIBLE
                }
                3 -> {
                    view_status_second_line.visibility = View.INVISIBLE
                    view_status_third_line.visibility = View.INVISIBLE
                }
                else -> { /* Do nothing */ }
            }

            with(statusDetailList[index]) {
                tv_cargo_status.text = context.getString(status.id)
                tv_cargo_port_code.text = portCode
                tv_cargo_port_name.text = portName
                val timeSymbol = getCargoTrackingTimeSymbol(context, index, this)
                tv_cargo_date_time_symbol.visibility = if (timeSymbol.isEmpty()) View.GONE else View.VISIBLE
                tv_cargo_date_time_symbol.text = timeSymbol
                tv_cargo_date_time.text = dateTime
            }

            setStatusUI(context, this, index, statusDetailList)
        }
    }

    private fun setStatusUI(context: Context,
                            view: View,
                            index: Int,
                            statusDetailList: List<CargoTrackingStatusDetail>) {

        val cargoTrackingTimeLineResource = getCargoTrackingTimeLineResoource(
                index,
                statusDetailList,
                false)

        with(view) {
            with(cargoTrackingTimeLineResource) {
                if (isLineGradient) {
                    view_status_first_line.background = context.getDrawable(firstLineColor)
                } else {
                    view_status_first_line.setBackgroundColor(context.getColor(firstLineColor))
                }
                if (isLineGradient) {
                    view_status_second_line.background = context.getDrawable(secondLineColor)
                    view_status_third_line.background = context.getDrawable(thirdLineColor)
                } else {
                    view_status_second_line.setBackgroundColor(context.getColor(secondLineColor))
                    view_status_third_line.setBackgroundColor(context.getColor(thirdLineColor))
                }
                view_status_circle.background = context.getDrawable(circleDrawable)
                tv_cargo_status.background = context.getDrawable(statusDrawable)
            }
        }
        setStatusExtraUI(context, view, statusDetailList[index])
    }

    private fun setStatusExtraUI(context: Context,
                                 view: View,
                                 cargoTrackingStatusDetail: CargoTrackingStatusDetail) {
        with (view) {
            with(cargoTrackingStatusDetail) {
                if (vesselStatus == CargoTrackingVesselStatusCode.VESSEL_NOT_READY) {
                    tv_cargo_port_code.setTextColor(context.getColor(R.color.color_bfbfbf))
                    tv_cargo_port_name.setTextColor(context.getColor(R.color.color_bfbfbf))
                    tv_cargo_date_time_symbol.setTextColor(context.getColor(R.color.color_bfbfbf))
                    tv_cargo_date_time.setTextColor(context.getColor(R.color.color_bfbfbf))
                } else {
                    tv_cargo_port_code.setTextColor(context.getColor(R.color.color_4c4c4c))
                    tv_cargo_port_name.setTextColor(context.getColor(R.color.color_4c4c4c))
                    tv_cargo_date_time_symbol.setTextColor(context.getColor(R.color.color_4c4c4c))
                    tv_cargo_date_time.setTextColor(context.getColor(R.color.color_4c4c4c))
                }
            }
        }
    }
}