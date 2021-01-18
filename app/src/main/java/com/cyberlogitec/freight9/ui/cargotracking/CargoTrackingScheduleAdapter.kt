package com.cyberlogitec.freight9.ui.cargotracking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.CargoTrackingVesselStatusCode.VESSEL_NOT_READY
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingStatusDetail
import com.cyberlogitec.freight9.lib.util.getCargoTrackingTimeLineResoource
import com.cyberlogitec.freight9.lib.util.getCargoTrackingTimeSymbol
import com.cyberlogitec.freight9.lib.util.toDp
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.*
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.cl_item_cargo_tracking_detail_schedule
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.fl_item_cargo_tracking_detail_schedule_circle
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.ll_item_cargo_tracking_detail_schedule
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.tv_cargo_date_time
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.tv_cargo_date_time_symbol
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.tv_cargo_port_code
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.tv_cargo_port_name
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.tv_cargo_status
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.view_status_circle
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.view_status_first_line
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.view_status_second_line
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule.view.view_status_third_line
import kotlinx.android.synthetic.main.item_cargo_tracking_detail_schedule_small.view.*

class CargoTrackingScheduleAdapter : RecyclerView.Adapter<CargoTrackingScheduleAdapter.ViewHolder>() {

    private lateinit var context: Context

    // true: margin apply, false: No margin
    private var isMarginApply = true
    // true : pale_gray, false : white
    private var isBackgroundPaleGray: Boolean = true
    private var datas = listOf<CargoTrackingStatusDetail>()

    fun setMarginApply(isMarginApply: Boolean) {
        this.isMarginApply = isMarginApply
    }

    fun setBackground(isBackgroundPaleGray: Boolean) {
        this.isBackgroundPaleGray = isBackgroundPaleGray
    }

    fun setData(datas: List<CargoTrackingStatusDetail>) {
        this.datas = datas
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_cargo_tracking_detail_schedule, parent, false))
    }

    override fun getItemCount(): Int = datas.size

    /*
    * view_status_first_line (visible/invisible)
    * view_status_second_line
    * view_status_third_line
    * > grey : @color/color_bfbfbf
    * > blue : @color/color_4a00e0
    *
    * view_status_circle
    * > grey : android:background="@drawable/pale_grey_circle_12_12"
    * > blue : android:background="@drawable/pale_blue_circle_12_12"
    *
    * tv_cargo_status
    * > enable : true - blue
    * > enable : false - grey
    *
    * tv_cargo_port_code
    * > grey : txt_opensans_eb_18_bfbfbf
    * > black : txt_opensans_eb_18_greyshbrown (4c4c4c)
    *
    * tv_cargo_date_time
    * > grey : txt_opensans_r_13_bfbfbf
    * > black : txt_opensans_r_13_greyshbrown (4c4c4c)
    * */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {

            // background color of item
            cl_item_cargo_tracking_detail_schedule.setBackgroundColor(context.getColor(
                    if (isBackgroundPaleGray) {
                        R.color.pale_gray
                    } else {
                        R.color.colorWhite
                    })
            )

            // margin apply of firstIitem, lastItem
            if (isMarginApply) {
                setItemMargin(this, position)
            }

            view_status_first_line.visibility = if (position == 0) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            view_status_second_line.visibility = if (position == itemCount - 1) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            view_status_third_line.visibility = if (position == itemCount - 1) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            setStatusUI(this, position)
            setExtraData(this, position)
        }
    }

    private fun setItemMargin(view: View, position: Int) {
        var marginTop = 0.toDp().toInt()
        var marginBottom = 0.toDp().toInt()
        var marginTopOfCircle = 7.toDp().toInt()
        if (position == 0) {
            marginTop = 50.toDp().toInt()
            marginTopOfCircle = 57.toDp().toInt()
        } else if (position == itemCount - 1) {
            marginBottom = 50.toDp().toInt()
        }

        with(view) {
            val llParams = ll_item_cargo_tracking_detail_schedule.layoutParams
                    as CoordinatorLayout.LayoutParams
            llParams.topMargin = marginTop
            llParams.bottomMargin = marginBottom
            ll_item_cargo_tracking_detail_schedule.layoutParams = llParams

            val flParams = fl_item_cargo_tracking_detail_schedule_circle.layoutParams
                    as CoordinatorLayout.LayoutParams
            flParams.topMargin = marginTopOfCircle
            fl_item_cargo_tracking_detail_schedule_circle.layoutParams = flParams
        }
    }

    /*
    * line, circle
    * */
    private fun setStatusUI(view: View, position: Int) {

        val cargoTrackingTimeLineResoource = getCargoTrackingTimeLineResoource(position, datas)

        with(view) {
            with(cargoTrackingTimeLineResoource) {
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
    }

    private fun setExtraData(view: View, position: Int) {
        with (view) {
            with(datas[position]) {
                tv_cargo_status.text = context.getString(status.id)
                tv_cargo_port_code.text = portCode
                tv_cargo_port_name.text = portName
                val timeSymbol = getCargoTrackingTimeSymbol(context, position, this)
                tv_cargo_date_time_symbol.visibility = if (timeSymbol.isEmpty()) View.GONE else View.VISIBLE
                tv_cargo_date_time_symbol.text = timeSymbol
                tv_cargo_date_time.text = dateTime
                if (vesselStatus == VESSEL_NOT_READY) {
                    tv_cargo_port_code.setTextColor(context.getColor(R.color.color_bfbfbf))
                    tv_cargo_date_time_symbol.setTextColor(context.getColor(R.color.color_bfbfbf))
                    tv_cargo_date_time.setTextColor(context.getColor(R.color.color_bfbfbf))
                } else {
                    tv_cargo_port_code.setTextColor(context.getColor(R.color.color_4c4c4c))
                    tv_cargo_date_time_symbol.setTextColor(context.getColor(R.color.color_4c4c4c))
                    tv_cargo_date_time.setTextColor(context.getColor(R.color.color_4c4c4c))
                }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}