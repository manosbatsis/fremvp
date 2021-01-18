package com.cyberlogitec.freight9.ui.booking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.CargoTrackingFilterDate
import com.cyberlogitec.freight9.config.CargoTrackingFilterMoveType
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.FilterDate.FILTER_DATE_ALL
import com.cyberlogitec.freight9.config.FilterDate.FILTER_DATE_POD_ETA
import com.cyberlogitec.freight9.config.FilterDate.FILTER_DATE_POL_ETD
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_BOOKING_DASHBOARD
import com.cyberlogitec.freight9.lib.model.booking.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.ui.enums.BookingDashboardNumberType
import com.cyberlogitec.freight9.lib.ui.swipe.SwipeableRecyclerViewTouchListener
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.cargotracking.CargoTrackingSearchPopup
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_booking_dashboard.*
import kotlinx.android.synthetic.main.appbar_dashboard.*
import kotlinx.android.synthetic.main.body_booking_dashboard.*
import kotlinx.android.synthetic.main.item_booking_dashboard.view.*
import kotlinx.android.synthetic.main.item_filter_horizontal.view.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@RequiresActivityViewModel(value = BookingDashboardViewModel::class)
class BookingDashboardActivity : BaseActivity<BookingDashboardViewModel>(),
        SwipeableRecyclerViewTouchListener.SwipeListener{

    private var popupWindow: PopupWindow? = null
    private var bookingFilter: BookingFilter = BookingFilter()
    private var bookingDashboardNumberTypes: MutableList<BookingDashboardNumberType> = mutableListOf()
    private var bookingList: MutableList<BookingDashboardItem> = mutableListOf()

    private val adapter:RecyclerAdapter by lazy {
        RecyclerAdapter()
                .apply {
                    onClickItem = {
                        showDetail(it)
                    }
                }
    }

    private fun showDetail(it: BookingDashboardItem) {

        startActivity(Intent(this, BookingDashboardDetailActivity::class.java).putExtra("bookingitem", it))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_booking_dashboard)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onDestroy() {


        super.onDestroy()
    }

    override fun onBackPressed() {
        if (popupWindow != null) {
            removePopup()
        }
        else {
            super.onBackPressed()
        }
    }

    private fun removePopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
            popupWindow = null
        }
    }

    private fun setRxOutputs() {
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startMenuActivity(MENUITEM_BOOKING_DASHBOARD, MenuActivity::class.java)
                }

        viewModel.onClickViewParameterAny
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { parameterPair ->
                        with(parameterPair) {
                            when (first) {
                                // Filter datas
                                ParameterAny.ANY_SEARCH_FILTER -> {
                                    val filterData = second as BookingFilter
                                    showToast("search text : ${getString(filterData.numberType.id)} - ${filterData.numberValue}")
                                    tv_booking_search.text = filterData.numberValue
                                    iv_booking_search_clear.visibility = if (filterData.numberValue.isNotEmpty()) {
                                        View.VISIBLE
                                    } else {
                                        View.GONE
                                    }

                                    doProcessFilter(filterData)
                                }
                                // go cargo tracking filter activity
                                ParameterAny.ANY_SEARCH_GO_FILTER -> {
                                    val filterData = second as BookingFilter
                                    RxActivityResult(this@BookingDashboardActivity)
                                            .start(Intent(this@BookingDashboardActivity, BookingDashboardFilterActivity::class.java)
                                                    .putExtra(Intents.BOOKING_DASHBOARD_FILTER, filterData)
                                            )
                                            .subscribe(
                                                    { result ->
                                                        if (result.isOk) {
                                                            bookingFilter = result.data.getSerializableExtra(Intents.BOOKING_DASHBOARD_FILTER)
                                                                    as BookingFilter
                                                            viewModel.clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_FILTER, bookingFilter))
                                                            Timber.d("f9: OK --> data:  ${bookingFilter.toJson()}")
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
        addBookingDashboardNumberType()
        initFilter()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        defaultbarInit(toolbar_booking_dashboard, menuType = MenuType.DEFAULT, title = getString(R.string.booking_dashboard),isEnableNavi = false)

        recyclerViewInit()
        searchInit()
        setListener()
        initFragment()
        requestBookings()
    }

    private fun initFragment() {

        //Attach Viewpager to tablayout
        var pageList: ArrayList<String> = ArrayList()
        pageList.add("1")
        pageList.add("2")
        vp_swipe.adapter = ViewPagerAdapter(pageList,this)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let { unselectedTab ->
                    ResourcesCompat.getFont(unselectedTab.parent!!.context, R.font.opensans_regular).also {
                        it?.let { setTabTitleTypeface(unselectedTab.position, it) }
                    }
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { selectedTab ->
                    ResourcesCompat.getFont(selectedTab.parent!!.context, R.font.opensans_extrabold).also {
                        it?.let { setTabTitleTypeface(selectedTab.position, it) }
                    }
                }
            }
        })

        TabLayoutMediator(tabLayout, vp_swipe, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            (vp_swipe.adapter as ViewPagerAdapter).also {
                tab.text = it.pageList[position]
            }
        }).attach()

        //Tab Width 변경
        for (i in 0..1) {
            val layout = ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
            val params = layout.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = params
        }
    }

    private fun requestBookings() {

        viewModel.inPuts.requestBookingDashboardList(Parameter.EVENT)

    }

    private fun recyclerViewInit() {

        recycler_filter_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
            adapter = this@BookingDashboardActivity.filterAdapter
        }

        recycler_booking_list.apply {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = this@BookingDashboardActivity.adapter
        }
        recycler_booking_list.addOnItemTouchListener(
                SwipeableRecyclerViewTouchListener(recycler_booking_list, this@BookingDashboardActivity)
        )

    }

    private fun setListener() {
        toolbar_booking_dashboard.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            viewModel.inPuts.clickToMenu(Parameter.CLICK)
        }

        viewModel.outPuts.onSuccessRequestBookingDashboardList()
                .bindToLifecycle(this)
                .subscribe {

                    bookingList.clear()
                    bookingList = it

                    adapter.datas.clear()
                    adapter.datas.addAll(it)
                    adapter.notifyDataSetChanged()
                    setPolsPods(it)
                }

        ll_booking_no.setSafeOnClickListener {
            showNumberTypeDialog()
        }
        /**
         * call filter activity
         */
        iv_filter.setSafeOnClickListener {
            // scroll to top
            bookingFilter.scrollType = CargoTrackingFilterMoveType.FILTER_SCROLL_TO_TOP
            clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_GO_FILTER, bookingFilter))

        }
        tv_booking_search.setSafeOnClickListener {
            Handler().postDelayed({
                showSearchPopup(::onSearchWordInput)
            }, 50)
        }

        iv_booking_search_clear.setSafeOnClickListener {
            searchFilter(EmptyString)
            showSearchPopup (::onSearchWordInput)
        }

        iv_arror_back.setSafeOnClickListener {
            ll_swipe_holder.visibility = View.INVISIBLE
        }

    }

    private fun initFilter() {
        with(bookingFilter) {
            isStatusBookingDraft = true
            isStatusBookingConfirmed = true
            isStatusPolArrived = true
            isStatusBdr = true
            isStatusPodArrived = true

            if (routePolList.isEmpty()) {
                routePolList.add(BookingFilter.Route(
                        code = getString(R.string.booking_dashboard_filters_route_pol_all),
                        isSelected = true)
                )
            }
            if (routePodList.isEmpty()) {
                routePodList.add(BookingFilter.Route(
                        code = getString(R.string.booking_dashboard_filters_route_pod_all),
                        isSelected = true)
                )
            }
        }
    }

    fun setTabTitleTypeface(position: Int, type: Typeface){
        val tabLayout : LinearLayout = ((tabLayout.getChildAt(0) as ViewGroup)).getChildAt(position) as LinearLayout
        val tabTextView: TextView = tabLayout.getChildAt(1) as TextView
        tabTextView.typeface = type
    }

    private fun setPolsPods(data: BookingDashboard?) {

        with(bookingFilter) {
            routePolList.clear()
            // 첫번째는 빈값 (All POLs)
            routePolList.add(BookingFilter.Route(
                    code = getString(R.string.booking_dashboard_filters_route_pol_all),
                    isSelected = true)
            )
            val polpoddata = data?.filter { it.booking != null && it.booking.bookingLocation != null }
            for(item in polpoddata?.distinctBy { it.booking.bookingLocation.portOfReceiptLocationCode  }!!) {

                routePolList.add(BookingFilter.Route(item.booking.bookingLocation.portOfReceiptLocationCode, item.booking.bookingLocation.portOfReceiptLocationName))
            }

            routePodList.clear()
            // 첫번째는 빈값 (All PODs)
            routePodList.add(BookingFilter.Route(
                    code = getString(R.string.booking_dashboard_filters_route_pod_all),
                    isSelected = true)
            )
            for(item in polpoddata?.distinctBy { it.booking.bookingLocation.deliveryLocationCode  }!!) {

                routePodList.add(BookingFilter.Route(item.booking.bookingLocation.deliveryLocationCode, item.booking.bookingLocation.deliveryLocationName))
            }

            val calendar = Calendar.getInstance()
            dateStarts = BookingFilter.Date(calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.YEAR))
            dateEnds = BookingFilter.Date(2, 28, 2021)
        }
    }

    //recycler adapter
    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        val datas = mutableListOf<BookingDashboardItem>()
        var viewPeroidMode : Boolean = false

        var onClickItem: (BookingDashboardItem) -> Unit = {}

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        init {
            currencyFormat.minimumFractionDigits = 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            currencyFormat.minimumFractionDigits = 0
            currencyFormat.maximumFractionDigits = 0
            return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_booking_dashboard, parent, false))
        }

        override fun getItemCount(): Int = datas.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.itemView) {
                tv_pol_status.visibility = View.VISIBLE
                val data = datas[position]
                val party = data.party
                if(party != null) {
                    tv_carrier_name.text = context.getCarrierCodeToF9(party.carrierInfo.partyInfo.partyCode)
                    iv_carrier_logo.setImageResource(context.getCarrierCodeToF9(party.carrierInfo.partyInfo.partyCode).getCarrierIcon())
                }

                data?.header?.messageTypeIdentifier?.let {
                    var transport: Transport? = null

                   // ------ 임시수정 message type에 따라서 구분되어야함
                        /*when(data?.header?.messageTypeIdentifier) {
                        "bookingRequest","bookingConfirmation" -> { transport = data?.transport?.let { data?.transport?.firstOrNull{ it.transportStageCode == TransportStageCode.TRANSPORT_STAGE_ON }}
                        }
                        "ShippingInstruction" -> {transport = data?.transport?.let { data?.transport?.firstOrNull() }}
                        else -> {transport = data?.transport?.let { data?.transport?.firstOrNull() }}
                    }*/
                        transport = data?.transport?.let { data?.transport?.firstOrNull() }
                   //----------------------

                    tv_vvd.text = transport?.vesselName ?: ""
                    tv_pol_cd.text = transport?.transportLocation?.portOfLoadingLocationCode ?: ""
                    tv_pol_name.text = transport?.transportLocation?.portOfLoadingLocationName ?: ""

                    tv_pod_cd.text = transport?.transportLocation?.portOfDischargeLocationCode ?: ""
                    tv_pod_name.text = transport?.transportLocation?.portOfDischargeLocationName ?: ""

                    val df: DateFormat = SimpleDateFormat("yy-MM-dd HH:mm")

                    runCatching { tv_etd_time.text = " '${df.format(transport?.transportDate?.estimatedDepartureDate?.toDate("yyyyMMdd"))}" }
                            .onFailure { tv_etd_time.text = " -" }
                    runCatching { tv_eta_time.text = " '${df.format(transport?.transportDate?.estimatedArrivalDate?.toDate("yyyyMMdd"))}" }
                            .onFailure { tv_eta_time.text = " -" }

                }

                tv_booking_no.text = data.booking?.messageNumber ?: "-"
                tv_bl_no.text = data.booking?.bookingReferenceNo?.billOfLadingNo ?: "-"
                val container = data.container.firstOrNull()
                var containerCount: Int
                containerCount = data?.container.size
                if(containerCount > 0)
                    tv_container_no.text = "${container?.containerNo} ${if(containerCount > 1)"+${containerCount -1}" else ""}"
                else
                    tv_container_no.text = "-"

                tv_consignee.text = data.party?.consigneeInfo?.partyInfo?.partyName ?: {"-"} ()

                tv_shipper.text = data.party?.consignorInfo?.partyInfo?.partyName ?: {"-"} ()

                if (data.isExpand){
                    ll_expand.visibility = View.VISIBLE
                    iv_expand.setImageDrawable(getDrawable(context, R.drawable.btn_collapse_default_l))
                } else {
                    ll_expand.visibility = View.GONE
                    iv_expand.setImageDrawable(getDrawable(context, R.drawable.btn_expand_default_l))
                }

                iv_expand.setOnClickListener {
                    if(ll_expand.visibility==View.VISIBLE) {
                        ll_expand.visibility = View.GONE
                        iv_expand.setImageDrawable(getDrawable(context, R.drawable.btn_expand_default_l))
                    } else {
                        ll_expand.visibility = View.VISIBLE
                        iv_expand.setImageDrawable(getDrawable(context, R.drawable.btn_collapse_default_l))
                    }

                    data.isExpand = !data.isExpand
                }
                setSafeOnClickListener {
                    onClickItem(data)
                }
            }

        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


    }
    private fun doProcessFilter(filterData: BookingFilter) {

        setFilterData(filterData)
        var searchList: MutableList<BookingDashboardItem> = mutableListOf()
        searchList.addAll(bookingList)
        if(!filterData.numberValue.isNullOrBlank()) {
            when(filterData.numberType) {
                BookingDashboardNumberType.TYPE_BOOKING_NO -> { searchList = searchList.filter{
                    it.booking?.messageNumber?.contains(filterData.numberValue, true) == true }
                        .toMutableList()
                }
                BookingDashboardNumberType.TYPE_BL_NO -> { searchList = searchList.filter {
                    it.booking?.bookingReferenceNo?.billOfLadingNo?.contains(filterData.numberValue, true) == true}
                        .toMutableList()
                }
                BookingDashboardNumberType.TYPE_VESSEL_NO -> { searchList = searchList.filter {
                    containVessel(it.transport, filterData.numberValue) }
                        .toMutableList()
                }
                BookingDashboardNumberType.TYPE_CONTAINER_NO -> {searchList = searchList.filter {
                    containContainer(it.container, filterData.numberValue)}
                        .toMutableList()
                }
                BookingDashboardNumberType.TYPE_CONSIGNEE_NAME -> { searchList = searchList.filter {
                    it.party.consigneeInfo.partyInfo.partyName.contains(filterData.numberValue, true) == true }
                        .toMutableList()
                }

            }

        }
        //filter status ??


        //-------------

        if(!filterData.routePolCode.isNullOrEmpty()){
            searchList = searchList.filter { it.booking?.bookingLocation?.portOfReceiptLocationCode?.equals(filterData.routePolCode) == true}.toMutableList()
        }
        if(!filterData.routePodCode.isNullOrEmpty()){
            searchList = searchList.filter { it.booking?.bookingLocation?.deliveryLocationCode?.equals(filterData.routePodCode) == true}.toMutableList()
        }

        when(filterData.dateType) {
            FILTER_DATE_ALL -> {}
            FILTER_DATE_POL_ETD -> {}
            FILTER_DATE_POD_ETA -> {}
        }

        adapter.datas.clear()
        adapter.datas.addAll(searchList)
        adapter.notifyDataSetChanged()


    }

    private fun containContainer(container: List<Container>, numberValue: String): Boolean {
        var isContain = false
        container.forEach { if(it?.containerNo?.contains(numberValue, true) == true) isContain = true}
        return isContain
    }

    private fun containVessel(vessel: List<Transport>, numberValue: String): Boolean {
        var isContain = false
        vessel.forEach { if(it?.vesselName?.contains(numberValue, true) == true) isContain = true}
        return isContain
    }

    private fun onSearchWordInput(searchWord: String) {
        searchFilter(searchWord)
    }

    private fun showSearchPopup(onSearchWordInput: ((String) -> Unit)) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_search_common, null)
        val popupWindow = CargoTrackingSearchPopup(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true,tv_booking_search.text.toString(), onSearchWordInput)
        popupWindow.showAtLocation(view, Gravity.TOP, 0, 0 )
    }

    private fun addBookingDashboardNumberType() {
        bookingDashboardNumberTypes.add(BookingDashboardNumberType.TYPE_BOOKING_NO)
        bookingDashboardNumberTypes.add(BookingDashboardNumberType.TYPE_BL_NO)
        bookingDashboardNumberTypes.add(BookingDashboardNumberType.TYPE_VESSEL_NO)
        bookingDashboardNumberTypes.add(BookingDashboardNumberType.TYPE_CONTAINER_NO)
        bookingDashboardNumberTypes.add(BookingDashboardNumberType.TYPE_CONSIGNEE_NAME)
    }

    private fun showNumberTypeDialog() {
        val spinDataList = mutableListOf<TextItem>()
        for (numberType in bookingDashboardNumberTypes) {
            spinDataList.add(TextItem(getString(numberType.id),
                    numberType.code == bookingFilter.numberType.code,
                    numberType.code))
        }

        val view = layoutInflater.inflate(R.layout.textpicker_bottom_sheet_dialog, null)
        val dialog = BottomSheetDialog(this)

        dialog.setCancelable(true)
        dialog.setContentView(view)

        var selectedNumberType: BookingDashboardNumberType = BookingDashboardNumberType.TYPE_BL_NO
        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.textpicker_item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                view.picker.setSelected(index)
                selectedNumberType = bookingDashboardNumberTypes
                        .find { it.code == spinDataList[index]._index } ?: BookingDashboardNumberType.TYPE_BL_NO
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
            bookingFilter.numberType = selectedNumberType
            bookingFilter.numberValue = EmptyString
            searchInit()
        }

        view.picker.setItems(spinDataList)
        view.picker.index = spinDataList.first { it._isSelected }._index
        dialog.show()
    }

    private fun searchFilter(searchWord: String) {
        bookingFilter.numberValue = searchWord
        viewModel.clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_FILTER, bookingFilter))
    }

    private fun searchInit() {
        // init search number type & input area
        tv_filter_booking_no.text = getString(bookingFilter.numberType.id)
        searchUiInit()
    }
    private fun searchUiInit() {
        with(bookingFilter){

            val calendar = Calendar.getInstance()
            dateStarts = BookingFilter.Date(calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.YEAR))
            dateEnds = BookingFilter.Date(2, 28, 2021)
        }
        searchFilter(EmptyString)

//        setFilterData()
    }

    //----- filter  --> original -> cargo trancking

    private fun setFilterData(filterData: BookingFilter = bookingFilter) {
        filterAdapter.itemList.clear()
        /*
        * All Route / CNSHA - USLAX
        * All Date  / Aug 13, 2020 - Aug 13, 2020
        */
        with(filterData) {
            var statusFilterCount = 5
            if(!isStatusBookingDraft)
                statusFilterCount--
            if(!isStatusBookingConfirmed)
                statusFilterCount--
            if(!isStatusPolArrived)
                statusFilterCount--
            if(!isStatusBdr)
                statusFilterCount--
            if(!isStatusPodArrived)
                statusFilterCount--

            var statusFilter = ""
            if(statusFilterCount == 5){
                statusFilter = getString(R.string.booking_dashboard_filters_status_all_status)
            }else {
                if(isStatusBookingDraft){statusFilter = getString(R.string.booking_dashboard_filters_status_draft_summary)}
                else if(isStatusBookingConfirmed){statusFilter = getString(R.string.booking_dashboard_filters_status_confirmed_summary)}
                else if(isStatusPolArrived){statusFilter = getString(R.string.booking_dashboard_filters_status_pol_arrived)}
                else if(isStatusBdr){statusFilter = getString(R.string.booking_dashboard_filters_status_bdr)}
                else {statusFilter = getString(R.string.booking_dashboard_filters_status_pod_arrived)}
                if(statusFilterCount > 1)
                    statusFilter += " +${statusFilterCount-1}"
            }
            filterAdapter.itemList.add(BookingSelectValue(0,
                    CargoTrackingFilterMoveType.FILTER_SCROLL_TO_TOP,
                    statusFilter)
            )

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
            filterAdapter.itemList.add(BookingSelectValue(1,
                    CargoTrackingFilterMoveType.FILTER_SCROLL_TO_ROUTE,
                    firstRouteFilter)
            )

            val prefix: String
            var secondDateFilter = ""
            when(dateType) {
                CargoTrackingFilterDate.FILTER_DATE_ALL -> {
                    if ( dateStarts.year < 1 || dateStarts.month < 1 || dateStarts.day < 1) {
                        prefix = getString(R.string.cargo_tracking_filters_all_date)
                    } else {
                        prefix = getString(R.string.cargo_tracking_filters_all)
                        secondDateFilter = "${getEngShortMonth(dateStarts.month)} ${dateStarts.day}, ${dateStarts.year}" +
                                " - " + "${getEngShortMonth(dateEnds.month)} ${dateEnds.day}, ${dateEnds.year}"
                    }
                }
                CargoTrackingFilterDate.FILTER_DATE_POL_ETD, CargoTrackingFilterDate.FILTER_DATE_POD_ETA -> {
                    prefix =  if (dateType == CargoTrackingFilterDate.FILTER_DATE_POL_ETD) {
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
            filterAdapter.itemList.add(BookingSelectValue(2,
                    CargoTrackingFilterMoveType.FILTER_SCROLL_TO_DATE,
                    prefix,
                    secondDateFilter)
            )
        }
        filterAdapter.notifyDataSetChanged()
    }


    private val filterAdapter by lazy {
        FilterAdapter()
                .apply {
                    onClickItem = { position ->
                        bookingFilter.scrollType = when (position) {
                            0 -> {
                                CargoTrackingFilterMoveType.FILTER_SCROLL_TO_TOP
                            }
                            1 -> {
                                CargoTrackingFilterMoveType.FILTER_SCROLL_TO_ROUTE
                            }
                            else -> {
                                CargoTrackingFilterMoveType.FILTER_SCROLL_TO_DATE
                            }
                        }
                        clickViewParameterAny(Pair(ParameterAny.ANY_SEARCH_GO_FILTER, bookingFilter))
                    }
                }
    }

    private class FilterAdapter : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

        var itemList = mutableListOf<BookingSelectValue>()
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


    //-----

    inner class ViewPagerAdapter(val pageList: ArrayList<String>, fragment: FragmentActivity) : FragmentStateAdapter(fragment){
        private var fragments = mutableListOf<Fragment>()
        override fun getItemCount(): Int = pageList.size
        override fun createFragment(position: Int): Fragment {
            val fragment = BookingDashboardConditionDetailFragment(viewModel, bookingList.firstOrNull())
            when(position){
                0 -> {fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.CHARGE_PAYPLAN)}
                else -> {fragment.setUiType(BookingDashboardConditionDetailFragment.BookingDashboardUiType.TRACKING)}
            }
            return fragment
        }
    }


    override fun canSwipeLeft(position: Int): Boolean {
        return true
    }

    override fun canSwipeRight(position: Int): Boolean {
        return true
    }

    override fun onDismissedBySwipeLeft(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        tabLayout.selectTab(tabLayout.getTabAt(0))
        ll_swipe_holder.visibility = View.VISIBLE
    }

    override fun onDismissedBySwipeRight(recyclerView: RecyclerView, reverseSortedPositions: IntArray) {
        tabLayout.selectTab(tabLayout.getTabAt(1))
        ll_swipe_holder.visibility = View.VISIBLE
    }
}
