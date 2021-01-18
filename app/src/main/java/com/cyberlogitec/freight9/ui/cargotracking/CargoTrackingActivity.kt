package com.cyberlogitec.freight9.ui.cargotracking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_ALL
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_POD_ETA
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate.FILTER_DATE_POL_ETD
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_DATE
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_ROUTE
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType.FILTER_SCROLL_TO_TOP
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.MenuItem
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingFilter
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingItem
import com.cyberlogitec.freight9.lib.model.cargotracking.CargoTrackingSelectValue
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import com.cyberlogitec.freight9.lib.ui.enums.CargoTrackingNumberType
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.lib.util.Intents.Companion.CARGO_TRACKING_DATA
import com.cyberlogitec.freight9.lib.util.Intents.Companion.CARGO_TRACKING_ENTRY_OTHER
import com.cyberlogitec.freight9.lib.util.ViewExpandCollapseAnimationUtils
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingScheduleItem.setScheduleItem
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_cargo_tracking.*
import kotlinx.android.synthetic.main.body_cargo_tracking.*
import kotlinx.android.synthetic.main.item_cargo_tracking.view.*
import kotlinx.android.synthetic.main.item_filter_horizontal.view.*
import kotlinx.android.synthetic.main.item_cargo_tracking_schedule.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_cargo_tracking.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

@RequiresActivityViewModel(value = CargoTrackingViewModel::class)
class CargoTrackingActivity : BaseActivity<CargoTrackingViewModel>() {

    private var cargoTrackingFilter: CargoTrackingFilter = CargoTrackingFilter()
    private var cargoTrackingNumberTypes: MutableList<CargoTrackingNumberType> = mutableListOf()
    private var cargoTrackingItemData: MutableList<CargoTrackingItem> = mutableListOf()

    private val cargoTrackingAdapter by lazy {
        CargoTrackingAdapter()
                .apply {
                    onClickItem = { position ->
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_INDEX, position))
                    }
                    onClickArrow = { position, isExpanded ->
                        clickViewParameterAny(Pair(ParameterAny.ANY_ITEM_OBJECT, Pair(position, isExpanded)))
                    }
                    onClickButton = {

                    }
                }
    }

    private val cargoTrackingFilterAdapter by lazy {
        CargoTrackingFilterAdapter()
                .apply {
                    onClickItem = { moveType ->
                        cargoTrackingFilter.scrollType = moveType
                        clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_GO_FILTER, cargoTrackingFilter))
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_cargo_tracking)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun setRxOutputs() {
        viewModel.outputs.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        ab_cargo_tracking.setExpanded(true)
                        recycler_cargo_tracking_list.scrollToPosition(0)
                        setCargoTrackingDummyData()
                    }
                }

        viewModel.onClickViewParameterClick
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterClick ->
                        when(parameterClick) {
                            // show main menu
                            ParameterClick.CLICK_TITLE_RIGHT_BTN -> {
                                startMenuActivity(MenuItem.MENUITEM_CARGO_TRACKING,
                                        MenuActivity::class.java)
                            }
                            // init search area
                            ParameterClick.CLICK_SEARCH_INIT -> {
                                searchFilter(EmptyString)
                            }
                            // show number type picker
                            ParameterClick.CLICK_SEARCH_TYPE -> {
                                showNumberTypeDialog()
                            }
                            // show search input popup
                            ParameterClick.CLICK_SEARCH_POPUP -> {
                                ab_cargo_tracking.setExpanded(true)
                                recycler_cargo_tracking_list.scrollToPosition(0)
                                Handler().postDelayed({
                                    showSearchPopup(::onSearchWordInput)
                                }, 50)
                            }
                            else -> {  }
                        }
                    }
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                // go Tracking detail
                                ParameterAny.ANY_ITEM_INDEX -> {
                                    val index = second as Int
                                    startActivity(Intent(this@CargoTrackingActivity,
                                            CargoTrackingDetailActivity::class.java)
                                            .putExtra(CARGO_TRACKING_ENTRY_OTHER, false)
                                            .putExtra(CARGO_TRACKING_DATA, ArrayList(getCargoTrackingDetailData())))
                                }
                                ParameterAny.ANY_ITEM_OBJECT -> {
                                    val pair = second as Pair<Int, Boolean>
                                    cargoTrackingAdapter.datas[pair.first].isExpanded = pair.second
                                    cargoTrackingAdapter.notifyDataSetChanged()
                                }
                                // Filter datas
                                ParameterAny.ANY_SEARCH_FILTER -> {
                                    val filterData = second as CargoTrackingFilter
                                    doProcessFilter(filterData)
                                }
                                // go cargo tracking filter activity
                                ParameterAny.ANY_SEARCH_GO_FILTER -> {
                                    val filterData = second as CargoTrackingFilter
                                    RxActivityResult(this@CargoTrackingActivity)
                                            .start(Intent(this@CargoTrackingActivity, CargoTrackingFilterActivity::class.java)
                                                    .putExtra(Intents.CARGO_TRACKING_FILTER, filterData)
                                            )
                                            .subscribe(
                                                    { result ->
                                                        if (result.isOk) {
                                                            cargoTrackingFilter = result.data.getSerializableExtra(Intents.CARGO_TRACKING_FILTER)
                                                                    as CargoTrackingFilter
                                                            viewModel.clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_FILTER, cargoTrackingFilter))
                                                            Timber.d("f9: OK --> data:  ${cargoTrackingFilter.toJson()}")
                                                        } else {
                                                            Timber.d("f9: NotOK --> resultCode:  ${result.resultCode}")
                                                        }
                                                    },
                                                    {
                                                        viewModel.error.onNext(it)
                                                    }
                                            )
                                }
                                else -> {  }
                            }
                        }
                    }
                }
    }

    private fun initData() {
        setDummyFilterData()
        addCargoTrackingNumberType()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(toolbar_cargo_tracking,
                menuType = MenuType.DEFAULT,
                title = getString(R.string.menu_cargo_tracking),
                isEnableNavi = false)

        recyclerViewInit()
        searchInit()
        setFilterDataToHorizontalList()
        setListener()
    }

    private fun recyclerViewInit() {
        recycler_cargo_tracking_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@CargoTrackingActivity.cargoTrackingAdapter
        }

        recycler_cargo_tracking_filter_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
            adapter = this@CargoTrackingActivity.cargoTrackingFilterAdapter
        }
    }

    private fun setListener() {
        //------------------------------------------------------------------------------------------
        // on click toolbar right button
        toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            clickViewParameterClick(ParameterClick.CLICK_TITLE_RIGHT_BTN)
        }

        // on click filter image
        iv_cargo_tracking_filter.setSafeOnClickListener {
            cargoTrackingFilter.scrollType = FILTER_SCROLL_TO_TOP
            clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_GO_FILTER, cargoTrackingFilter))
        }

        // on click search word textivew
        tv_cargo_tracking_search.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clickViewParameterClick(ParameterClick.CLICK_SEARCH_POPUP)
                v.performClick()
            }
            false
        }

        // on click search word clear
        iv_search_clear.setSafeOnClickListener {
            searchUiInit()
        }

        // on click number category
        ll_cargo_tracking_no.setSafeOnClickListener {
            clickViewParameterClick(ParameterClick.CLICK_SEARCH_TYPE)
        }

        recycler_cargo_tracking_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1)) {
                    /* TOP of List */
                } else if (!recyclerView.canScrollVertically(1)){
                    /* End of List */
                } else {
                    /* Idle */
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val diffYPosition = dx - dy
                if (diffYPosition > 0) {
                    // Scroll Down
                    if (!isAppBarExpanded()) {
                        ab_cargo_tracking.setExpanded(true)
                    }
                } else {
                    /* Scroll Up */
                }
            }
        })

        recycler_cargo_tracking_filter_list.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val isTopOfList = !recyclerView.canScrollHorizontally(-1)
                view_left_gradation.visibility = if (isTopOfList) View.GONE else View.VISIBLE
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) { }
        })
    }

    private fun isAppBarExpanded() = (ab_cargo_tracking.height - ab_cargo_tracking.bottom) == 0

    private fun searchFilter(searchWord: String) {
        cargoTrackingFilter.numberValue = searchWord
        viewModel.clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_FILTER, cargoTrackingFilter))
    }

    private fun searchUiInit() {
        viewModel.clickViewParameterClick(ParameterClick.CLICK_SEARCH_INIT)
    }

    private fun searchInit() {
        // init search number type & input area
        tv_cargo_tracking_no.text = getString(cargoTrackingFilter.numberType.id)
        searchUiInit()
    }

    private fun onSearchWordInput(searchWord: String) {
        searchFilter(searchWord)
    }

    private fun setFilterDataToHorizontalList(filterData: CargoTrackingFilter = cargoTrackingFilter) {
        /*
        * All Route / CNSHA - USLAX
        * All Date  / Aug 13, 2020 - Aug 13, 2020
        */
        cargoTrackingFilterAdapter.itemList.clear()

        with(filterData) {

            val allPols = routePolCode.isEmpty() || routePolCode == getString(R.string.booking_dashboard_filters_route_pol_all)
            val allPods = routePodCode.isEmpty() || routePodCode == getString(R.string.booking_dashboard_filters_route_pod_all)
            val firstRouteFilter = if (allPols && allPods) {
                getString(R.string.cargo_tracking_filters_all_routes)
            } else if (allPols && !allPods) {
                getString(R.string.booking_dashboard_filters_route_pol_all) + " - " + routePodCode
            } else if (!allPols && allPods) {
                routePolCode + " - " + getString(R.string.booking_dashboard_filters_route_pod_all)
            } else {
                "$routePolCode - $routePodCode"
            }

            cargoTrackingFilterAdapter.itemList.add(CargoTrackingSelectValue(
                    0,
                    FILTER_SCROLL_TO_ROUTE,
                    firstRouteFilter)
            )

            /*
             * TODO : Server 로 부터 받은 List 에서 POL, POD 모두에 대해서...
             *  StartDate, EndDate 를 추출한 후 dateStarts, dateEnds 에 set 해주어야 한다
             */
            val prefix: String
            var secondDateFilter = EmptyString
            when(dateType) {
                FILTER_DATE_ALL -> {
                    if ( dateStarts.year < 1 || dateStarts.month < 1 || dateStarts.day < 1) {
                        prefix = getString(R.string.cargo_tracking_filters_all_date)
                    } else {
                        prefix = getString(R.string.cargo_tracking_filters_all)
                        secondDateFilter = "${getEngShortMonth(dateStarts.month)} ${dateStarts.day}, ${dateStarts.year}" +
                                " - " + "${getEngShortMonth(dateEnds.month)} ${dateEnds.day}, ${dateEnds.year}"
                    }
                }
                FILTER_DATE_POL_ETD, FILTER_DATE_POD_ETA -> {
                    prefix =  if (dateType == FILTER_DATE_POL_ETD) {
                        getString(R.string.cargo_tracking_time_symbol_etd)
                    } else {
                        getString(R.string.cargo_tracking_time_symbol_eta)
                    }
                    secondDateFilter = "${getEngShortMonth(dateStarts.month)} ${dateStarts.day}, ${dateStarts.year}" +
                            " - " + "${getEngShortMonth(dateEnds.month)} ${dateEnds.day}, ${dateEnds.year}"
                }
                else -> {
                    prefix = EmptyString
                }
            }

            cargoTrackingFilterAdapter.itemList.add(CargoTrackingSelectValue(
                    1,
                    FILTER_SCROLL_TO_DATE,
                    prefix,
                    secondDateFilter)
            )
        }
        cargoTrackingFilterAdapter.notifyDataSetChanged()
    }

    private fun doProcessFilter(filterData: CargoTrackingFilter) {

        // set search input number to UI
        tv_cargo_tracking_search.text = filterData.numberValue
        iv_search_clear.visibility = if (filterData.numberValue.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // set extra filter date to UI
        setFilterDataToHorizontalList(filterData)

        showToast("doProcessFilter...\n" + "search text : ${getString(filterData.numberType.id)} - ${filterData.numberValue}")

        /*
        * TODO
        * 1. List 에서  cargoTrackingFilter 조건으로 filtering
        * 2. cargoTrackingAdapter.setData(filtered datas)
        * 3. cargoTrackingAdapter.notifiDatasetChanged
        */
    }

    private fun showSearchPopup(onSearchWordInput: ((String) -> Unit)) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_search_common, null)
        val popupWindow = CargoTrackingSearchPopup(
                view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true,
                tv_cargo_tracking_search.text.toString(),
                onSearchWordInput
        )
        popupWindow.showAtLocation(view, Gravity.TOP, 0, 0 )
    }

    private fun addCargoTrackingNumberType() {
        cargoTrackingNumberTypes.add(CargoTrackingNumberType.TYPE_BOOKING_NO)
        cargoTrackingNumberTypes.add(CargoTrackingNumberType.TYPE_CONTAINER_NO)
        cargoTrackingNumberTypes.add(CargoTrackingNumberType.TYPE_BL_NO)
        cargoTrackingNumberTypes.add(CargoTrackingNumberType.TYPE_VESSEL_NO)
        cargoTrackingNumberTypes.add(CargoTrackingNumberType.TYPE_CONSIGNEE_NAME)
        cargoTrackingNumberTypes.add(CargoTrackingNumberType.TYPE_SHIPPER_NAME)
    }

    private fun showNumberTypeDialog() {
        val spinDataList = mutableListOf<TextItem>()
        for (numberType in cargoTrackingNumberTypes) {
            spinDataList.add(TextItem(getString(numberType.id),
                    numberType.code == cargoTrackingFilter.numberType.code,
                    numberType.code))
        }

        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(this)

        dialog.setCancelable(true)
        dialog.setContentView(view)

        var selectedNumberType: CargoTrackingNumberType = CargoTrackingNumberType.TYPE_BL_NO
        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                selectedNumberType = cargoTrackingNumberTypes
                        .find { it.code == spinDataList[index]._index } ?: CargoTrackingNumberType.TYPE_BL_NO
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            cargoTrackingFilter.numberType = selectedNumberType
            cargoTrackingFilter.numberValue = EmptyString
            searchInit()
        }

        view.picker.setItems(spinDataList)
        view.picker.index = spinDataList.first { it._isSelected }._index
        dialog.show()
    }

    private fun setCargoTrackingDummyData() {
        if (cargoTrackingItemData.isEmpty()) {
            cargoTrackingItemData = getCargoTrackingItemData()
        }
        cargoTrackingAdapter.datas = cargoTrackingItemData
        cargoTrackingAdapter.allDatas = cargoTrackingItemData
        cargoTrackingAdapter.notifyDataSetChanged()
    }

    private fun setDummyFilterData() {
        with(cargoTrackingFilter) {
            if (routePolList.isEmpty()) {
                // 첫번째는 빈값 (All POLs)
                routePolList.add(CargoTrackingFilter.Route(
                        code = getString(R.string.booking_dashboard_filters_route_pol_all),
                        isSelected = true)
                )
                routePolList.add(CargoTrackingFilter.Route("KRPUS", "Pusan"))
                routePolList.add(CargoTrackingFilter.Route("CNTAO", "Qingdao, Shandong"))
                routePolList.add(CargoTrackingFilter.Route("CNSHA", "Shanghai, Shanghai"))
                routePolList.add(CargoTrackingFilter.Route("CNNGB", "Ningbo, Zhejiang"))
                routePolList.add(CargoTrackingFilter.Route("TWKHH", "Kaohsiung City"))
            }

            if (routePodList.isEmpty()) {
                // 첫번째는 빈값 (All PODs)
                routePodList.add(CargoTrackingFilter.Route(
                        code = getString(R.string.booking_dashboard_filters_route_pod_all),
                        isSelected = true)
                )
                routePodList.add(CargoTrackingFilter.Route("CNSHA", "Shanghai, Shanghai"))
                routePodList.add(CargoTrackingFilter.Route("CNSHK", "Shekou, Guangdong"))
                routePodList.add(CargoTrackingFilter.Route("SGSIN", "Singapore"))
                routePodList.add(CargoTrackingFilter.Route("SAJED", "jeddah"))
                routePodList.add(CargoTrackingFilter.Route("CNYTN", "Yantian, Guangdong"))
            }

            val calendar = Calendar.getInstance()
            dateStarts = CargoTrackingFilter.Date(calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.YEAR))
            dateEnds = CargoTrackingFilter.Date(2, 28, 2021)
        }
    }

    /***********************************************************************************************
     * RecyclerAdapter
     */
    private class CargoTrackingAdapter : RecyclerView.Adapter<CargoTrackingAdapter.ViewHolder>() {

        lateinit var context: Context

        var datas = mutableListOf<CargoTrackingItem>()
        var allDatas = mutableListOf<CargoTrackingItem>()

        var onClickItem: (Int) -> Unit = { _: Int -> }
        var onClickArrow: (Int, Boolean) -> Unit = { _: Int, _ -> }
        var onClickButton: (Boolean) -> Unit = { _: Boolean -> }

        fun setData(datas: MutableList<CargoTrackingItem>) {
            this.datas.clear()
            this.datas = datas
            this.allDatas.clear()
            this.allDatas = datas
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_cargo_tracking, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                val itemData = datas[position]
                with(itemData) {
                    tv_booking_no.text = context.getString(R.string.cargo_tracking_title_booking_no_short) +
                            " " + bookingNo
                    iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                    tv_carrier_name.text = carrierCode
                    tv_vvd_digit.text = vvdDigitCode

                    setScheduleItem(context, item_cargo_tracking_schedule1, 0, statusDetailList)
                    setScheduleItem(context, item_cargo_tracking_schedule2, 1, statusDetailList)
                    setScheduleItem(context, item_cargo_tracking_schedule3, 2, statusDetailList)
                    setScheduleItem(context, item_cargo_tracking_schedule4, 3, statusDetailList)

                    // TODO
                    cargo_tracking_item_booking_no.text = if (bookingNo.isEmpty()) "-" else bookingNo
                    cargo_tracking_item_bl_no.text = if (blNo.isEmpty()) "-" else blNo
                    cargo_tracking_item_hbl_no.text = if (hblNo.isEmpty()) "-" else hblNo
                    cargo_tracking_item_mbl_no.text = if (mblNo.isEmpty()) "-" else mblNo
                    cargo_tracking_item_container_no.text = if (containerNo.isEmpty()) "-" else containerNo
                    cargo_tracking_item_consignee.text = if (consigneeName.isEmpty()) "-" else consigneeName
                    cargo_tracking_item_shipper.text = if (shipperName.isEmpty()) "-" else shipperName
                }

                setExpandUi(this, position, itemData.isExpanded)

                iv_arrow.setOnClickListener {
                    onClickArrow(position, !itemData.isExpanded)
                }

                setOnClickListener {
                    onClickItem(position)
                }
            }
        }

        private fun setExpandUi(view: View, position: Int, isCollapsed: Boolean) {
            view.iv_arrow.setImageResource(if (isCollapsed) R.drawable.btn_collapse_default_l else R.drawable.btn_expand_default_l)

            if (isCollapsed) {
                ViewExpandCollapseAnimationUtils.expand(view.ll_expand_content)
            } else {
                ViewExpandCollapseAnimationUtils.collapse(view.ll_expand_content)
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

    private class CargoTrackingFilterAdapter : RecyclerView.Adapter<CargoTrackingFilterAdapter.ViewHolder>() {

        var itemList = mutableListOf<CargoTrackingSelectValue>()
        var onClickItem: (Int) -> Unit = { _: Int -> }

        override fun getItemCount() = itemList.count()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
                ViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_filter_horizontal, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {

                setItemMargin(this, position)

                with(itemList[position]) {
                    tv_filter_value_duration.visibility = if (detail.isEmpty()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    tv_filter_value.text = value
                    // ALL From ~ To, ETD From ~ To, ETA From ~ To
                    tv_filter_value_duration.text = "  $detail"

                    setOnClickListener {
                        onClickItem(moveType)
                    }
                }
            }
        }

        private fun setItemMargin(view: View, position: Int) {
            var marginLeft = 16.toDp().toInt()
            if (position == 0) {
                marginLeft = 0.toDp().toInt()
            }
            with(view) {
                val llParams = ll_item_filter.layoutParams as LinearLayout.LayoutParams
                llParams.leftMargin = marginLeft
                ll_item_filter.layoutParams = llParams
            }
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}