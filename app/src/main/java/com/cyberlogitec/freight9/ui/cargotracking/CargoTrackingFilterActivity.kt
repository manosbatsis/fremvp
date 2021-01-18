package com.cyberlogitec.freight9.ui.cargotracking

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_ALL
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_POD_ETA
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_POL_ETD
import com.cyberlogitec.freight9.config.CargoTrackingFilterDatePeriod.FILTER_DATE_PERIOD_1M
import com.cyberlogitec.freight9.config.CargoTrackingFilterDatePeriod.FILTER_DATE_PERIOD_3M
import com.cyberlogitec.freight9.config.CargoTrackingFilterDatePeriod.FILTER_DATE_PERIOD_6M
import com.cyberlogitec.freight9.config.CargoTrackingFilterDatePeriod.FILTER_DATE_PERIOD_CUSTOM
import com.cyberlogitec.freight9.config.CargoTrackingFilterDateType.FILTER_DATE_END
import com.cyberlogitec.freight9.config.CargoTrackingFilterDateType.FILTER_DATE_START
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_DATE
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_ROUTE
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_TOP
import com.cyberlogitec.freight9.config.CargoTrackingFilterSpinType.FILTER_SPIN_POD
import com.cyberlogitec.freight9.config.CargoTrackingFilterSpinType.FILTER_SPIN_POL
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingFilter
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.datepicker.date.DatePickerDialogFragment
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.getEngShortMonth
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_cargo_tracking_filter.*
import kotlinx.android.synthetic.main.body_cargo_tracking_filter.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import org.joda.time.DateTime
import timber.log.Timber
import java.util.*


@RequiresActivityViewModel(value = CargoTrackingFilterViewModel::class)
class CargoTrackingFilterActivity : BaseActivity<CargoTrackingFilterViewModel>() {

    private var cargoTrackingFilter: CargoTrackingFilter = CargoTrackingFilter()
    private lateinit var tfRegular: Typeface
    private lateinit var tfExtraBold: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_cargo_tracking_filter)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    private fun initData() {
        ResourcesCompat.getFont(this, R.font.opensans_regular)?.let{ tfRegular = it }
        ResourcesCompat.getFont(this, R.font.opensans_extrabold)?.let { tfExtraBold = it }
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_common,
                menuType = MenuType.CROSS,
                title = getString(R.string.cargo_tracking_filters),
                isEnableNavi = false)

        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { intent ->
                        cargoTrackingFilter = intent.getSerializableExtra(Intents.CARGO_TRACKING_FILTER)
                                as CargoTrackingFilter
                        Handler().postDelayed({
                            var yPosition = 0
                            when(cargoTrackingFilter.scrollType) {
                                FILTER_SCROLL_TO_TOP -> {
                                    yPosition = 0
                                }
                                FILTER_SCROLL_TO_ROUTE -> {
                                    yPosition = tv_title_route.top
                                }
                                FILTER_SCROLL_TO_DATE -> {
                                    yPosition = tv_title_date.top
                                }
                            }
                            sv_cargo_tracking_filter_root.smoothScrollTo(0, yPosition)

                        },100)
                        setUi()
                    }
                }

        viewModel.outPuts.onClickToClose()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        onBackPressed()
                    }
                }

        viewModel.outPuts.onClickToSpinDialog()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { spinType ->
                        showSpinPolPodDialog(spinType)
                    }
                }

        viewModel.outPuts.onClickToSpinCalendarDialog()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { spinType ->
                        // show when only Custom
                        if (cargoTrackingFilter.datePeriodType == FILTER_DATE_PERIOD_CUSTOM) {
                            showSpinCalendarDialog(spinType)
                        }
                    }
                }

        viewModel.outPuts.onClickToDateType()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { dateType ->
                        setClickFilterDateType(dateType)
                    }
                }

        viewModel.outPuts.onClickToDatePeriodType()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { datePeriodType ->
                        setClickFilterDatePeriodType(datePeriodType)
                    }
                }

        viewModel.outPuts.onClickToApply()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        val intent = Intent()
                        intent.putExtra(Intents.CARGO_TRACKING_FILTER, getFilterData())
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
    }

    private fun setListener() {

        // title "X" button
        toolbar_right_btn.setSafeOnClickListener {
            viewModel.inPuts.clickToClose(Parameter.CLICK)
        }

        // APPLY button
        btn_apply.setSafeOnClickListener {
            viewModel.inPuts.clickToApply(Parameter.CLICK)
        }

        /*
         * ROUTE
         */
        // Route : POL - Spin Control (ALL POLs item 포함)
        iv_route_pol.setSafeOnClickListener {
            viewModel.inPuts.clickToSpinDialog(FILTER_SPIN_POL)
        }

        // Route : POD - Spin Control (ALL PODs item 포함)
        iv_route_pod.setSafeOnClickListener {
            viewModel.inPuts.clickToSpinDialog(FILTER_SPIN_POD)
        }

        /*
         * DATE
         */
        // Date : ALL
        tv_date_all.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_ALL)
        }

        // Date : POL ETD
        tv_date_pol_etd.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_POL_ETD)
        }

        // Date : POD ETA
        tv_date_pod_eta.setOnClickListener {
            viewModel.inPuts.clickToDateType(FILTER_DATE_POD_ETA)
        }

        // Date : 1M
        tv_date_perioa_1m.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_1M)
        }

        // Date : 3M
        tv_date_period_3m.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_3M)
        }

        // Date : 6M
        tv_date_period_6m.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_6M)
        }

        // Date : Custom
        tv_date_period_custom.setOnClickListener {
            viewModel.inPuts.clickToDatePeriodType(FILTER_DATE_PERIOD_CUSTOM)
        }

        // Date : Start
        tv_date_period_start.setOnClickListener {
            viewModel.inPuts.clickToSpinCalendarDialog(FILTER_DATE_START)
        }

        // Date : End
        tv_date_period_end.setOnClickListener {
            viewModel.inPuts.clickToSpinCalendarDialog(FILTER_DATE_END)
        }
    }

    private fun setUi() {
        setRoute()
        setDate()
    }

    private fun setRoute(filter: CargoTrackingFilter = cargoTrackingFilter) {
        with(filter) {
            val polCode = if (routePolCode.isEmpty()) {
                getString(R.string.booking_dashboard_filters_route_pol_all)
            } else {
                routePolCode
            }
            tv_route_pol_selected.text = polCode
            if (routePolList.isNotEmpty()) {
                tv_route_polname_selected.text = routePolList.first { it.code == polCode }.name
            }

            val podCode = if (routePodCode.isEmpty()) {
                getString(R.string.booking_dashboard_filters_route_pod_all)
            } else {
                routePodCode
            }
            tv_route_pod_selected.text = podCode
            if (routePodList.isNotEmpty()) {
                tv_route_podname_selected.text = routePodList.first { it.code == podCode }.name
            }
        }
    }

    private fun setDate(filter: CargoTrackingFilter = cargoTrackingFilter) {
        setClickFilterDateType(filter.dateType)

        val calendar = Calendar.getInstance()
        with(filter) {
            var yearStarts= dateStarts.year
            var monthStarts = dateStarts.month
            var dayStarts = dateStarts.day
            if (yearStarts < 1 || monthStarts < 1 || dayStarts < 1) {
                yearStarts = calendar.get(Calendar.YEAR)
                monthStarts = calendar.get(Calendar.MONTH) + 1
                dayStarts = calendar.get(Calendar.DAY_OF_MONTH)
                dateStarts.year = yearStarts
                dateStarts.month = monthStarts
                dateStarts.day = dayStarts
            }
            tv_date_period_start.text = "${getEngShortMonth(monthStarts)} $dayStarts, $yearStarts"

            var yearEnds = dateEnds.year
            var monthEnds = dateEnds.month
            var dayEnds = dateEnds.day
            if (yearEnds < 1 || monthEnds < 1 || dayEnds < 1) {
                yearEnds = calendar.get(Calendar.YEAR)
                monthEnds = calendar.get(Calendar.MONTH) + 1
                dayEnds = calendar.get(Calendar.DAY_OF_MONTH)
                dateEnds.year = yearEnds
                dateEnds.month = monthEnds
                dateEnds.day = dayEnds
            }
            tv_date_period_end.text = "${getEngShortMonth(monthEnds)} $dayEnds, $yearEnds"
        }
        setClickFilterDatePeriodType(filter.datePeriodType)
    }

    private fun getFilterData(): CargoTrackingFilter {
        with(cargoTrackingFilter) {

            val selectedPolCode = if (routePolList.isNotEmpty()) {
                routePolList.first { it.isSelected }.code
            } else {
                EmptyString
            }
            routePolCode = if (selectedPolCode == getString(R.string.booking_dashboard_filters_route_pol_all)
                    || selectedPolCode.isEmpty()) {
                EmptyString
            } else {
                selectedPolCode
            }

            val selectedPodCode = if (routePodList.isNotEmpty()) {
                routePodList.first { it.isSelected }.code
            } else {
                EmptyString
            }
            routePodCode = if (selectedPodCode == getString(R.string.booking_dashboard_filters_route_pod_all)
                    || selectedPodCode.isEmpty()) {
                EmptyString
            } else {
                selectedPodCode
            }
        }
        return cargoTrackingFilter
    }

    private fun setClickFilterDateType(filterDateType: Int = FILTER_DATE_ALL) {
        val normalTextColor = getColor(R.color.greyish_brown)
        val selectedTextColor = getColor(R.color.white)
        val normalTextTypeface = tfRegular
        val selectedTextTypeface = tfExtraBold

        tv_date_all.typeface = if (filterDateType == FILTER_DATE_ALL) selectedTextTypeface else normalTextTypeface
        tv_date_all.setTextColor(if (filterDateType == FILTER_DATE_ALL) selectedTextColor else normalTextColor)
        if (filterDateType == FILTER_DATE_ALL) {
            tv_date_all.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_left_top_bottom)
        } else {
            tv_date_all.setBackgroundColor(getColor(R.color.white))
        }

        tv_date_pol_etd.typeface = if (filterDateType == FILTER_DATE_POL_ETD) selectedTextTypeface else normalTextTypeface
        tv_date_pol_etd.setTextColor(if (filterDateType == FILTER_DATE_POL_ETD) selectedTextColor else normalTextColor)
        tv_date_pol_etd.setBackgroundColor(getColor(if (filterDateType == FILTER_DATE_POL_ETD) R.color.greyish_brown else R.color.white))

        tv_date_pod_eta.typeface = if (filterDateType == FILTER_DATE_POD_ETA) selectedTextTypeface else normalTextTypeface
        tv_date_pod_eta.setTextColor(if (filterDateType == FILTER_DATE_POD_ETA) selectedTextColor else normalTextColor)
        if (filterDateType == FILTER_DATE_POD_ETA) {
            tv_date_pod_eta.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_right_top_bottom)
        } else {
            tv_date_pod_eta.setBackgroundColor(getColor(R.color.white))
        }

        cargoTrackingFilter.dateType = filterDateType
    }

    private fun setClickFilterDatePeriodType(filterDatePeriodType: Int = FILTER_DATE_PERIOD_CUSTOM) {
        val normalTextColor = getColor(R.color.greyish_brown)
        val selectedTextColor = getColor(R.color.white)
        val normalTextTypeface = tfRegular
        val selectedTextTypeface = tfExtraBold

        tv_date_perioa_1m.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_1M) selectedTextTypeface else normalTextTypeface
        tv_date_perioa_1m.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_1M) selectedTextColor else normalTextColor)
        if (filterDatePeriodType == FILTER_DATE_PERIOD_1M) {
            tv_date_perioa_1m.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_left_top)
        } else {
            tv_date_perioa_1m.setBackgroundColor(getColor(R.color.white))
        }

        tv_date_period_3m.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_3M) selectedTextTypeface else normalTextTypeface
        tv_date_period_3m.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_3M) selectedTextColor else normalTextColor)
        tv_date_period_3m.setBackgroundColor(getColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_3M) R.color.greyish_brown else R.color.white))

        tv_date_period_6m.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_6M) selectedTextTypeface else normalTextTypeface
        tv_date_period_6m.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_6M) selectedTextColor else normalTextColor)
        tv_date_period_6m.setBackgroundColor(getColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_6M) R.color.greyish_brown else R.color.white))

        tv_date_period_custom.typeface = if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) selectedTextTypeface else normalTextTypeface
        tv_date_period_custom.setTextColor(if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) selectedTextColor else normalTextColor)
        if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) {
            tv_date_period_custom.background = getDrawable(R.drawable.bg_round_corner_booking_filter_greyish_right_top)
        } else {
            tv_date_period_custom.setBackgroundColor(getColor(R.color.white))
        }

        cargoTrackingFilter.datePeriodType = filterDatePeriodType
        setDatePeriodDuration(filterDatePeriodType)
    }

    private fun setDatePeriodDuration(filterDatePeriodType: Int) {
        // Set today ~ 1M, 3M, 6M and disable Picker
        with(cargoTrackingFilter) {
            val startYear = dateStarts.year
            val startMonth = dateStarts.month
            val startDay = dateStarts.day

            val calendar = Calendar.getInstance()
            val startDateTime = DateTime(startYear, startMonth, startDay, 0, 0, 0, 0)
            calendar.time = startDateTime.toDate()

            when(filterDatePeriodType) {
                FILTER_DATE_PERIOD_1M -> {
                    calendar.add(Calendar.MONTH, 1)
                }
                FILTER_DATE_PERIOD_3M -> {
                    calendar.add(Calendar.MONTH, 3)
                }
                FILTER_DATE_PERIOD_6M -> {
                    calendar.add(Calendar.MONTH, 6)
                }
                else -> {  }
            }

            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH) + 1
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            if (filterDatePeriodType == FILTER_DATE_PERIOD_CUSTOM) {
                year = dateEnds.year
                month = dateEnds.month
                day = dateEnds.day
            }

            val selectedDotDate = "${getEngShortMonth(month)} $day, $year"
            tv_date_period_end.text = selectedDotDate
            dateEnds = CargoTrackingFilter.Date(month, day, year)
        }
    }

    /**
     * TODO
     * pair.first  : FILTER_DATE_START or FILTER_DATE_END
     * pair.second : Date set to Spin Calendar
     */
    private fun showSpinCalendarDialog(spinType: Int) {
        val datePickerDialogFragment = DatePickerDialogFragment()

        with(cargoTrackingFilter) {
            val year = if (spinType == FILTER_DATE_START) {
                dateStarts.year
            } else {
                dateEnds.year
            }

            val month = if (spinType == FILTER_DATE_START) {
                dateStarts.month
            } else {
                dateEnds.month
            }

            val day = if (spinType == FILTER_DATE_START) {
                dateStarts.day
            } else {
                dateEnds.day
            }

            if (year > 0 && month > 0 && day > 0) {
                datePickerDialogFragment.setSelectedDate(year, month, day)
            }
            datePickerDialogFragment.setOnDateChooseListener(object : DatePickerDialogFragment.OnDateChooseListener {
                override fun onDateChoose(year: Int, month: Int, day: Int) {
                    val selectedDotDate = "${getEngShortMonth(month)} $day, $year"
                    if (spinType == FILTER_DATE_START) {
                        tv_date_period_start.text = selectedDotDate
                        dateStarts = CargoTrackingFilter.Date(month, day, year)
                    } else {
                        tv_date_period_end.text = selectedDotDate
                        dateEnds = CargoTrackingFilter.Date(month, day, year)
                    }
                }
            })
        }
        datePickerDialogFragment.show(this.supportFragmentManager, "DatePickerDialogFragment")
    }

    private fun showSpinPolPodDialog(spinType: Int = FILTER_SPIN_POL) {

        val spinDataList = mutableListOf<TextItem>()
        if (spinType == FILTER_SPIN_POL) {
            for ((index, routePolData) in cargoTrackingFilter.routePolList.withIndex()) {
                if (index == 0) {
                    routePolData.code = getString(R.string.booking_dashboard_filters_route_pol_all)
                }
                spinDataList.add(TextItem(routePolData.code + " " + routePolData.name, routePolData.isSelected, index))
            }
        } else {
            for ((index, routePodData) in cargoTrackingFilter.routePodList.withIndex()) {
                if (index == 0) {
                    routePodData.code = getString(R.string.booking_dashboard_filters_route_pod_all)
                }
                spinDataList.add(TextItem(routePodData.code + " " + routePodData.name, routePodData.isSelected, index))
            }
        }

        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(this)

        dialog.setCancelable(true)
        dialog.setContentView(view)

        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                if (spinType == FILTER_SPIN_POL) {
                    cargoTrackingFilter.routePolList.map { it.isSelected = false }
                    cargoTrackingFilter.routePolList[index].isSelected = true
                } else {
                    cargoTrackingFilter.routePodList.map { it.isSelected = false }
                    cargoTrackingFilter.routePodList[index].isSelected = true
                }
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            if (spinType == FILTER_SPIN_POL) {
                tv_route_pol_selected.text = cargoTrackingFilter.routePolList.first { it.isSelected }.code
                tv_route_polname_selected.text = cargoTrackingFilter.routePolList.first { it.isSelected }.name
            } else {
                tv_route_pod_selected.text = cargoTrackingFilter.routePodList.first { it.isSelected }.code
                tv_route_podname_selected.text = cargoTrackingFilter.routePodList.first { it.isSelected }.name
            }
        }

        view.picker.setItems(spinDataList)
        view.picker.index = if (spinType == FILTER_SPIN_POL) {
            cargoTrackingFilter.routePolList.indexOf(cargoTrackingFilter.routePolList.first { it.isSelected })
        } else {
            cargoTrackingFilter.routePodList.indexOf(cargoTrackingFilter.routePodList.first { it.isSelected })
        }

        dialog.show()
    }
}