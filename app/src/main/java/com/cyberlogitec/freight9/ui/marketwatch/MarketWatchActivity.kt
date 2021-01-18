package com.cyberlogitec.freight9.ui.marketwatch

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cyberlogitec.freight9.BuildConfig.WS_URL_WATCH
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_MARKET_WATCH
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.split.*
import com.cyberlogitec.freight9.lib.ui.split.SplitConst.Companion.SPLIT_SLIDE_HALF
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.lib.util.Intents.Companion.MARKET_WATCH_CHART_SETTING
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.cyberlogitec.freight9.ui.routeselect.select.RouteSelectActivity
import com.cyberlogitec.freight9.ui.trademarket.*
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.CombinedChartRenderer
import com.github.mikephil.charting.renderer.ExtentionLineChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.act_market.btn_market_filter_carrier
import kotlinx.android.synthetic.main.act_market.btn_market_filter_container
import kotlinx.android.synthetic.main.act_market.btn_market_filter_ppd
import kotlinx.android.synthetic.main.act_market.container_current_route
import kotlinx.android.synthetic.main.act_market.tv_market_pod
import kotlinx.android.synthetic.main.act_market.tv_market_pod_detail
import kotlinx.android.synthetic.main.act_market.tv_market_pol
import kotlinx.android.synthetic.main.act_market.tv_market_pol_detail
import kotlinx.android.synthetic.main.act_market_watch.*
import kotlinx.android.synthetic.main.item_market_split_title.view.*
import kotlinx.android.synthetic.main.split_market_drag_top.*
import kotlinx.android.synthetic.main.split_market_view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs


@RequiresActivityViewModel(value = MarketWatchViewModel::class)
class MarketWatchActivity : BaseActivity<MarketWatchViewModel>(), OnChartValueSelectedListener, OnChartGestureListener {

    private lateinit var selectedBaseWeek: String
    private lateinit var chartRequestFilter: PostMarketWatchChartRequest
    private lateinit var tfRegular: Typeface
    private lateinit var tfLight: Typeface
    private lateinit var tfBold: Typeface
    private lateinit var tfExtraBold: Typeface
    private lateinit var splitScreen: SplitScreen
    private var splitDisplayCategory = SplitDisplayCategory.LiveDealPrice

    private lateinit var bottomsheetbehavior : CustomBottomSheetBehavior<FrameLayout>

    private var carrierList = ArrayList<Carrier>()
    private var containerList = ArrayList<Container>()
    private var paymentList = ArrayList<Payment>()

    private var selectedRoute: MarketRoute = MarketRoute(null,"","","","")

    private var isInitChart: Boolean = false

    private var gson: Gson = Gson()

    private val volumeChartRatio = 1.5f

    private var dataList: ArrayList<MarketWatchChartList.WeekItems> = ArrayList()

    private var chartSetting: Chart = Chart(storeId = this.javaClass.simpleName)
    private var chartZeroOffset = 0.15f
    private var chartOffset = 1 - chartZeroOffset

    private var stompUrl: String = ""

    private val adapter by lazy {
        SplitViewPagerAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_market_watch)
        (application as App).component.inject(this)

        window.statusBarColor = getColor(R.color.color_1d1d1d)

        initToolbar()

        initRoute()
        initFilter()
        initChartSetting()


        initSplitView()
        initView()
        initChart()
    }

    private fun initChartSetting() {
        viewModel.outPuts.onSuccessLoadChartSetting()
                .bindToLifecycle(this)
                .subscribe {
                    if(it.storeId == this.javaClass.simpleName) {
                        chartSetting = it

                        updateData()
                    }
                }
        viewModel.outPuts.onFailLoadChartSetting()
                .bindToLifecycle(this)
                .subscribe {
                    chartSetting = Chart(this.javaClass.simpleName)
                    chartSetting.chartType = getString(R.string.market_watch_chart_candle)
                    chartSetting.interVal = getString(R.string.market_watch_chart_1week)
                    updateData()
                }
        viewModel.inPuts.loadChartSetting(this.javaClass.simpleName)
    }

    private fun initView() {
        iv_go_week_deal.setSafeOnClickListener {
            if(dataList.isNullOrEmpty())
                return@setSafeOnClickListener
            chartRequestFilter.polDetail = selectedRoute.fromDetail!!
            chartRequestFilter.podDetail = selectedRoute.toDetail!!
            val intent = Intent(this, MarketWatchWeekActivity::class.java)
            intent.putExtra("week", selectedBaseWeek)
            intent.putExtra("filter",chartRequestFilter)
            intent.putExtra("carriers",carrierList)
            intent.putExtra("payment",paymentList)
            intent.putExtra("chartLists", dataList )
            startActivity(intent)
        }

        iv_go_setting.setSafeOnClickListener {
            val intent = Intent(this, MarketWatchSettingActivity::class.java)
            intent.putExtra("type", MarketWatchSettingActivity.WatchUiType.CHART_SETTING_DEFAULT)
            intent.putExtra(MARKET_WATCH_CHART_SETTING, chartSetting)
            startActivityForResult(intent, PICK_CHART_SETTING_REQUEST)
        }
    }


    private fun initRoute() {

        selectedRoute = MarketRoute(null,"CNSHA","Shanghai, Shanghai","DEHAM","Hamburg, HH")
        viewModel.outPuts.onSuccessLoadRouteFilter()
                .bindToLifecycle(this)
                .subscribe {
                    if (it != null) {
                        updateRoute(it)
                    }else {
                        selectedRoute = MarketRoute(null,"","","","")
                    }
                }
        viewModel.inPuts.loadRouteFilter(Parameter.EVENT)
        //add route selector
        container_current_route.setSafeOnClickListener {
            Timber.d("f9: startActivity(RouteSelectActivity)")
            val intent = Intent(this, RouteSelectActivity::class.java)
            intent.putExtra("fromCode", selectedRoute.fromCode)
            intent.putExtra("fromDetail", selectedRoute.fromDetail)
            intent.putExtra("toCode", selectedRoute.toCode)
            intent.putExtra("toDetail", selectedRoute.toDetail)
            startActivityForResult(intent, PICK_ROUTE_REQUEST)
        }
        updateRoute(selectedRoute)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                PICK_CARRIER_REQUEST -> {
                    carrierList.clear()
                    btn_market_filter_carrier.text = checkCarrierFilterName(carrierList)
                }
                PICK_ROUTE_REQUEST -> {
                    if (data != null) {
                        selectedRoute = MarketRoute(null,
                                data.getStringExtra("selectFromCode"),
                                data.getStringExtra("selectFromDetail"),
                                data.getStringExtra("selectToCode"),
                                data.getStringExtra("selectToDetail"))
                    }
                    storeRouteDB(selectedRoute)
                    updateRoute(selectedRoute)
                }
                PICK_CHART_SETTING_REQUEST -> {
                    if (data != null) {
                        chartSetting = data.getSerializableExtra(MARKET_WATCH_CHART_SETTING) as Chart
                        viewModel.inPuts.storeChartSetting(chartSetting)
                        updateData()
                    }
                }
            }
        }

    }
    private fun updateRoute(route: MarketRoute) {
        tv_market_pol.text = if(route.fromCode.isNotEmpty()) route.fromCode else "FROM"
        tv_market_pol_detail.text = route.fromDetail
        tv_market_pod.text = if(route.toCode.isNotEmpty()) route.toCode else "TO"
        tv_market_pod_detail.text = route.toDetail
        selectedRoute = route
    }

    private fun storeRouteDB(route: MarketRoute) {
        viewModel.inPuts.storeRouteFilter(route)

    }

    private fun initToolbar() {
        toolbar_common.setBackgroundColor(getColor(R.color.color_1d1d1d))

        // set custom toolbar
        defaultbarInit(toolbar_common, menuType = MenuType.DEFAULT, title = "Market Watch", isEnableNavi = false)

        // wait click event (toolbar left button)
        toolbar_common.toolbar_left_btn.setOnClickListener{
            it?.let {
                Timber.d("f9: toolbar_left_btn clcick")
                onBackPressed()
            }
        }

        // on click toolbar right button
        // emit event to viewModel -> show drawer menu
        toolbar_common.toolbar_right_btn.setOnClickListener {
            Timber.d("f9: toolbar_right_btn click")

            viewModel.inPuts.clickToMenu(Parameter.CLICK)
        }

        // receive ViewModel event (gotoMenu)
        viewModel.outPuts.gotoMenu()
                .bindToLifecycle(this)
                .subscribe{
                    Timber.d("f9: startActivity(MenuActivity)")
                    startMenuActivity(MENUITEM_MARKET_WATCH, MenuActivity::class.java)
                }

    }

    private fun initFilter() {

        viewModel.outPuts.refreshCarrierFilter()
                .bindToLifecycle(this)
                .subscribe {
                    carrierList.clear()
                    carrierList.addAll(it)
                    btn_market_filter_carrier.text = checkCarrierFilterName(carrierList)
                    requestChartList()
                }
        viewModel.outPuts.refreshContainerFilter()
                .bindToLifecycle(this)
                .subscribe {
                    containerList.clear()
                    containerList.addAll(it)
                    btn_market_filter_container.text = checkContainerFilterName(containerList)
                }
        viewModel.outPuts.refreshPaymentFilter()
                .bindToLifecycle(this)
                .subscribe {
                    paymentList.clear()
                    paymentList.addAll(it)
                    btn_market_filter_ppd.text = checkPaymentFilterName(paymentList)

                }

        btn_market_filter_carrier.setSafeOnClickListener {
            Timber.d("f9: btn_market_filter_carrier click")
            viewModel.inPuts.clickToCarrierFilter(Parameter.CLICK)

        }
        btn_market_filter_carrier.text = checkCarrierFilterName(carrierList)

        viewModel.outPuts.gotoCarrierFilter()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketCarrierFilterActivity")
                    startActivity(MarketCarrierFilterActivity::class.java)
                }
        btn_market_filter_container.setSafeOnClickListener {
            Timber.d("f9: btn_market_filter_carrier click")
            viewModel.inPuts.clickToContainerFilter(Parameter.CLICK)
        }
        btn_market_filter_container.text = checkContainerFilterName(containerList)
        viewModel.outPuts.gotoContainerFilter()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketContainerFilterActivity")
                    startActivity(MarketContainerFilterActivity::class.java)
                }

        btn_market_filter_ppd.setSafeOnClickListener {
            Timber.d("f9: btn_market_filter_ppd click")
            viewModel.inPuts.clickToPpdFilter(Parameter.CLICK)
        }
        viewModel.outPuts.gotoPpdFilter()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(MarketPpdFilterActivity")
                    startActivity(MarketPpdFilterActivity::class.java)
                }
    }

    private fun checkCarrierFilterName(list: ArrayList<Carrier>): String? {
        var displayCarrier = ""
        var count = 0
        for(data in list) {
            if(data.select!!){
                count++
                if(displayCarrier == "")
                    displayCarrier = data.carriercode
            }
        }
        if(count == list.size)
            displayCarrier = getString(R.string.carrier_all)
        else if(count > 1)
            displayCarrier += " +${count -1}"

        return displayCarrier

    }
    private fun checkContainerFilterName(list: ArrayList<Container>): String? {
        var rdTerm = ""
        var type = ""
        var size = ""

        for(data in list) {
            if(data.selected!!){
                when(data.dataType) {
                    "rdterm" -> {
                        when(data.code) {
                            "YY" -> { rdTerm = resources.getString(R.string.rd_term_type_cycy_abbrev)
                                chartRequestFilter.rDTermCode = "01"}
                            "YD" -> { rdTerm = resources.getString(R.string.rd_term_type_cydoor_abbrev)
                                chartRequestFilter.rDTermCode = "02"}
                            "DY" -> { rdTerm = resources.getString(R.string.rd_term_type_doorcy_abbrev)
                                chartRequestFilter.rDTermCode = "03"}
                            "DD" -> { rdTerm = resources.getString(R.string.rd_term_type_doordoor_abbrev)
                                chartRequestFilter.rDTermCode = "04"}
                        }
                    }
                    "type" -> {
                        when(data.code) {
                            ContainerType.F_TYPE -> { type = resources.getString(R.string.full_container_simple_abbrev)
                                chartRequestFilter.containerTypeCode = "01"}
                            ContainerType.R_TYPE -> { type = resources.getString(R.string.rf_container_simple_abbrev)
                                chartRequestFilter.containerTypeCode = "02"}
                            ContainerType.S_TYPE -> { type = resources.getString(R.string.soc_container_simple_abbrev)
                                chartRequestFilter.containerTypeCode = "03"}
                            ContainerType.E_TYPE -> { type = resources.getString(R.string.empty_container_simple_abbrev)
                                chartRequestFilter.containerTypeCode = "04"}
                        }
                    }
                    "size" -> {size = data.code}
                }
            }
        }
        return "$rdTerm $type"
    }

    private fun checkPaymentFilterName(list: ArrayList<Payment>): CharSequence? {
        var type = ""
        var plan = ""

        for(data in list) {
            if(data.selected!!){
                when(data.dataType) {
                    "type" -> {
                        when(data.paymenttypecode) {
                            "P" -> { type = resources.getString(R.string.filter_payment_type_ppd)
                                chartRequestFilter.paymentTermCode = "01"}
                            "C" -> { type = resources.getString(R.string.filter_payment_type_cct)
                                chartRequestFilter.paymentTermCode = "02"}
                        }
                    }
                    "plan" -> {plan = data.iniPymtRto!!}
                }
            }
        }
        return "$type ($plan%)"
    }

    private fun changeSplitViewTitle(displayCategory: SplitDisplayCategory) {
        splitScreen.changeTitle(displayCategory)
    }

    private fun initSplitView() {

        adapter.add(resources.getString(R.string.split_market_live_deal_price))
        adapter.add(resources.getString(R.string.split_market_your_offers_on_market))
        adapter.add(resources.getString(R.string.split_market_your_inventory))

        vp_market_split.apply {
            adapter = this@MarketWatchActivity.adapter
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_1, MarketLiveDealFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_2, MarketMyOfferFragment.newInstance(viewModel))
                .commit()
        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_container_body_3, MarketInventoryFragment.newInstance(viewModel))
                .commit()

        vp_market_split.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                Timber.d("viewpager scroll state changed")

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Timber.d("viewpager page scrolled")

            }

            override fun onPageSelected(position: Int) {
                Timber.d("viewpager page selected $position")
                changeSplitView(position)
            }
        })
        tabLayout_indicator.setupWithViewPager(vp_market_split)
        Handler().postDelayed({
            initSplitView(SplitDisplayCategory.LiveDealPrice, BottomSheetBehavior.STATE_COLLAPSED)
        },100)
    }

    private fun changeSplitView(position: Int) {
        ll_container_body_1.visibility = View.INVISIBLE
        ll_container_body_2.visibility = View.INVISIBLE
        ll_container_body_3.visibility = View.INVISIBLE
        ll_container_body_4.visibility = View.INVISIBLE
        when(position) {
            0-> { ll_container_body_1.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.LiveDealPrice)}
            1-> { ll_container_body_2.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.YourOffersOnMarket)}
            2-> { ll_container_body_3.visibility = View.VISIBLE
                changeSplitViewTitle(SplitDisplayCategory.YourInventory)}
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun initChart() {

        chart_trade.description.isEnabled = false
        chart_trade.setBackgroundColor(baseContext?.let { ContextCompat.getColor(it, R.color.color_0d0d0d) }!!)
        chart_trade.setDrawGridBackground(false)
        chart_trade.xAxis.setDrawGridLines(false)
        chart_trade.axisLeft.setDrawGridLines(false)
        chart_trade.axisRight.setDrawGridLines(false)
        chart_trade.isAutoScaleMinMaxEnabled = true
        chart_trade.setPinchZoom(false)
        chart_trade.isScaleXEnabled = true
        chart_trade.isScaleYEnabled = false
        chart_trade.setDrawBarShadow(false)
        chart_trade.legend.isEnabled = false
        chart_trade.isHighlightFullBarEnabled = false
        chart_trade.extraBottomOffset = 25f

        chart_trade.drawOrder = arrayOf(DrawOrder.BAR, DrawOrder.CANDLE)

        val rightAxis = chart_trade.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = getFont(baseContext!!, R.font.opensans_bold)!!
        rightAxis.textColor = getColor(R.color.greyish_brown)
        rightAxis.textSize = 13f
        rightAxis.minWidth = 48f
        rightAxis.setDrawAxisLine(true)
        rightAxis.axisLineColor = getColor(R.color.color_1a1a1a)

        val leftAxis = chart_trade.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.minWidth = 0f
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawAxisLine(false)

        val xAxis = chart_trade.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.mLabelHeight = 85
        xAxis.setLabelCount(6, true)
        xAxis.typeface = getFont(baseContext!!, R.font.opensans_bold)!!
        xAxis.textColor = getColor(R.color.greyish_brown)
        xAxis.textSize = 13f
        xAxis.axisMinimum = 1f
        xAxis.mLabelWidth = 30
        xAxis.setMultiLineLabel(true)
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = getColor(R.color.color_1a1a1a)
        xAxis.granularity = 1f

        xAxis.setAvoidFirstLastClipping(false)
        chart_trade.setOnChartValueSelectedListener(this)
        chart_trade.onChartGestureListener = this

        chartRequestFilter = PostMarketWatchChartRequest(getRequestCarrier(),"01", "01",  "01", selectedRoute.fromCode, selectedRoute.fromDetail!!, selectedRoute.toCode, selectedRoute.toDetail!!,"01")

        viewModel.outPuts.onSuccessRequestChartList()
                .bindToLifecycle(this)
                .subscribe { it ->

                    this.dataList.clear()
                    if(it.weekItems != null)
                        this.dataList.addAll(it.weekItems.sortedBy { it.baseYearWeek})
                    updateData()

                    //stomp 연결 시작
                    stompUrl = it.topicName
                    if(::mStompClient.isInitialized && mStompClient.isConnected) {
                        compositeDisposable?.remove(dispTopic)
                        setSubscription(makeStompUrl(chartRequestFilter))
                    }else {
                        connectStomp(makeStompUrl(chartRequestFilter))
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        viewModel.inPuts.refreshFilter(Parameter.EVENT)
    }

    private fun requestChartList() {
        chartRequestFilter.pol = selectedRoute.fromCode
        chartRequestFilter.pod = selectedRoute.toCode
        chartRequestFilter.companyCodes = getRequestCarrier()
        //payplan depseq 를 보냄

        viewModel.inPuts.requestChartList(chartRequestFilter)

    }

    private fun getRequestCarrier() : ArrayList<String> {
        val carrier = ArrayList<String>()
        for(data in carrierList) {
            if(data.select!!)
                carrier.add(data.carriercode)
        }
        return carrier
    }

    private fun makeData() {
        dataList.clear()

        for (i in 0..110) {
            val low: Float = getRandom(500f,10f)
            val high: Float = getRandom(500f,low)
            val open: Float = getRandom(high - low,low)
            val deal: Float = getRandom(high - low,low)

            dataList.add(MarketWatchChartList.WeekItems("201901", "open",
                    open.toDouble(),
                    low.toDouble(),
                    high.toDouble(),
                    deal.toDouble(),
                    getRandom(1200f,100f).toDouble(),
                    10.0,
                    0.02,
                    Calendar.getInstance().time.toString()))
        }
    }
    private fun updateData() {
        if(this.dataList.isNullOrEmpty()) {
            if(chart_trade.data != null) {

                chart_trade.clear()
            }
            return
        }
        /*
        this.dataList.clear()
        this.dataList.addAll(dataList.sortedBy { it.baseYearWeek})*/
        chart_trade.moveViewToX(0f)

        chart_trade.drawOrder = arrayOf(
                DrawOrder.BAR, DrawOrder.CANDLE, DrawOrder.LINE
        )

        val data = CombinedData()
        when(chartSetting.chartType) {
            getString(R.string.market_watch_chart_line) -> {
                data.setData(generateLineData(this.dataList, getColor(R.color.color_chart_mountain_line), false))

            }
            getString(R.string.market_watch_chart_mountain) -> {
                data.setData(generateMountainData(this.dataList,false))
            }
            else -> {
                data.setData(generateCandleData(this.dataList))
            }
        }
        data.setData(generateVolumeData(this.dataList))

        chart_trade.xAxis.axisMaximum = data.xMax + 0.25f
        chart_trade.xAxis.axisMinimum = data.xMin - 0.25f
        //거래량 차트 y 최대값
        val yAxisLeft = chart_trade.getAxis(YAxis.AxisDependency.LEFT)
        val max = this.dataList.maxBy { it.volume }
        yAxisLeft.axisMaximum = max!!.volume.toFloat()/volumeChartRatio *10
        val yAxisRight = chart_trade.getAxis(YAxis.AxisDependency.RIGHT)
        chart_trade.axisLeft.spaceTop = 100f

        when(chartSetting.interVal) {
            getString(R.string.market_watch_chart_1day) -> {
                val formatter = XAxisDayFormatter()
                formatter.setStartDate(this.dataList.first().baseYearWeek)
                chart_trade.xAxis.valueFormatter = formatter
            }
            getString(R.string.market_watch_chart_1week) -> {
                val formatter = XAxisWeekFormatter()
                formatter.setStartDate(this.dataList.first().baseYearWeek)
                chart_trade.xAxis.valueFormatter =  formatter
            }
            getString(R.string.market_watch_chart_1month) -> {
                val formatter = XAxisMonthFormatter()
                formatter.setStartDate(this.dataList.first().baseYearWeek)
                chart_trade.xAxis.valueFormatter = formatter
            }
        }

        chart_trade.axisRight.valueFormatter = RightAxisFormatter()

        chart_trade.zoom(1f, 0f, 0f, 0f)

        chart_trade.data = data

        for(renderer in (chart_trade.renderer as CombinedChartRenderer).subRenderers) {
            when(renderer) {
                is ExtentionLineChartRenderer -> { renderer.mDrawFillOffsetPerBottom = chartZeroOffset}
            }
        }
        yAxisRight.axisMinimum = -(chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum*chartZeroOffset/chartOffset)
        chart_trade.setVisibleXRange(1f,20f)

        chart_trade.moveViewToX(data.xMax)

        updateCard(this.dataList.last())

    }

    private fun generateVolumeData(dataList: ArrayList<MarketWatchChartList.WeekItems>): BarData {
        val entries = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()
        for ((index, data) in dataList.withIndex()) {
            entries.add(BarEntry(index+1f, data.volume.toFloat()))
            when(index) {
                0 -> {colors.add(getColor(R.color.color_chart_bar_volume_down))}
                else -> {
                    if(dataList[index-1].volume > data.volume)
                        colors.add(getColor(R.color.color_chart_bar_volume_down))
                    else
                        colors.add(getColor(R.color.color_chart_bar_volume_up))
                }
            }
        }

        val set = BarDataSet(entries, "Bar 1")
        set.color = Color.rgb(76, 76, 76)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.setDrawValues(false)
        set.colors = colors

        val barWidth = 0.45f // x2 dataset
        val d = BarData(set)
        d.isHighlightEnabled = false
        d.barWidth = barWidth

        return d
    }
    private fun generateCandleData(dataList: ArrayList<MarketWatchChartList.WeekItems>): CandleData {
        val d = CandleData()
        val entries = ArrayList<CandleEntry>()

        for ((index, data) in dataList.withIndex()) {

            entries.add(CandleEntry(index + 1f,
                    data.high.toFloat(),
                    data.low.toFloat(),
                    data.open.toFloat(),
                    data.close.toFloat(),data))
        }

        val set = CandleDataSet(entries, "Candle DataSet")
        set.decreasingColor = Color.rgb(0, 58, 255)
        set.increasingColor = Color.rgb(0, 179, 160)
        set.neutralColor = ContextCompat.getColor(baseContext, R.color.greyish_brown)
        set.highLightColor = getColor(R.color.white)
        set.color = getColor(R.color.color_chart_mountain_line)
        set.enableDashedHighlightLine(Utils.convertDpToPixel(1f),10f,0f)
        set.increasingPaintStyle = Paint.Style.FILL
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.shadowColor = Color.DKGRAY
        set.barSpace = 0.3f
        set.valueTextSize = 13f
        set.valueTextColor = getColor(R.color.color_141414)
        set.valueTypeface = getFont(this, R.font.opensans_extrabold)
        set.setDrawValues(false)
        d.addDataSet(set)

        return d
    }

    private fun generateLineData(dataList: ArrayList<MarketWatchChartList.WeekItems>, color: Int, useValueLabel: Boolean): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()

        for ((index, data) in dataList.withIndex()) {

            entries.add(Entry(index + 1f,
                    data.close.toFloat(),data))
        }
        val set = LineDataSet(entries, "Line DataSet")
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.valueTextSize = 20f
        set.color = color
        set.lineWidth = 1f
        set.highLightColor = getColor(R.color.white)
        set.enableDashedHighlightLine(Utils.convertDpToPixel(1f),10f,0f)
        set.isDrawValueLabel = useValueLabel
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.valueTextSize = 13f
        set.valueTextColor = getColor(R.color.color_141414)
        set.valueTypeface = getFont(this, R.font.opensans_extrabold)
        d.addDataSet(set)

        /*makeData()
        entries = ArrayList<Entry>()
        for ((index, data) in list.withIndex()) {

            entries.add(Entry(index + 1f,
                    data.close.toFloat(),data))
        }
        val set1 = LineDataSet(entries, "Line DataSet1")
        set1.axisDependency = YAxis.AxisDependency.RIGHT
        set1.valueTextSize = 20f
        set1.color = color
        set1.lineWidth = 1f
        set1.label
        set1.isDrawValueLabel = true
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.isHighlightEnabled = false
        d.addDataSet(set1)*/

        return d

    }
    private fun generateMountainData(dataList: ArrayList<MarketWatchChartList.WeekItems>, useValueLabel: Boolean): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()

        for ((index, data) in dataList.withIndex()) {

            entries.add(Entry(index + 1f,
                    data.close.toFloat(),data))
        }
        val set = LineDataSet(entries, "Mountain DataSet")
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.valueTextSize = 20f
        set.color = getColor(R.color.color_chart_mountain_line)
        set.lineWidth = 1f
        set.setDrawFilled(true)
        set.fillDrawable = getDrawable(R.drawable.fade_chart_bfbfbf)
        set.highLightColor = getColor(R.color.white)
        set.enableDashedHighlightLine(Utils.convertDpToPixel(1f),10f,0f)
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.valueTextSize = 13f
        set.valueTextColor = getColor(R.color.color_141414)
        set.valueTypeface = getFont(this, R.font.opensans_extrabold)
        set.isDrawValueLabel = useValueLabel
        d.addDataSet(set)

        return d
    }

    inner class XAxisDayFormatter : ValueFormatter() {
        private var startDate = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat")
        val wdf : SimpleDateFormat = SimpleDateFormat("MM-dd")
        var cal = Calendar.getInstance()


        override fun getFormattedValue(value: Float): String {
            cal.time = startDate.time
            cal.add(Calendar.WEEK_OF_YEAR, value.toInt()-1)
            cal.set(Calendar.DAY_OF_WEEK, 1)
            return if(((value % 53).toInt()+1) == 1)
                wdf.format(cal.time) + "\n" + cal.get(Calendar.YEAR).toString()
            else
                wdf.format(cal.time)
        }
        fun setStartDate(baseWeek: String) {
            startDate.set(Calendar.YEAR,baseWeek.substring(0,4).toInt())
            startDate.set(Calendar.WEEK_OF_YEAR, baseWeek.substring(4,6).toInt())

        }
    }

    inner class XAxisWeekFormatter : ValueFormatter() {
        private var startDate = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat")
        val wdf : SimpleDateFormat = SimpleDateFormat("ww")
        var cal = Calendar.getInstance()


        override fun getFormattedValue(value: Float): String {
            cal.time = startDate.time
            cal.add(Calendar.WEEK_OF_YEAR, value.toInt()-1)
            cal.set(Calendar.DAY_OF_WEEK, 1)
            return if(((value % 53).toInt()+1) == 1)
                "W"+ wdf.format(cal.time) + "\n" + cal.get(Calendar.YEAR).toString()
            else
                "W"+ wdf.format(cal.time)
        }
        fun setStartDate(baseWeek: String) {
            startDate.set(Calendar.YEAR,baseWeek.substring(0,4).toInt())
            startDate.set(Calendar.WEEK_OF_YEAR, baseWeek.substring(4,6).toInt())
        }
    }

    inner class XAxisMonthFormatter : ValueFormatter() {
        private var startDate = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat")
        val wdf : SimpleDateFormat = SimpleDateFormat("MMM")
        var cal = Calendar.getInstance()


        override fun getFormattedValue(value: Float): String {
            cal.time = startDate.time
            cal.add(Calendar.WEEK_OF_YEAR, value.toInt()-1)
            cal.set(Calendar.DAY_OF_WEEK, 1)
            return if(((value % 53).toInt()+1) == 1)
                wdf.format(cal.time) + "\n" + cal.get(Calendar.YEAR).toString()
            else
                wdf.format(cal.time)
        }
        fun setStartDate(baseWeek: String) {
            startDate.set(Calendar.YEAR,baseWeek.substring(0,4).toInt())
            startDate.set(Calendar.WEEK_OF_YEAR, baseWeek.substring(4,6).toInt())
        }
    }
    inner class RightAxisFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value >0) DecimalFormat("#,###").format(value.toLong()) else ""
        }
    }

    private fun getRandom(range: Float, start: Float): Float {
        return (Math.random() * range).toFloat() + start
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null) {
            updateCard(e.data as MarketWatchChartList.WeekItems)
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateCard(data: MarketWatchChartList.WeekItems) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.minimumFractionDigits = 0

        tv_week.text = "${getWeekFull(data.baseYearWeek)}, ${data.baseYearWeek.substring(0,4)}"
        tv_deal_price.text = currencyFormat.format(data.close.toInt())
        when {
            (data.close - data.open) > 0 -> {
                v_deal_zero.visibility = View.INVISIBLE
                v_deal_up_down.visibility = View.VISIBLE
                v_deal_up_down.background = getDrawable(R.drawable.ic_up_green)
                tv_deal_price_diff.setTextColor(getColor(R.color.green_blue))
                tv_deal_price_diff_percent.setTextColor(getColor(R.color.green_blue))
            }
            (data.close - data.open) < 0 -> {
                v_deal_zero.visibility = View.INVISIBLE
                v_deal_up_down.visibility = View.VISIBLE
                v_deal_up_down.background = getDrawable(R.drawable.ic_down_blue)
                tv_deal_price_diff.setTextColor(getColor(R.color.color_003aff))
                tv_deal_price_diff_percent.setTextColor(getColor(R.color.color_003aff))
            }
            else -> {
                v_deal_zero.visibility = View.VISIBLE
                v_deal_up_down.visibility = View.INVISIBLE
                tv_deal_price_diff.setTextColor(getColor(R.color.white))
                tv_deal_price_diff_percent.setTextColor(getColor(R.color.white))
            }
        }
        tv_deal_price_diff.text = currencyFormat.format(abs(data.close - data.open).toInt())
        tv_deal_price_diff_percent.text = "(${"%.2f".format(abs((data.close - data.open).toFloat()/data.open*100))}%)"
        val cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        when(data.status) {
            "Trading Closed" -> {
                tv_deal_date.text = data.status
            }
            else -> {

                cal.time =data.latestEventTimeStamp.toDate("yyyyMMddHHmmssSSSSSS")
                tv_deal_date.text = df.format(cal.time)
            }
        }
        //date 표시
        tv_open_price.text = "${currencyFormat.format(data.open.toInt())}/T"
        tv_low_price.text = "${currencyFormat.format(data.low.toInt())}/T"
        tv_high_price.text = "${currencyFormat.format(data.high.toInt())}/T"

        tv_deal_volume.text = "${DecimalFormat("#,###").format(data.volume.toInt())}T"

        selectedBaseWeek = data.baseYearWeek

    }

    override fun onNothingSelected() {}

    private var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    private var bottomSheetSlideOffset: Float = SplitConst.SPLIT_SLIDE_COLLAPSED

    private fun initSplitView(splitDisplayCategory: SplitDisplayCategory, isCollapsed: Int) {

        val behavior = BottomSheetBehavior.from<FrameLayout>(bottom_sheet_frameout)

        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        splitScreen = SplitScreen(SplitUiData(this, splitDisplayCategory,
                bottom_sheet_frameout, isCollapsed, ""),
                ::receiveSplitViewEvent, ::receiveSplitViewSlideOffset)
        val dm: DisplayMetrics = applicationContext.resources.displayMetrics
        splitScreen.setHalfExpandRatio(SplitConst.SPLIT_TITLE_HEIGHT_80.toPx().toFloat()/dm.heightPixels)
    }
    private fun receiveSplitViewSlideOffset(slideOffset: Float) {
        when(getBottomState()) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                if (slideOffset < getBottomSlideOffset()) {
                    bottom_sheet_frameout.setPadding(0, 0, 0, 0)
                }
            }
            BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED -> {
                if (slideOffset > SPLIT_SLIDE_HALF) {
                    bottom_sheet_frameout.setPadding(0, resources.getDimension(R.dimen.default_title_height).toInt(), 0, 0)
                }
            }
        }
        setBottomSlideOffset(slideOffset)
    }
    private fun getBottomState() = bottomSheetState

    private fun getBottomSlideOffset() = bottomSheetSlideOffset

    private fun setBottomSlideOffset(slideOffset: Float) {
        bottomSheetSlideOffset = slideOffset
    }

    private fun receiveSplitViewEvent(splitUiReceiveData: SplitUiReceiveData) {
        when(splitUiReceiveData.state) {
            BottomSheetBehavior.STATE_DRAGGING -> {

            }
            BottomSheetBehavior.STATE_COLLAPSED -> {

            }
            BottomSheetBehavior.STATE_HIDDEN -> {
            }
        }
        setBottomState(splitUiReceiveData.state)
    }
    private fun setBottomState(state: Int) {
        if (state == BottomSheetBehavior.STATE_SETTLING
                || state == BottomSheetBehavior.STATE_DRAGGING) {
            return
        }
        bottomSheetState = state
    }

    inner class SplitViewPagerAdapter: PagerAdapter() {

        private var dataList: ArrayList<String> = ArrayList()

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view: View = layoutInflater.inflate(R.layout.item_market_split_title, container, false)

            view.tv_split_view_title.text = dataList[position]
            container.addView(view)
            return view
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return (view == `object` as View)
        }

        override fun getCount(): Int {
            return dataList.size
        }

        fun add(data:String) {
            dataList.add(data)
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }
    companion object {
        const val PICK_CARRIER_REQUEST = 1
        const val PICK_ROUTE_REQUEST = 2
        const val PICK_CHART_SETTING_REQUEST = 3

    }

    override fun onChartFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float) {
    }

    override fun onChartTranslate(p0: MotionEvent?, p1: Float, p2: Float) {
        val yMin = -(chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum*chartZeroOffset/chartOffset)
        if(chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMinimum != yMin) {
            chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMinimum = yMin
            chart_trade.invalidate()
        }
    }

    override fun onChartScale(p0: MotionEvent?, p1: Float, p2: Float) {
    }

    override fun onChartGestureEnd(p0: MotionEvent?, p1: ChartTouchListener.ChartGesture?) {
    }

    override fun onChartSingleTapped(p0: MotionEvent?) {
    }

    override fun onChartGestureStart(p0: MotionEvent?, p1: ChartTouchListener.ChartGesture?) {
    }

    override fun onChartLongPressed(p0: MotionEvent?) {
    }

    override fun onChartDoubleTapped(p0: MotionEvent?) {
    }

    /*
    stomp 연결
     */

    private lateinit var mStompClient: StompClient

    private var response: String = ""

    private var compositeDisposable: CompositeDisposable? = null

    private lateinit var dispTopic: Disposable

    private val usrId = "name"

    private fun connectStomp(url: String) {
        Timber.d("connect stomp $url")
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL_WATCH)

        val share = SharedPreferenceManager(this)
        val headers: MutableList<StompHeader> = ArrayList()
        headers.add(StompHeader(usrId, share.name))

        headers.add(StompHeader("Authorization", share.token))

//        mStompClient.withServerHeartbeat(5000)
//        mStompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)

        resetSubscriptions()

        val dispLifecycle = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {  setMessage("Stomp connection error",true) }
                .subscribe { lifecycleEvent: LifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {setMessage("Stomp connection opened", true)
                        }
                        LifecycleEvent.Type.ERROR -> {
                            setMessage("Stomp connection error \n${lifecycleEvent.exception}",true)
                            if(!this.isDestroyed)
                                doNetworkCheck()
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            setMessage("Stomp connection closed", true)
                            resetSubscriptions()
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> setMessage("Stomp failed server heartbeat", true)
                    }
                }

        compositeDisposable?.add(dispLifecycle)

        setSubscription(url)

        mStompClient.connect(headers)
    }
    private fun setSubscription(url: String) {
        Timber.d("setsubscription $url")
        // Receive greetings
        dispTopic = mStompClient.topic("/topic/${url}")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { t -> setMessage("doOnError $t", true) }
                .subscribe({ topicMessage: StompMessage ->
                    setMessage("Received ${topicMessage.payload}", true)
                    val data: MarketWatchChartList = gson.fromJson(topicMessage.payload, MarketWatchChartList::class.java)
                    runOnUiThread {

                        this.dataList.clear()
                        if(data.weekItems != null)
                            this.dataList.addAll(data.weekItems.sortedBy { it.baseYearWeek})
                        updateData()
                        /*setData(data.matrixWeekPrice, data.qtyUnit)
                        chartData = data.matrixWeekPrice*/
                    }
                })
                { throwable: Throwable? -> setMessage("Error on subscribe topic \n${throwable}", true) }

        compositeDisposable?.add(dispTopic)

    }
    private fun resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable!!.dispose()
        }
        compositeDisposable = CompositeDisposable()
    }
    private fun disconnectStomp() {
        if(::mStompClient.isInitialized)
            mStompClient.disconnect()

    }
    private fun setMessage(msg: String, isReset: Boolean) {
        Timber.d("f9: $msg")
        if(isReset)
            response = (msg + "\n")
        else
            response += (msg + "\n")

    }
    private lateinit var networkCheckDisp: Disposable
    private fun doNetworkCheck() {
        if(::networkCheckDisp.isInitialized)
            networkCheckDisp.dispose()
        networkCheckDisp = Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(checkNetwork()) {
                        connectStomp(makeStompUrl(chartRequestFilter))
                        networkCheckDisp.dispose()
                    }
                }
    }
    private fun checkNetwork():Boolean {

        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private fun makeStompUrl(filter: PostMarketWatchChartRequest): String {
        var url = ""
        url = "${filter.containerTypeCode}_${filter.marketTypeCode}_${filter.paymentTermCode}_${filter.pol}_${filter.pod}_${filter.rDTermCode}"
        return url
    }
}
