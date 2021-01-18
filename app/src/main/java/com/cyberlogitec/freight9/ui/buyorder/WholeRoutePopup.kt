package com.cyberlogitec.freight9.ui.buyorder

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.model.BorList
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.ui.route.RouteGridView
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.toDp
import kotlinx.android.synthetic.main.popup_order_whole_route.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class WholeRoutePopup(var view: View, width: Int, height: Int, focusable: Boolean) :
        PopupWindow(view, width, height, focusable) {

    private var tabList = mutableListOf<TabEnum>()
    private var laneList = mutableListOf<Lane>()
    private var routeDataList: RouteDataList = RouteDataList()
    private var borList: BorList? = BorList()

    private var currentLaneIndex = 0
//    private var wholeRoutes = mutableListOf<WholeRoute>()
//    private var isTableView = true
    private var isGridView = false
    private var isTabRemove = true  // For MVP

    init {
        view.sv_whole_route_tab_horizontal.visibility = if (isTabRemove) View.GONE else View.VISIBLE

        view.iv_order_whole_route_close.setSafeOnClickListener {
            dismiss()
        }

        tabList.add(TabEnum.TAB_TABLE)
        tabList.add(TabEnum.TAB_GRID)
        makeLaneDataDummy()
        makeGridDataDummy()
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0)

    }

    fun initValue(routeDataList: RouteDataList? = null, borList: BorList? = null) {

        if (routeDataList == null) {
            makeWholeRouteData()

            // 상단 Tab 생성
            makeTabList()

        } else {
            this.routeDataList = routeDataList
            this.borList = borList

            // 상단 Tab 생성
            makeTabList()

        }
    }

    //----------------------------------------------------------------------------------------------

    private fun makeTabList() {
        if (!isTabRemove) {
            for (tab in tabList) {
                val llItem = LinearLayout(view.context)
                llItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                llItem.orientation = LinearLayout.VERTICAL
                llItem.gravity = Gravity.BOTTOM
                val margin = 12.toDp().toInt()
                val params = llItem.layoutParams as LinearLayout.LayoutParams
                params.setMargins(margin, 0, margin, 0)
                llItem.layoutParams = params

                llItem.addView(makeTabTextView(view.context.getString(tab.nameId)))
                llItem.addView(makeTabImageView())
                llItem.tag = tab.tagSeq

                llItem.setOnClickListener { clickTabProcess(llItem, tab) }
                view.ll_whole_route_tab_horizontal.addView(llItem)
            }
        }
        // 첫번째 Tab 초기화
        clickTabProcess(null, if (isTabRemove) tabList[1] else tabList[0])
    }

    private fun clickTabProcess(clicklayout: LinearLayout?, tab: TabEnum) {

        val clickTagid = if (null == clicklayout) layout_tab_tag_seq else clicklayout.tag as Int

        // 선택한 tab highlight, 그 외 tab dark
        makeSelectedTabList(tab)

        for (index in 0 until view.ll_whole_route_tab_horizontal.childCount) {
            val childview = view.ll_whole_route_tab_horizontal.getChildAt(index) as LinearLayout

            var textcolorvalue = R.color.greyish_brown
            var viewcolorvalue = R.color.black
            if (childview.tag as Int == clickTagid) {
                textcolorvalue = R.color.colorWhite
                viewcolorvalue = R.color.purpley_blue
            }

            val subChildCount = childview.childCount
            for (subIndex in 0 until subChildCount) {
                when (val subChildView = childview.getChildAt(subIndex)) {
                    is TextView -> {
                        subChildView.setTextColor(ContextCompat.getColor(view.context, textcolorvalue))
                    }
                    is View -> {
                        subChildView.setBackgroundColor(view.context.getColor(viewcolorvalue))
                    }
                }
            }
        }
    }

    private fun makeTabTextView(title: String): TextView {
        val textview = TextView(view.context)
        textview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        val params = textview.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        params.setMargins(0, 0, 0, 4.toDp().toInt())
        textview.layoutParams = params
        textview.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textview.setTextAppearance(R.style.txt_opensans_b_16_greyishbrown)
        textview.text = title
        return textview
    }

    private fun makeTabImageView(): View {
        val imageview = View(view.context)
        imageview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4.toDp().toInt())
        imageview.setBackgroundColor(view.context.getColor(R.color.black))
        return imageview
    }

    // Table, Grid tab 누른 경우
    private fun makeSelectedTabList(tab: TabEnum) {
        // TODO : 선택한 tab 에 대한 UI, Data setting
        if (tab.index == 0) {
            // Table
            view.sv_whole_route_table_lane_horizontal.visibility = View.VISIBLE
            view.sv_whole_route_tab_table.visibility = View.VISIBLE
            view.ll_whole_route_tab_grid.visibility = View.GONE
            makeLaneTabList()
        } else if (tab.index == 1) {
            // Grid
            view.sv_whole_route_table_lane_horizontal.visibility = View.GONE
            view.sv_whole_route_tab_table.visibility = View.GONE
            view.ll_whole_route_tab_grid.visibility = View.VISIBLE
            makeGridView()
        }
    }

    //----------------------------------------------------------------------------------------------

    // TODO : 선택한 lane tab 에 대한 UI, Data setting
    private fun makeLaneView(laneDatas: List<LaneData>) {
        view.ll_whole_route_tab_table_row_list.removeAllViews()
        for (laneData in laneDatas) {
            view.ll_whole_route_tab_table_row_list.addView(makeLaneRow(laneData))
        }
    }

    private fun makeLaneRow(laneData: LaneData): LinearLayout {

        // Row
        val llRow = LinearLayout(view.context)
        val height = 40.toDp().toInt()
        llRow.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        val rowParams = llRow.layoutParams as LinearLayout.LayoutParams
        val leftRightMargin = if (laneData.isOcean or laneData.isSearched) 0 else 16.toDp().toInt()
        rowParams.setMargins(leftRightMargin, 0.toDp().toInt(), leftRightMargin, 16.toDp().toInt())
        val leftRightPadding = if (laneData.isOcean or laneData.isSearched) 16.toDp().toInt() else 0
        llRow.setPadding(leftRightPadding, 0, leftRightPadding, 0)
        llRow.layoutParams = rowParams
        llRow.orientation = LinearLayout.HORIZONTAL
        llRow.gravity = Gravity.CENTER_VERTICAL

        if (laneData.isOcean) {
            llRow.setBackgroundResource(R.color.pale_gray)
            val textview = TextView(view.context)
            textview.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val params = textview.layoutParams as LinearLayout.LayoutParams
            textview.layoutParams = params
            textview.gravity = Gravity.CENTER
            textview.setTextAppearance(R.style.txt_opensans_r_13_verylightpink)
            textview.text = view.context.getString(R.string.buy_order_whole_route_ocean_area)
            llRow.addView(textview)
        } else {
            llRow.setBackgroundResource(if (laneData.isSearched) R.color.color_f2f2f2 else R.color.colorWhite)

            // Port : code + name
            val llPort = LinearLayout(view.context)
            llPort.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            val portParams = llPort.layoutParams as LinearLayout.LayoutParams
            portParams.weight = 1.2f
            llPort.layoutParams = portParams
            llPort.orientation = LinearLayout.VERTICAL
            llPort.gravity = Gravity.START or Gravity.CENTER_VERTICAL

            // code
            val textViewCode = TextView(view.context)
            textViewCode.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            var params = textViewCode.layoutParams as LinearLayout.LayoutParams
            textViewCode.layoutParams = params
            textViewCode.gravity = Gravity.START
            textViewCode.includeFontPadding = false
            textViewCode.setTextAppearance(R.style.txt_opensans_eb_13_greyishbrown)
            textViewCode.text = laneData.portCode
            llPort.addView(textViewCode)

            // name
            val textviewName = TextView(view.context)
            textviewName.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params = textviewName.layoutParams as LinearLayout.LayoutParams
            textviewName.layoutParams = params
            textviewName.gravity = Gravity.START
            textviewName.includeFontPadding = false
            textviewName.setTextAppearance(R.style.txt_opensans_r_13_greyishbrown)
            textviewName.text = laneData.portName
            llPort.addView(textviewName)
            llRow.addView(llPort)

            // Arrival
            val textviewArrival = TextView(view.context)
            textviewArrival.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            params = textviewArrival.layoutParams as LinearLayout.LayoutParams
            params.weight = 0.83f
            textviewArrival.layoutParams = params
            textviewArrival.gravity = Gravity.CENTER
            textviewArrival.setTextAppearance(R.style.txt_opensans_r_13_greyishbrown)
            textviewArrival.text = calcArrivalDepartureElapsed(true, laneData)
            llRow.addView(textviewArrival)

            // Departure
            val textviewDeparture = TextView(view.context)
            textviewDeparture.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            params = textviewDeparture.layoutParams as LinearLayout.LayoutParams
            params.weight = 1.0f
            textviewDeparture.layoutParams = params
            textviewDeparture.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            textviewDeparture.setTextAppearance(R.style.txt_opensans_r_13_greyishbrown)
            textviewDeparture.text = calcArrivalDepartureElapsed(false, laneData)
            llRow.addView(textviewDeparture)
        }
        return llRow
    }

    private fun makeGridView() {
        if (isGridView()) return
        view.gv_whole_route_grid_view.resetView()   //재사용시 view 초기화 시켜줘야함
        view.gv_whole_route_grid_view.mPol = borList?.locPolCd ?: "CNSHAL"
        view.gv_whole_route_grid_view.mPod = borList?.locPodCd ?: "CNSHAD"
        view.gv_whole_route_grid_view.mViewType = RouteGridView.GridViewType.SELL_OFFER
        view.gv_whole_route_grid_view.setData(routeDataList)
        setGridView(true)
    }

    private fun isGridView(): Boolean {
        return isGridView
    }

    private fun setGridView(isGridView: Boolean) {
        this.isGridView = isGridView
    }

    /**
     * TODO : DateFormat 이 결정되면 로직 수정 필요함
     */
    private fun calcArrivalDepartureElapsed(isArrival: Boolean, laneData: LaneData): String {
        var elapsed = "-"
        if (isArrival) {
            // 현재 index 부터 뒤쪽으로 추적하여
            // 처음으로 나타나는 departure 또는 arrival 이 있으면 그 시간과 gap을 체크한다.
            if (!laneData.arrival.isEmpty()) {
                // 첫번째는 Departure 만 있음
                if (laneData.index == 0) {
                    /*
                     * TODO : "-"
                    */
                } else {
                    // "2019-11-01 09:30"
                    val year = laneData.arrival.substring(0, 4)
                    val month = laneData.arrival.substring(5, 7)
                    val date = laneData.arrival.substring(8, 10)
                    val hour = laneData.arrival.substring(11, 13)
                    val minute = laneData.arrival.substring(14, 16)

                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, year.toInt())
                    cal.set(Calendar.MONTH, month.toInt() - 1)
                    cal.set(Calendar.DATE, date.toInt())
                    cal.set(Calendar.HOUR, hour.toInt())
                    cal.set(Calendar.MINUTE, minute.toInt())

                    val calDiff = Calendar.getInstance()
                    for (n in laneData.index - 1 downTo 0) {
                        with(laneList[currentLaneIndex].laneDatas[n]) {
                            if (!departure.isEmpty()) {
                                val dYear = departure.substring(0, 4)
                                val dMonth = departure.substring(5, 7)
                                val dDate = departure.substring(8, 10)
                                val dHour = departure.substring(11, 13)
                                val dMinute = departure.substring(14, 16)
                                calDiff.set(Calendar.YEAR, dYear.toInt())
                                calDiff.set(Calendar.MONTH, dMonth.toInt() - 1)
                                calDiff.set(Calendar.DATE, dDate.toInt())
                                calDiff.set(Calendar.HOUR, dHour.toInt())
                                calDiff.set(Calendar.MINUTE, dMinute.toInt())
                                val diffInMillis = cal.timeInMillis - calDiff.timeInMillis
                                val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                                return "D+$days $hour:$minute"
                            } else if (!arrival.isEmpty()) {
                                val aYear = arrival.substring(0, 4)
                                val aMonth = arrival.substring(5, 7)
                                val aDate = arrival.substring(8, 10)
                                val aHour = arrival.substring(11, 13)
                                val aMinute = arrival.substring(14, 16)
                                calDiff.set(Calendar.YEAR, aYear.toInt())
                                calDiff.set(Calendar.MONTH, aMonth.toInt() - 1)
                                calDiff.set(Calendar.DATE, aDate.toInt())
                                calDiff.set(Calendar.HOUR, aHour.toInt())
                                calDiff.set(Calendar.MINUTE, aMinute.toInt())
                                val diffInMillis = cal.timeInMillis - calDiff.timeInMillis
                                val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                                return "D+$days $hour:$minute"
                            }
                        }
                    }
                }
            }
        } else {
            if (!laneData.departure.isEmpty()) {
                // "2019-11-01 09:30"
                val year = laneData.departure.substring(0, 4)
                val month = laneData.departure.substring(5, 7)
                val date = laneData.departure.substring(8, 10)
                val hour = laneData.departure.substring(11, 13)
                val minute = laneData.departure.substring(14, 16)

                // Jun.18 HH:MM
                if (laneData.index == 0) {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, year.toInt())
                    cal.set(Calendar.MONTH, month.toInt() - 1)
                    cal.set(Calendar.DATE, date.toInt())
                    cal.set(Calendar.HOUR, hour.toInt())
                    cal.set(Calendar.MINUTE, minute.toInt())
                    elapsed = SimpleDateFormat("MMM.dd", Locale.ENGLISH).format(cal.time)
                    elapsed += (" $hour:$minute")
                }
                // D+2 HH:MM
                else {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, year.toInt())
                    cal.set(Calendar.MONTH, month.toInt() - 1)
                    cal.set(Calendar.DATE, date.toInt())
                    cal.set(Calendar.HOUR, hour.toInt())
                    cal.set(Calendar.MINUTE, minute.toInt())

                    //elapsed = "D+2 09:30"
                    val calDiff = Calendar.getInstance()

                    // laneData 에 arrival 이 있으면 departure - arrival
                    if (!laneData.arrival.isEmpty()) {
                        val aYear = laneData.arrival.substring(0, 4)
                        val aMonth = laneData.arrival.substring(5, 7)
                        val aDate = laneData.arrival.substring(8, 10)
                        val aHour = laneData.arrival.substring(11, 13)
                        val aMinute = laneData.arrival.substring(14, 16)
                        calDiff.set(Calendar.YEAR, aYear.toInt())
                        calDiff.set(Calendar.MONTH, aMonth.toInt() - 1)
                        calDiff.set(Calendar.DATE, aDate.toInt())
                        calDiff.set(Calendar.HOUR, aHour.toInt())
                        calDiff.set(Calendar.MINUTE, aMinute.toInt())
                        val diffInMillis = cal.timeInMillis - calDiff.timeInMillis
                        val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                        return "D+$days $hour:$minute"
                    }
                    // laneData 에 arrival 이 없으면 이전 index 의 departure 부터 ~
                    else {
                        for (n in laneData.index - 1 downTo 0) {
                            with(laneList[currentLaneIndex].laneDatas[n]) {
                                if (!arrival.isEmpty()) {
                                    val aYear = arrival.substring(0, 4)
                                    val aMonth = arrival.substring(5, 7)
                                    val aDate = arrival.substring(8, 10)
                                    val aHour = arrival.substring(11, 13)
                                    val aMinute = arrival.substring(14, 16)
                                    calDiff.set(Calendar.YEAR, aYear.toInt())
                                    calDiff.set(Calendar.MONTH, aMonth.toInt() - 1)
                                    calDiff.set(Calendar.DATE, aDate.toInt())
                                    calDiff.set(Calendar.HOUR, aHour.toInt())
                                    calDiff.set(Calendar.MINUTE, aMinute.toInt())
                                    val diffInMillis = cal.timeInMillis - calDiff.timeInMillis
                                    val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                                    return "D+$days $hour:$minute"
                                } else if (!departure.isEmpty()) {
                                    val dYear = departure.substring(0, 4)
                                    val dMonth = departure.substring(5, 7)
                                    val dDate = departure.substring(8, 10)
                                    val dHour = departure.substring(11, 13)
                                    val dMinute = departure.substring(14, 16)
                                    calDiff.set(Calendar.YEAR, dYear.toInt())
                                    calDiff.set(Calendar.MONTH, dMonth.toInt() - 1)
                                    calDiff.set(Calendar.DATE, dDate.toInt())
                                    calDiff.set(Calendar.HOUR, dHour.toInt())
                                    calDiff.set(Calendar.MINUTE, dMinute.toInt())
                                    val diffInMillis = cal.timeInMillis - calDiff.timeInMillis
                                    val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
                                    return "D+$days $hour:$minute"
                                }
                            }
                        }
                    }
                }
            }
        }
        return elapsed
    }

    //----------------------------------------------------------------------------------------------

    private fun makeLaneTabList() {
        view.sv_whole_route_table_lane_horizontal.visibility = if (laneList.size > 1) View.VISIBLE else View.GONE
        view.ll_whole_route_table_lane_horizontal.removeAllViews()
        if (laneList.size > 1) {
            for (lane in laneList) {
                val llItem = LinearLayout(view.context)
                llItem.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                llItem.orientation = LinearLayout.VERTICAL
                llItem.gravity = Gravity.BOTTOM
                val margin = 12.toDp().toInt()
                val params = llItem.layoutParams as LinearLayout.LayoutParams
                params.setMargins(margin, 0, margin, 0)
                llItem.layoutParams = params

                llItem.addView(makeTabLaneTextView(lane.name))
                llItem.addView(makeTabLaneImageView())
                llItem.tag = lane.tagSeq

                llItem.setOnClickListener { clickTabLaneProcess(llItem, lane) }
                view.ll_whole_route_table_lane_horizontal.addView(llItem)
            }
        }
        // 첫번째 Tab 초기화
        clickTabLaneProcess(null, laneList[0])
    }

    private fun clickTabLaneProcess(clicklayout: LinearLayout?, lane: Lane) {

        val clickTagId = if (null == clicklayout) layout_lane_tag_seq else clicklayout.tag as Int

        // 선택한 tab highlight, 그 외 tab dark
        makeSelectedTabLaneList(lane)

        val childCount = view.ll_whole_route_table_lane_horizontal.childCount

        for (index in 0 until childCount) {
            val childView = view.ll_whole_route_table_lane_horizontal.getChildAt(index) as LinearLayout

            var textAppearance = R.style.txt_opensans_r_14_greyishbrown
            var viewColorValue = R.color.pale_gray
            if (childView.tag as Int == clickTagId) {
                textAppearance = R.style.txt_opensans_b_13_1a1a1a
                viewColorValue = R.color.blue_violet
            }

            val subChildCount = childView.childCount
            for (subIndex in 0 until subChildCount) {
                when (val subChildView = childView.getChildAt(subIndex)) {
                    is TextView -> {
                        subChildView.setTextAppearance(textAppearance)
                    }
                    is View -> {
                        subChildView.setBackgroundColor(view.context.getColor(viewColorValue))
                    }
                }
            }
        }
    }

    private fun makeTabLaneTextView(title: String): TextView {
        val textView = TextView(view.context)
        textView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        val params = textView.layoutParams as LinearLayout.LayoutParams
        params.weight = 1.0f
        params.setMargins(0, 0, 0, 2.toDp().toInt())
        textView.layoutParams = params
        textView.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        textView.setTextAppearance(R.style.txt_opensans_r_13_greyishbrown)
        textView.text = title
        return textView
    }

    private fun makeTabLaneImageView(): View {
        val imageView = View(view.context)
        imageView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4.toDp().toInt())
        imageView.setBackgroundColor(view.context.getColor(R.color.pale_gray))
        return imageView
    }

    // Lane tab 누른 경우
    private fun makeSelectedTabLaneList(lane: Lane) {
        currentLaneIndex = lane.index
        makeLaneView(lane.laneDatas)
    }

    //----------------------------------------------------------------------------------------------

    private fun makeWholeRouteData() {

    }

    private fun makeLaneDataDummy() {
        for (x in 0..4) {
            val laneDatas = mutableListOf<LaneData>()
            laneDatas.add(LaneData(0, false, false, "CNSHA1", "Shanghai, China", "", "2019-11-01 09:30"))
            laneDatas.add(LaneData(1, false, true, "CNSHA2", "Shanghai, China", "2019-11-05 09:30", "2019-11-07 09:30"))
            laneDatas.add(LaneData(2, false, false, "CNSHA", "Shanghai, China", "2019-11-10 09:30", "2019-11-13 09:30"))
            laneDatas.add(LaneData(3, false, false, "CNCAN", "Guangzhou, China", "", ""))
            laneDatas.add(LaneData(4, false, false, "CNHKG", "Hongkong, China", "2019-11-15 09:30", "2019-11-18 09:30"))
            laneDatas.add(LaneData(5, true, false, "", "", "", ""))
            laneDatas.add(LaneData(6, false, false, "USLAX", "Los Angeles, US", "2019-11-20 09:30", "2019-11-22 09:30"))
            laneDatas.add(LaneData(7, false, false, "USLAX", "Los Angeles, US", "", ""))
            laneDatas.add(LaneData(8, false, true, "USLAX", "Los Angeles, US", "2019-11-23 09:30", "2019-11-25 09:30"))
            laneDatas.add(LaneData(9, false, false, "USLAX", "Los Angeles, US", "", ""))
            laneDatas.add(LaneData(10, false, false, "USLAX", "Los Angeles, US", "2019-11-23 09:30", "2019-11-25 09:30"))
            val lane = Lane(x, "Lane" + (x + 1), layout_lane_tag_seq + x, laneDatas)
            laneList.add(lane)
        }
    }

    private fun makeGridDataDummy() {
        routeDataList.add(RouteData("CNATX", "CNATX", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL1", "DEL1"))
        routeDataList.add(RouteData("CNBA1", "CNBA1", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL2", "DEL2"))
        routeDataList.add(RouteData("CNBOT", "CNBOT", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL3", "DEL3"))
        routeDataList.add(RouteData("CNBSD", "CNBSD", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL11", "DEL11"))
        routeDataList.add(RouteData("CNBTN", "CNBTN", "CNSHAL", "CNSHAL", "CNSHAD", "CNSHAD", "DEL12", "DEL12"))

        routeDataList.add(RouteData("CNBA1", "CNBA1", "CNSHAL1", "CNSHAL1", "CNSHAD", "CNSHAD", "DEL2", "DEL2"))
        routeDataList.add(RouteData("CNBA1", "CNBA1", "CNSHAL1", "CNSHAL1", "CNSHAD", "CNSHAD", "DEL3", "DEL3"))

        routeDataList.add(RouteData("CNSH1", "CNSH1", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL12", "DEL12"))
        routeDataList.add(RouteData("CNSH1", "CNSH1", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL13", "DEL13"))
        routeDataList.add(RouteData("CNSH2", "CNSH2", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL14", "DEL14"))
        routeDataList.add(RouteData("CNSH3", "CNSH3", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL15", "DEL15"))
        routeDataList.add(RouteData("CNSH4", "CNSH4", "CNSHAL", "CNSHAL", "CNSHAD2", "CNSHAD2", "DEL16", "DEL16"))
    }

    //----------------------------------------------------------------------------------------------

    enum class TabEnum constructor(
            val index: Int,
            val nameId: Int,
            val tagSeq: Int
    ) {
        TAB_TABLE(0, R.string.buy_order_whole_route_table, layout_tab_tag_seq),
        TAB_GRID(1, R.string.buy_order_whole_route_grid, layout_tab_tag_seq + 1),
    }

    companion object {
        const val layout_tab_tag_seq = 1000000
        const val layout_lane_tag_seq = 2000000
    }

    data class Lane(
            var index: Int,
            var name: String,
            var tagSeq: Int,
            var laneDatas: List<LaneData>
    )

    data class LaneData(
            var index: Int,
            var isOcean: Boolean,
            var isSearched: Boolean,
            var portCode: String,
            var portName: String,
            var arrival: String,
            var departure: String
    )
}

