package com.cyberlogitec.freight9.ui.marketwatch

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.MenuItem.MENUITEM_MARKET_INDEX
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.split.SplitScreen
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.menu.MenuActivity
import com.github.mikephil.charting.charts.CombinedChart
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
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_market_watch_index.*
import kotlinx.android.synthetic.main.act_market_watch_index.chart_trade
import kotlinx.android.synthetic.main.act_market_watch_index.iv_close_1
import kotlinx.android.synthetic.main.act_market_watch_index.iv_close_2
import kotlinx.android.synthetic.main.act_market_watch_index.iv_close_3
import kotlinx.android.synthetic.main.act_market_watch_index.iv_go_setting
import kotlinx.android.synthetic.main.act_market_watch_index.ll_ma_1
import kotlinx.android.synthetic.main.act_market_watch_index.ll_ma_2
import kotlinx.android.synthetic.main.act_market_watch_index.ll_ma_3
import kotlinx.android.synthetic.main.act_market_watch_index.tv_deal_price
import kotlinx.android.synthetic.main.act_market_watch_index.tv_deal_price_diff
import kotlinx.android.synthetic.main.act_market_watch_index.tv_deal_price_diff_percent
import kotlinx.android.synthetic.main.act_market_watch_index.tv_ma_1
import kotlinx.android.synthetic.main.act_market_watch_index.tv_ma_2
import kotlinx.android.synthetic.main.act_market_watch_index.tv_ma_3
import kotlinx.android.synthetic.main.act_market_watch_index.tv_week
import kotlinx.android.synthetic.main.act_market_watch_index.v_deal_up_down
import kotlinx.android.synthetic.main.act_market_watch_index.v_deal_zero
import kotlinx.android.synthetic.main.chart_index_marker.view.*
import kotlinx.android.synthetic.main.chart_index_tab_name.view.*
import kotlinx.android.synthetic.main.item_range_bar.view.*
import kotlinx.android.synthetic.main.toolbar_common.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt


@RequiresActivityViewModel(value = MarketWatchIndexViewModel::class)
class MarketWatchIndexActivity : BaseActivity<MarketWatchIndexViewModel>(), OnChartValueSelectedListener, OnChartGestureListener {

    private lateinit var selectedIndex: MarketIndexList
    private lateinit var indexList: List<MarketIndexList>
    private lateinit var tfRegular: Typeface
    private lateinit var tfLight: Typeface
    private lateinit var tfBold: Typeface
    private lateinit var tfExtraBold: Typeface
    private lateinit var splitScreen: SplitScreen

    private  var pageList: ArrayList<String> = ArrayList()

    private var isInitChart: Boolean = false

    private var gson: Gson = Gson()

    private val volumeChartRatio = 1.5f
    private var chartZeroOffset = 0.15f
    private var chartOffset = 1 - chartZeroOffset

    private var dataList: ArrayList<MarketIndexChartList.Items> = ArrayList()

    private var chartSetting: Chart = Chart(storeId = this.javaClass.simpleName)

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_market_watch_index)
        (application as App).component.inject(this)

        window.statusBarColor = getColor(R.color.color_1d1d1d)

        initToolbar()
        initChart()
        loadIndexList()

        initChartSetting()
        initView()
        updateMaUi()

        val cal = Calendar.getInstance()
        cal.time = Date()


        Timber.d("today week = ${SimpleDateFormat("yyyyww").format(cal.time)}")

    }

    private fun loadIndexList() {

        viewModel.outPuts.onSuccessRequestIndexList()
                .bindToLifecycle(this)
                .subscribe {
                    // 여기서 받은 index 리스트를 화면에 보여준다
                    updateIndexTab(it)
                }
        viewModel.inPuts.requestIndexList(Parameter.EVENT)
    }

    private fun updateIndexTab(it: List<MarketIndexList>?) {

        indexList = it!!
        for(data in indexList) {
            data.idxCd.let { it1 -> pageList.add(it1) }
        }

        tabLayout.removeAllTabs()
        for (i in pageList.indices) {
            tabLayout.addTab(tabLayout.newTab().setText(pageList[i]))
            val layout = ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
            val params = layout.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = params
        }
    }

    private fun initChartSetting() {

        viewModel.outPuts.onSuccessLoadChartSetting()
                .bindToLifecycle(this)
                .subscribe {
                    updateChartSetting(it)
                    updateRangeBtnLabel()
                }
        viewModel.outPuts.onFailLoadChartSetting()
                .bindToLifecycle(this)
                .subscribe {
                    chartSetting = Chart(this.javaClass.simpleName)
                    chartSetting.chartType = getString(R.string.market_watch_chart_line)
                    if(::selectedIndex.isInitialized)
                        selectedIndex.let {  chartSetting.interVal = selectedIndex.intervalItem?.minBy { it.intervalSeq }?.interval }
                    else
                        chartSetting.interVal = resources.getStringArray(R.array.chart_index_interval).first()
                    chartSetting.intervalList?.addAll( resources.getStringArray(R.array.chart_index_interval))
                    chartSetting.movingAverage = ArrayList()
                    updateData()
                    updateRangeBtnLabel()
                }
        viewModel.inPuts.loadChartSetting(this.javaClass.simpleName)

    }

    private fun initView() {
        mView = layoutInflater.inflate(R.layout.chart_index_marker, null)

        iv_go_setting.setSafeOnClickListener {

            val intent = Intent(this, MarketWatchSettingActivity::class.java)
            intent.putExtra("type", MarketWatchSettingActivity.WatchUiType.CHART_SETING_MA)
            intent.putExtra(Intents.MARKET_WATCH_CHART_SETTING, chartSetting)
            startActivityForResult(intent, PICK_CHART_SETTING_REQUEST)
        }

        tabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {unselectedTab ->
                    getFont(unselectedTab.parent!!.context, R.font.opensans_regular).also {
                        it?.let { setTabTitleTypeface(unselectedTab.position, it) }
                    }
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {selectedTab ->
                    getFont(selectedTab.parent!!.context, R.font.opensans_extrabold).also {
                        it?.let {setTabTitleTypeface(selectedTab.position, it)}
                    }

                    selectedIndex = indexList[selectedTab.position]
                    /*if(!selectedIndex.intervalItem!!.contains(chartSetting.interVal))
                        chartSetting.interVal = selectedIndex.intervalItem!![0]*/
                    requestChart(selectedTab.text.toString())

                    val rect = Rect()
                    hsv_index.getLocalVisibleRect(rect)
                    val calWidth = hsv_index.width - (selectedTab.parent!!.width - selectedTab.view.left)


                    if(0 <  calWidth)
                        v_buffer_end.layoutParams = LinearLayout.LayoutParams(calWidth, ViewGroup.LayoutParams.MATCH_PARENT)
                    else
                        v_buffer_end.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)

                    Handler().postDelayed({
                        hsv_index.scrollX = selectedTab.view.left

                    },100)
                    selectedIndex.idxNm?.let { showIndexNamePopup(it) }

                }
            }

        })

        for (i in pageList.indices) {
            tabLayout.addTab(tabLayout.newTab().setText(pageList[i]))
            val layout = ((tabLayout.getChildAt(0) as LinearLayout).getChildAt(i) as LinearLayout)
            val params = layout.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layout.layoutParams = params
        }


        iv_close_1.setSafeOnClickListener {
            chartSetting.movingAverage?.get(0)?.selected = false
            viewModel.inPuts.storeChartSetting(chartSetting)
        }

        iv_close_2.setSafeOnClickListener {
            chartSetting.movingAverage?.get(1)?.selected = false
            viewModel.inPuts.storeChartSetting(chartSetting)
        }

        iv_close_3.setSafeOnClickListener {
            chartSetting.movingAverage?.get(2)?.selected = false
            viewModel.inPuts.storeChartSetting(chartSetting)
        }
        btn_3m.setOnClickListener {
            changeRange(btn_3m)
            updateButtonUi(it)
        }
        btn_6m.setOnClickListener {
            changeRange(btn_6m)
            updateButtonUi(it)
        }
        btn_1y.setOnClickListener {
            changeRange(btn_1y)
            updateButtonUi(it)
        }
        btn_all.setOnClickListener {
            changeRange(btn_all)
            updateButtonUi(it)
        }
    }

    private fun updateRangeBtnLabel() {
        when(chartSetting.interVal) {
            getString(R.string.market_index_chart_yearly) -> {
                btn_3m.text = "3Y"
                btn_6m.text = "6Y"
                btn_1y.text = "9Y"
            }
            else -> {
                btn_3m.text = "3M"
                btn_6m.text = "6M"
                btn_1y.text = "1Y"
            }
        }
    }

    private fun requestChart(idxcode: String) {
        chart_trade.clear()
        val index = indexList.find { it.idxCd == idxcode }
        index?.let {
            val req = PostMarketIndexChartRequest(it.idxSubject,it.idxCategory,it.idxCd,
                    if(!it.intervalItem!!.none { it.interval == chartSetting.interVal })
                        chartSetting.interVal
                    else
                        it.intervalItem!!.minBy { it.intervalSeq }?.interval)
            viewModel.inPuts.requestChart(req)
        }
    }

    var startRange: Int = 2
    var endRange: Int = 4
    private fun changeRange(btn: Button) {
        var range: Int = 0
        when(btn){
            btn_3m -> {range = 3}
            btn_6m -> {range = 6}
            btn_1y -> {range = 12}
            btn_all -> { range = -1}
        }

        if(range == -1){
            startRange = 0
            endRange = dataList.size
            rangeBar.setSelectedEntries(startRange,endRange)
            return

        }

        when(chartSetting.interVal) {
            getString(R.string.market_index_chart_daily) -> {range *= 4 * 7}
            getString(R.string.market_index_chart_weekly) -> {range *= 4}
            getString(R.string.market_index_chart_monthly) -> {}
            getString(R.string.market_index_chart_yearly) -> {}
        }

        val diff = range - (endRange - startRange +1 )

        if(diff > 0){
            for (i in diff downTo 1) {
                if(endRange < dataList.size){
                    endRange++
                }else {
                    startRange--
                }

            }
        } else if (diff < 0) {
            for (i in diff .. -1) {
                endRange--

            }
        }
        if(startRange < 0)
            startRange = 0

        rangeBar.setSelectedEntries(startRange,endRange)

    }

    private fun updateButtonUi(it: View) {
        btn_3m.typeface = getFont(this, R.font.opensans_regular)
        btn_6m.typeface = getFont(this, R.font.opensans_regular)
        btn_1y.typeface = getFont(this, R.font.opensans_regular)
        btn_all.typeface = getFont(this, R.font.opensans_regular)
        btn_3m.setTextColor(getColor(R.color.greyish_brown))
        btn_6m.setTextColor(getColor(R.color.greyish_brown))
        btn_1y.setTextColor(getColor(R.color.greyish_brown))
        btn_all.setTextColor(getColor(R.color.greyish_brown))
        btn_3m.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        btn_6m.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        btn_1y.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        btn_all.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        it.background = getDrawable(R.drawable.bg_round_corner_12d5_bfbfbf)
        (it as Button).typeface = getFont(this, R.font.opensans_extrabold)
        it.setTextColor(getColor(R.color.color_141414))

    }
    private fun resetButton() {
        btn_3m.typeface = getFont(this, R.font.opensans_regular)
        btn_6m.typeface = getFont(this, R.font.opensans_regular)
        btn_1y.typeface = getFont(this, R.font.opensans_regular)
        btn_all.typeface = getFont(this, R.font.opensans_regular)
        btn_3m.setTextColor(getColor(R.color.greyish_brown))
        btn_6m.setTextColor(getColor(R.color.greyish_brown))
        btn_1y.setTextColor(getColor(R.color.greyish_brown))
        btn_all.setTextColor(getColor(R.color.greyish_brown))
        btn_3m.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        btn_6m.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        btn_1y.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
        btn_all.background = getDrawable(R.drawable.bg_round_corner_12d5_greyishbrown_border)
    }

    private fun updateChartSetting(chart: Chart) {

        if(!chartSetting.interVal.equals(chart.interVal)) {
            chartSetting = chart
            if(::selectedIndex.isInitialized)
                requestChart(selectedIndex.idxCd)
            return
        }
        if (!::selectedIndex.isInitialized)
            return

        chartSetting = chart

        if(selectedIndex.intervalItem!!.none { it.interval == chartSetting.interVal})
            chartSetting.interVal = selectedIndex.intervalItem!!.minBy{it.intervalSeq}?.interval

        val intervalList = ArrayList<String>()
        for(data in selectedIndex.intervalItem!!.sortedBy { it.intervalSeq }){
            intervalList.add(data.interval)
        }
        chartSetting.intervalList = intervalList
        updateData()
        updateMaUi()

    }

    private fun updateMaUi() {
        for((index, data) in chartSetting.movingAverage?.withIndex()!!){
            when(index) {
                0 -> {
                    if(data.selected!!) ll_ma_1.visibility = View.VISIBLE else ll_ma_1.visibility = View.GONE
                    tv_ma_1.text = "MA ${data.peroid.toString()}"
                }
                1 -> {
                    if(data.selected!!) ll_ma_2.visibility = View.VISIBLE else ll_ma_2.visibility = View.GONE
                    tv_ma_2.text = "MA ${data.peroid.toString()}"
                }
                2 -> {
                    if(data.selected!!) ll_ma_3.visibility = View.VISIBLE else ll_ma_3.visibility = View.GONE
                    tv_ma_3.text = "MA ${data.peroid.toString()}"
                }
            }
        }
    }

    fun setTabTitleTypeface(position: Int, type: Typeface){
        val tabLayout : LinearLayout = ((tabLayout.getChildAt(0) as ViewGroup)).getChildAt(position) as LinearLayout
        val tabTextView: TextView = tabLayout.getChildAt(1) as TextView
        tabTextView.typeface = type
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                PICK_CHART_SETTING_REQUEST -> {
                    if (data != null) {
                        val setting = data.getSerializableExtra(Intents.MARKET_WATCH_CHART_SETTING) as Chart
                        viewModel.inPuts.storeChartSetting(setting)
                    }
                }
            }
        }
    }

    private fun initToolbar() {
        toolbar_common.setBackgroundColor(getColor(R.color.color_1d1d1d))

        // set custom toolbar
        defaultbarInit(toolbar_common, menuType = MenuType.DEFAULT, title = "Market Index", isEnableNavi = false)

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
                    startMenuActivity(MENUITEM_MARKET_INDEX, MenuActivity::class.java)
                }

    }

    @SuppressLint("SimpleDateFormat")
    private fun initChart() {

        viewModel.outPuts.onSuccessRequestChartList()
                .bindToLifecycle(this)
                .subscribe{
                    dataList.clear()
                    if(it.weekItems != null){
                        dataList.addAll(it.weekItems.sortedBy { items ->  items.intervalStamp })
                    }
                    updateData()
                }
        chart_trade.description.isEnabled = false
        chart_trade.setBackgroundColor(baseContext?.let { ContextCompat.getColor(it, R.color.color_0d0d0d) }!!)
        chart_trade.setDrawGridBackground(false)
        chart_trade.xAxis.setDrawGridLines(false)
        chart_trade.axisLeft.setDrawGridLines(false)
        chart_trade.axisRight.setDrawGridLines(false)
        chart_trade.isAutoScaleMinMaxEnabled = true
        chart_trade.setPinchZoom(false)
        chart_trade.isScaleXEnabled = false
        chart_trade.isScaleYEnabled = false
        chart_trade.isDragDecelerationEnabled = false
        chart_trade.isDragEnabled = false
        chart_trade.setDrawBarShadow(false)
        chart_trade.legend.isEnabled = false
        chart_trade.isHighlightFullBarEnabled = false
        chart_trade.extraBottomOffset = 25f

        chart_trade.drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE)

        val rightAxis = chart_trade.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = getFont(baseContext!!, R.font.opensans_bold)!!
        rightAxis.textColor = ContextCompat.getColor(baseContext!!, R.color.greyish_brown)
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
        xAxis.mLabelHeight = 100
        //xAxis.setLabelCount(3, true)
        xAxis.typeface = getFont(baseContext!!, R.font.opensans_bold)!!
        xAxis.textColor = ContextCompat.getColor(baseContext!!, R.color.greyish_brown)
        xAxis.textSize = 13f
        xAxis.mLabelWidth = 30
        xAxis.setMultiLineLabel(true)
        xAxis.granularity = 1f
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = getColor(R.color.color_1a1a1a)

        chart_trade.setOnChartValueSelectedListener(this)
    }

    /*private fun makeData() {
        dataList.clear()

        for (i in 0..200) {
            val low: Float = getRandom(500f,10f)
            val high: Float = getRandom(500f,low)
            val open: Float = getRandom(high - low,low)
            val deal: Float = getRandom(high - low,low)

            dataList.add(MarketIndexChartList.Items("201901", deal.toDouble(),
                    getRandom(1200f,100f).toInt(),
                    low.toDouble(),
                    high.toDouble()
            ))
        }
        updateData()
    }*/

    private fun makeMaData(period: Int, offset: Int): ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        for ((index, data) in dataList.withIndex()) {
            if(index+1 >= period /*+ offset*/) {
                val price = dataList.subList(index+1 -period,index+1).sumByDouble { it.value }/period
                if(index +1f + offset <= dataList.size)
                    entries.add(Entry(index+1f + offset, price.toFloat()))
            }
        }
        return entries

    }
    private fun updateRangebar() {


        val rangeBarEntries = ArrayList<BarEntry>()
        for((index,data) in dataList.withIndex())
            rangeBarEntries.add(BarEntry(index.toFloat()+1, data.value.toFloat()))

        rangeBar.onRangeChanged = { leftPinValue, rightPinValue ->
            startRange = rangeBar.elementRangeBar.start+1
            endRange = rangeBar.elementRangeBar.end+1
            changeChartVisible()
        }
        rangeBar.onLeftPinChanged = { index, leftPinValue ->
            startRange = index +1
        }
        rangeBar.onRightPinChanged = { index, rightPinValue ->
            endRange = index +1
        }
        rangeBar.onSelectedEntriesSizeChanged = { selectedEntriesSize ->
            startRange = rangeBar.elementRangeBar.start
            endRange = rangeBar.elementRangeBar.end
            changeChartVisible()
            resetButton()
        }
        rangeBar.onSelectedItemsSizeChanged = { selectedItemsSize ->
        }


        rangeBar.setEntries(rangeBarEntries)
        startRange = 0
        endRange = dataList.size
        rangeBar.setSelectedEntries(startRange,endRange)
        rangeBar.elementRangeBar.requestLayout()

    }

    private fun changeChartVisible() {
        if (startRange == endRange)
            return

        chart_trade.zoom(((dataList.size.toFloat())/(endRange-startRange + 0.26f))/chart_trade.scaleX,0f,0f,0f)
        chart_trade.moveViewToX(startRange.toFloat()-0.24f)
    }

    private fun updateData() {
        if(this.dataList.isNullOrEmpty()) {
            if(chart_trade.data != null) {

                chart_trade.clear()
            }
            return
        }
        //chart_trade.moveViewToX(0.76f)

        chart_trade.drawOrder = arrayOf(
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        )

        val data = CombinedData()
        when(chartSetting.chartType) {
            getString(R.string.market_watch_chart_mountain) -> {
                data.setData(generateMountainData(dataList,false))
            }
            else -> {
                data.setData(generateLineData(dataList, getColor(R.color.color_chart_mountain_line), false, false))

            }
        }
        data.setData(generateVolumeData(dataList))

        chart_trade.xAxis.axisMaximum = data.xMax + 0.25f
        chart_trade.xAxis.axisMinimum = data.xMin - 0.25f
        //거래량 차트 y 최대값
        val yAxisLeft = chart_trade.getAxis(YAxis.AxisDependency.LEFT)
        val max = dataList.maxBy { it.volume }
        var ymax = max!!.volume.toFloat()/volumeChartRatio *10
        if(ymax >= 0)
            ymax = 100f
        yAxisLeft.axisMaximum = ymax
        val yAxisRight = chart_trade.getAxis(YAxis.AxisDependency.RIGHT)
        chart_trade.axisLeft.spaceTop = 100f

        when(chartSetting.interVal) {
            getString(R.string.market_index_chart_daily) -> { chart_trade.xAxis.valueFormatter = XAxisDayFormatter()
            }
            getString(R.string.market_index_chart_weekly) -> { chart_trade.xAxis.valueFormatter = XAxisWeekFormatter()
            }
            getString(R.string.market_index_chart_monthly) -> { chart_trade.xAxis.valueFormatter = XAxisMonthFormatter()
            }
            getString(R.string.market_index_chart_yearly) -> { chart_trade.xAxis.valueFormatter = XAxisYearFormatter(data.xMax.toInt()) }
        }

        chart_trade.axisRight.valueFormatter = RightAxisFormatter()

        //chart_trade.zoom(1f, 0f, 0f, 0f)

        chart_trade.data = data

        for(renderer in (chart_trade.renderer as CombinedChartRenderer).subRenderers) {
            when(renderer) {
                is ExtentionLineChartRenderer -> { renderer.mDrawFillOffsetPerBottom = chartZeroOffset}
            }
        }
        yAxisRight.axisMinimum = -(chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum*chartZeroOffset/chartOffset)

        chart_trade.notifyDataSetChanged()
        if(dataList.size > 0) {
            //chart_trade.highlightValue(chart_trade.highlighter.getHighlight(dataList.size.toFloat(), dataList.last().value.toFloat()))
            chart_trade.highlightValue(null)
        }
        chart_trade.invalidate()
        chart_trade.moveViewToX(data.xMax)

        updateCard(this.dataList.last())

        updateRangebar()
        updateButtonUi(btn_all)
    }

    private fun generateVolumeData(dataList: ArrayList<MarketIndexChartList.Items>): BarData {
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

    private fun generateLineData(dataList: ArrayList<MarketIndexChartList.Items>, color: Int, useValueLabel: Boolean, onlyMA: Boolean): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()

        if(onlyMA.not()) {
            for ((index, data) in dataList.withIndex()) {

                entries.add(Entry(index + 1f,
                        data.value.toFloat(), data))
                data.xVal = index + 1f
            }
            val set = LineDataSet(entries, "Line DataSet")
            set.axisDependency = YAxis.AxisDependency.RIGHT
            set.valueTextSize = 20f
            set.valueTypeface = getFont(this, R.font.opensans_bold)
            set.color = color
            set.lineWidth = 1f
            set.highLightColor = getColor(R.color.white)
            set.enableDashedHighlightLine(Utils.convertDpToPixel(1f), 10f, 0f)
            set.isDrawValueLabel = useValueLabel
            set.setDrawCircles(false)
            set.setDrawValues(false)
            d.addDataSet(set)
        }

        for((index, data) in chartSetting.movingAverage?.withIndex()!!) {
            if(data.selected?.not()!!)
                continue
            d.addDataSet(makeMovingAverageData(data))
        }
        return d

    }

    private fun makeMovingAverageData(data: MovingAverage): LineDataSet {
        val set = LineDataSet(makeMaData(data.peroid!!.toInt(), data.offset!!.toInt()), "Line DataSet")
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.valueTextSize = 20f
        set.valueTypeface = getFont(this, R.font.opensans_bold)
        set.color = data.color
        set.lineWidth = 0.5f
        set.isDrawValueLabel = true
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.isHighlightEnabled = false
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        return set
    }
    private fun generateMountainData(dataList: ArrayList<MarketIndexChartList.Items>, useValueLabel: Boolean): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()

        for ((index, data) in dataList.withIndex()) {

            entries.add(Entry(index + 1f,
                    data.value.toFloat(),data))
            data.xVal = index + 1f
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
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(240, 238, 70)
        set.valueTypeface = getFont(baseContext!!, R.font.opensans_bold)
        set.isDrawValueLabel = useValueLabel
        d.addDataSet(set)

        for(data in chartSetting.movingAverage!!) {
            if(data.selected?.not()!!)
                continue
            d.addDataSet(makeMovingAverageData(data))
        }

        return d
    }

    inner class XAxisDayFormatter : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            var label = ""
            var cal = Calendar.getInstance()
            var data : MarketIndexChartList.Items
            try {

                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalStamp.toDate("yyyyMMdd")
                label = "${data.intervalStamp.substring(4,6)}-${data.intervalStamp.substring(6,8)}"
                if(cal.get(Calendar.DAY_OF_YEAR) == 1)
                    label+= "\n${data.intervalStamp.substring(0,4)}"
            }catch (e:Exception) { }
            return label
        }
    }

    inner class XAxisWeekFormatter : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            var label = ""
            var cal = Calendar.getInstance()
            cal.minimalDaysInFirstWeek = 4
            cal.firstDayOfWeek = Calendar.MONDAY

            var data : MarketIndexChartList.Items
            try {

                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalStamp.toDate("yyyyMMddHHmmssSSSSSS")
                label = "W${String.format("%02d",cal.get(Calendar.WEEK_OF_YEAR))}"
                if(cal.get(Calendar.WEEK_OF_YEAR) == 1) {

                    label += "\n${cal.weekYear}"
                }

            }catch (e:Exception) { }
            return label
        }
    }

    inner class XAxisMonthFormatter : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            var label = ""
            val cal = Calendar.getInstance()
            val data : MarketIndexChartList.Items
            try {

                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalStamp.toDate("yyyyMMddHHmmssSSSSSS")
                label = SimpleDateFormat("MMM").format(cal.time)
                if(cal.get(Calendar.MONTH) == 0)
                    label+= "\n${data.intervalStamp.substring(0,4)}"

            }catch (e:Exception) { }
            return label
        }
    }
    inner class XAxisYearFormatter(count: Int) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            var label = ""
            val cal = Calendar.getInstance()
            val data : MarketIndexChartList.Items
            try {

                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalStamp.toDate("yyyyMMddHHmmssSSSSSS")
                label = SimpleDateFormat("yyyy").format(cal.time)

            }catch (e:Exception) { }
            return label
        }
    }
    inner class RightAxisFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value >0) DecimalFormat("#,###").format(value.toLong()) else ""
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null) {
            updateCard(e.data as MarketIndexChartList.Items)
            showMarkerView(e?.data as MarketIndexChartList.Items)
        }
    }
    var mPosX : Int = 0
    var mPosY : Int = 0
    lateinit var mPopupWindow : PopupWindow
    lateinit var mView : View
    @SuppressLint("ClickableViewAccessibility")
    private fun showMarkerView(data: MarketIndexChartList.Items) {
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.minimumFractionDigits = 0
        mView.tv_marker_dealdate.text = data.xAxis
        mView.tv_marker_price.text = "${currencyFormat.format(data.value.toInt())}/T"
        if (::mPopupWindow.isInitialized.not()) {
            mPopupWindow = PopupWindow(mView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false)
        }
        val location = IntArray(2)
        rl_chart_container!!.getLocationOnScreen(location)
        mPosY = location[1]
        mPosX = location[0]
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.showAtLocation(rl_chart_container, Gravity.NO_GRAVITY, mPosX, mPosY)


        val limitLocation = IntArray(2)
        rl_chart_container!!.getLocationOnScreen(limitLocation)
        var dx = 0
        var dy = 0
        var origX = 0
        var origY = 0
        mView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    origX = event.rawX.toInt()
                    origY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    dx = (event.rawX - origX).toInt()
                    dy = (event.rawY - origY).toInt()
                    mPopupWindow.update(mPosX + dx,
                            if(limitLocation[1] > (mPosY + dy)){
                                limitLocation[1] }
                            else if ((limitLocation[1]+rl_chart_container.height) < (mPosY + dy + mView.height)){
                                limitLocation[1] + rl_chart_container.height - mView.height
                            }
                            else{
                                mPosY + dy},
                            -1,-1,true)
                }
                MotionEvent.ACTION_UP -> {
                    mPosX += dx
                    mPosY += dy

                }
            }
            true
        }
    }
    private fun showIndexNamePopup(indexName: String) {
        val view = layoutInflater.inflate(R.layout.chart_index_tab_name, null)
        val popupwindow = PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false)
        view.tv_index_name.text = indexName

        val location = IntArray(2)
        cl_detail!!.getLocationOnScreen(location)

        popupwindow.isOutsideTouchable = true
        popupwindow.showAtLocation(cl_detail, Gravity.NO_GRAVITY, Utils.convertDpToPixel(16f).toInt(), location[1])

    }

    private fun updateCard(data: MarketIndexChartList.Items) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.minimumFractionDigits = 0

        tv_week.text = data.intervalStamp
        val cal = Calendar.getInstance()
        cal.minimalDaysInFirstWeek = 4
        cal.firstDayOfWeek = Calendar.MONDAY

        cal.time = data.intervalStamp.toDate("yyyyMMddHHmmssSSSSSS")
        tv_week.text = "Week ${String.format("%02d",cal.get(Calendar.WEEK_OF_YEAR))}, ${cal.weekYear}"

        tv_deal_price.text = currencyFormat.format(data.value.toInt())
        when {
            (data.changeValue) > 0 -> {
                v_deal_zero.visibility = View.INVISIBLE
                v_deal_up_down.visibility = View.VISIBLE
                v_deal_up_down.background = getDrawable(R.drawable.ic_up_green)
                tv_deal_price_diff.setTextColor(getColor(R.color.green_blue))
                tv_deal_price_diff_percent.setTextColor(getColor(R.color.green_blue))
            }
            (data.changeValue) < 0 -> {
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
        tv_deal_price_diff.text = currencyFormat.format(abs(data.changeValue).toInt())
        tv_deal_price_diff_percent.text = "(${"%.2f".format(abs(data.changeRate))}%)"
        tv_volume.text = "${data.volume}T"

    }

    override fun onNothingSelected() {}

    private fun refreshCandleRange() {
        if(!dataList.isNullOrEmpty()) {
            if(abs(chart_trade.xChartMax/chart_trade.scaleX -(chart_trade.highestVisibleX - chart_trade.lowestVisibleX)) > 1)
                return

            var minX = floor(chart_trade.lowestVisibleX)
            var maxX = chart_trade.highestVisibleX.roundToInt().toFloat()
            if(minX < 0)
                minX = 0f
            if(maxX > dataList.size)
                maxX = dataList.size.toFloat()

            if(minX >= maxX)
                return
            val max = dataList.subList(minX.toInt(), maxX.toInt())
                    .maxBy { it.value }!!.value

            val min = -(max) / (10-volumeChartRatio*2) * volumeChartRatio*2
            if (!(chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum == max.toFloat() && chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMinimum == min.toFloat())) {
                chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum = max.toFloat()
                chart_trade.getAxis(YAxis.AxisDependency.RIGHT).axisMinimum = min.toFloat()
                chart_trade.invalidate()
            }
        }
    }

    companion object {
        const val PICK_CHART_SETTING_REQUEST = 1

    }

    override fun onChartGestureEnd(p0: MotionEvent?, p1: ChartTouchListener.ChartGesture?) {
//        refreshCandleRange()
    }

    override fun onChartFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float) {
    }

    override fun onChartSingleTapped(p0: MotionEvent?) {
    }

    override fun onChartGestureStart(p0: MotionEvent?, p1: ChartTouchListener.ChartGesture?) {
    }

    override fun onChartScale(p0: MotionEvent?, p1: Float, p2: Float) {

    }

    override fun onChartLongPressed(p0: MotionEvent?) {
    }

    override fun onChartDoubleTapped(p0: MotionEvent?) {
    }

    override fun onChartTranslate(p0: MotionEvent?, p1: Float, p2: Float) {
//        refreshCandleRange()
    }
}
