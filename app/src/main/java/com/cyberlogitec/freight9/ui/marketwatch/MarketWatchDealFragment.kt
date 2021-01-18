package com.cyberlogitec.freight9.ui.marketwatch

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.res.ResourcesCompat.getFont
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Chart
import com.cyberlogitec.freight9.lib.model.MarketWatchProductWeekDetailChartList
import com.cyberlogitec.freight9.lib.model.MovingAverage
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.toDate
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
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_market_watch.iv_close_1
import kotlinx.android.synthetic.main.act_market_watch.iv_close_2
import kotlinx.android.synthetic.main.act_market_watch.iv_close_3
import kotlinx.android.synthetic.main.act_market_watch.iv_go_setting
import kotlinx.android.synthetic.main.act_market_watch.ll_ma_1
import kotlinx.android.synthetic.main.act_market_watch.ll_ma_2
import kotlinx.android.synthetic.main.act_market_watch.ll_ma_3
import kotlinx.android.synthetic.main.act_market_watch.tv_ma_1
import kotlinx.android.synthetic.main.act_market_watch.tv_ma_2
import kotlinx.android.synthetic.main.act_market_watch.tv_ma_3
import kotlinx.android.synthetic.main.chart_marker.view.*
import kotlinx.android.synthetic.main.frag_market_week_deal.candle_chart
import kotlinx.android.synthetic.main.frag_watch_deal_chart.rl_chart_container
import timber.log.Timber
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

class MarketWatchDealFragment constructor(val viewModel: BaseViewModel): RxFragment(), OnChartValueSelectedListener, OnChartGestureListener {

    private val volumeChartRatio = 1.5f
    private var chartZeroOffset = 0.15f
    private var chartOffset = 1 - chartZeroOffset

    private var chartSetting: Chart = Chart(storeId = this.javaClass.simpleName)
    private var dataList: ArrayList<MarketWatchProductWeekDetailChartList.WeekItems> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_watch_deal_chart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        initView()
        initListener()
        initChartSetting()

        chartInitialize()

    }

    private fun initListener() {
        when(viewModel) {
            is MarketWatchViewModel -> {
                viewModel.outPuts.onSuccessRequestWeekChartList()
                        .bindToLifecycle(this)
                        .subscribe {
                            this.dataList.clear()
                            if(it.cells != null)
                                this.dataList.addAll(it.cells.sortedBy { it.intervalTimestamp})
                            updateData()
                        }
                viewModel.outPuts.refreshToBaseweek()
                        .bindToLifecycle(this)
                        .subscribe{
                        }
            }
        }
    }

    private fun initView() {
        mView = layoutInflater.inflate(R.layout.chart_marker, null)

        iv_go_setting.setSafeOnClickListener {

            val intent = Intent(context!!, MarketWatchSettingActivity::class.java)
            intent.putExtra("type", MarketWatchSettingActivity.WatchUiType.CHART_SETING_MA)
            intent.putExtra(Intents.MARKET_WATCH_CHART_SETTING, chartSetting)
            activity!!.startActivityForResult(intent, MarketWatchActivity.PICK_CHART_SETTING_REQUEST)
        }

        iv_close_1.setSafeOnClickListener {
            chartSetting.movingAverage?.get(0)?.selected = false
            (viewModel as MarketWatchViewModel).inPuts.storeChartSetting(chartSetting)
        }

        iv_close_2.setSafeOnClickListener {
            chartSetting.movingAverage?.get(1)?.selected = false
            (viewModel as MarketWatchViewModel).inPuts.storeChartSetting(chartSetting)
        }

        iv_close_3.setSafeOnClickListener {
            chartSetting.movingAverage?.get(2)?.selected = false
            (viewModel as MarketWatchViewModel).inPuts.storeChartSetting(chartSetting)
        }
    }

    private fun updateChartSetting(chart: Chart) {
        chartSetting = chart
        updateData()
        updateMaUi()
    }

    @SuppressLint("SetTextI18n")
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

    private fun initChartSetting() {
        when(viewModel) {
            is MarketWatchViewModel -> {
                viewModel.outPuts.onSuccessLoadChartSetting()
                        .bindToLifecycle(this)
                        .subscribe {
                            updateChartSetting(it)
                        }
                viewModel.outPuts.onFailLoadChartSetting()
                        .bindToLifecycle(this)
                        .subscribe {
                            chartSetting = Chart(this.javaClass.simpleName)
                            chartSetting.chartType = getString(R.string.market_watch_chart_candle)
                            chartSetting.interVal = getString(R.string.market_watch_chart_1week)
                            chartSetting.movingAverage = ArrayList()
                            updateData()
                        }
                viewModel.inPuts.loadChartSetting(this.javaClass.simpleName)

            }
        }
    }

    /*private fun makeData() {
        dataList.clear()

        for (i in 0..110) {
            val low: Float = getRandom(500f,10f)
            val high: Float = getRandom(500f,low)
            val open: Float = getRandom(high - low,low)
            val deal: Float = getRandom(high - low,low)

            dataList.add(MarketWatchProductWeekDetailChartList.WeekItems("201901", "open",
                    open.toDouble(),
                    low.toDouble(),
                    high.toDouble(),
                    deal.toDouble(),
                    getRandom(1200f,100f).toDouble(),
                    10.0,
                    0.02,
                    Calendar.getInstance().time.toString()))
        }
    }*/
    private fun makeMaData(period: Int, offset: Int): ArrayList<Entry> {
        val entries = ArrayList<Entry>()
        for ((index, data) in dataList.withIndex()) {
            if(index+1 >= period /*+ offset*/) {
                val price = dataList.subList(index+1 -period,index+1).sumByDouble { it.close }/period
                if(index +1f + offset <= dataList.size)
                    entries.add(Entry(index+1f + offset, price.toFloat()))
            }
        }
        return entries

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.v("f9: onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("f9: onDestroy")

    }

    private fun chartInitialize(){

        candle_chart.description.isEnabled = false
        candle_chart.setBackgroundColor(context?.let { getColor(it, R.color.color_0d0d0d)}!!)
        candle_chart.setDrawGridBackground(false)
        candle_chart.xAxis.setDrawGridLines(false)
        candle_chart.axisLeft.setDrawGridLines(false)
        candle_chart.axisRight.setDrawGridLines(false)
        candle_chart.isAutoScaleMinMaxEnabled = true
        candle_chart.setPinchZoom(false)
        candle_chart.isScaleXEnabled = true
        candle_chart.isScaleYEnabled = false
        candle_chart.setDrawBarShadow(false)
        candle_chart.legend.isEnabled = false
        candle_chart.isHighlightFullBarEnabled = false
        candle_chart.extraBottomOffset = 25f

        candle_chart.drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE)

        val rightAxis = candle_chart.axisRight
        rightAxis.setDrawGridLines(false)
//        rightAxis.axisMinimum = 0f // this replaces setStartAt+Zero(true)
        rightAxis.typeface = getFont(context!!, R.font.opensans_bold)!!
        rightAxis.textColor = getColor(context!!,R.color.greyish_brown)
        rightAxis.textSize = 13f
        rightAxis.minWidth = 48f
        rightAxis.setDrawAxisLine(true)
        rightAxis.axisLineColor = getColor(context!!, R.color.color_1a1a1a)
//        rightAxis.setDrawZeroLine(true)

        val leftAxis = candle_chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.minWidth = 0f
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawAxisLine(false)

        val xAxis = candle_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.mLabelHeight = 100
//        xAxis.setLabelCount(3, true)
        xAxis.typeface = getFont(context!!, R.font.opensans_bold)!!
        xAxis.textColor = getColor(context!!, R.color.greyish_brown)
        xAxis.textSize = 13f
        xAxis.axisMinimum = 1f
        xAxis.mLabelWidth = 30
        xAxis.setMultiLineLabel(true)
        xAxis.granularity = 1f
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineColor = getColor(context!!, R.color.color_1a1a1a)

        candle_chart.setOnChartValueSelectedListener(this)
        candle_chart.onChartGestureListener = this

    }

    private fun updateData() {
        if(this.dataList.isNullOrEmpty()) {
            if(candle_chart.data != null) {

                candle_chart.clear()
            }
            return
        }
        candle_chart.moveViewToX(0f)

        candle_chart.drawOrder = arrayOf(
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        )

        val data = CombinedData()
        when(chartSetting.chartType) {
            getString(R.string.market_watch_chart_line) -> {
                data.setData(generateLineData(dataList, getColor(context!!, R.color.color_chart_mountain_line), useValueLabel = false, onlyMA = false))

            }
            getString(R.string.market_watch_chart_mountain) -> {
                data.setData(generateMountainData(dataList,false))
            }
            else -> {
                data.setData(generateLineData(dataList, getColor(context!!, R.color.color_chart_mountain_line), useValueLabel = false, onlyMA = true))
                data.setData(generateCandleData(dataList))
            }
        }
        data.setData(generateVolumeData(dataList))

        candle_chart.xAxis.axisMaximum = data.xMax + 0.25f
        candle_chart.xAxis.axisMinimum = 1 - 0.25f
        //거래량 차트 y 최대값
        val yAxisLeft = candle_chart.getAxis(YAxis.AxisDependency.LEFT)
        val max = dataList.maxBy { it.volume }
        yAxisLeft.axisMaximum = max!!.volume.toFloat()/volumeChartRatio *10
        val yAxisRight = candle_chart.getAxis(YAxis.AxisDependency.RIGHT)
        candle_chart.axisLeft.spaceTop = 100f

        when(chartSetting.interVal) {
            getString(R.string.market_watch_chart_1day) -> {
                val formatter = XAxisDayFormatter()
                candle_chart.xAxis.valueFormatter = formatter}
            getString(R.string.market_watch_chart_1week) -> {
                val formatter = XAxisWeekFormatter()
                candle_chart.xAxis.valueFormatter = formatter
            }
            getString(R.string.market_watch_chart_1month) -> {
                val formatter = XAxisMonthFormatter(data.xMax.toInt())
                candle_chart.xAxis.valueFormatter = formatter
            }
        }

        candle_chart.axisRight.valueFormatter = RightAxisFormatter()

        candle_chart.zoom(1f, 0f, 0f, 0f)

        candle_chart.data = data

        for(renderer in (candle_chart.renderer as CombinedChartRenderer).subRenderers) {
            when(renderer) {
                is ExtentionLineChartRenderer -> { renderer.mDrawFillOffsetPerBottom = chartZeroOffset}
            }
        }
        yAxisRight.axisMinimum = -(candle_chart.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum*chartZeroOffset/chartOffset)
        var offset = 0
        if(dataList.size < 10)
            offset = 10 - dataList.size
        candle_chart.zoom(((dataList.size.toFloat()+offset)/(10f))/candle_chart.scaleX,0f,0f,0f)
        candle_chart.moveViewToX(data.xMax)
        refreshCandleRange()

    }

    private fun generateVolumeData(dataList: ArrayList<MarketWatchProductWeekDetailChartList.WeekItems>): BarData {
        val entries = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()
        var offset = 0
        if(dataList.size < 10)
            offset = 10 - dataList.size
        for ((index, data) in dataList.withIndex()) {
            entries.add(BarEntry(index+1f + offset, data.volume.toFloat()))
            when(index) {
                0 -> {colors.add(getColor(context!!, R.color.color_chart_bar_volume_down))}
                else -> {
                    if(dataList[index-1].volume > data.volume)
                        colors.add(getColor(context!!, R.color.color_chart_bar_volume_down))
                    else
                        colors.add(getColor(context!!, R.color.color_chart_bar_volume_up))
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
    private fun generateCandleData(dataList: ArrayList<MarketWatchProductWeekDetailChartList.WeekItems>): CandleData {
        val d = CandleData()
        val entries = ArrayList<CandleEntry>()

        var offset = 0
        if(dataList.size < 10)
            offset = 10 - dataList.size

        for ((index, data) in dataList.withIndex()) {

            entries.add(CandleEntry(index + 1f + offset,
                    data.high.toFloat(),
                    data.low.toFloat(),
                    data.open.toFloat(),
                    data.close.toFloat(),data))
            data.xVal = index + 1f + offset
        }

        val set = CandleDataSet(entries, "Candle DataSet")
        set.decreasingColor = Color.rgb(0, 58, 255)
        set.increasingColor = Color.rgb(0, 179, 160)
        set.neutralColor = getColor(context!!, R.color.greyish_brown)
        set.highLightColor = getColor(context!!, R.color.white)
        set.color = getColor(context!!, R.color.color_chart_mountain_line)
        set.enableDashedHighlightLine(Utils.convertDpToPixel(1f),10f,0f)
        set.increasingPaintStyle = Paint.Style.FILL
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.shadowColor = Color.DKGRAY
        set.barSpace = 0.3f
        set.valueTextSize = 13f
        set.valueTextColor = getColor(context!!, R.color.color_141414)
        set.valueTypeface = getFont(context!!, R.font.opensans_extrabold)
        set.setDrawValues(false)
        d.addDataSet(set)

        return d
    }

    private var timestampList: ArrayList<String> = ArrayList()
    private fun generateLineData(dataList: ArrayList<MarketWatchProductWeekDetailChartList.WeekItems>, color: Int, useValueLabel: Boolean, onlyMA: Boolean): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()


        var offset = 0
        if(dataList.size < 10)
            offset = 10 - dataList.size

        if(onlyMA.not()) {
            for ((index, data) in dataList.withIndex()) {

                entries.add(Entry(index + 1f + offset,
                        data.close.toFloat(), data))
                data.xVal = index + 1f + offset
            }
            val set = LineDataSet(entries, "Line DataSet")
            set.axisDependency = YAxis.AxisDependency.RIGHT
            set.valueTextSize = 13f
            set.valueTextColor = getColor(context!!, R.color.color_141414)
            set.valueTypeface = getFont(context!!, R.font.opensans_extrabold)
            set.color = color
            set.lineWidth = 1f
            set.highLightColor = getColor(context!!, R.color.white)
            set.enableDashedHighlightLine(Utils.convertDpToPixel(1f), 10f, 0f)
            set.isDrawValueLabel = useValueLabel
            set.setDrawCircles(false)
            set.setDrawValues(false)
            d.addDataSet(set)
        }


        for(data in chartSetting.movingAverage!!) {
            if(data.selected?.not()!!)
                continue
            d.addDataSet(makeMovingAverageData(data))
        }


        return d

    }

    private fun makeMovingAverageData(data: MovingAverage): LineDataSet {
        val set = LineDataSet(makeMaData(data.peroid!!.toInt(), data.offset!!.toInt()), "Line DataSet")
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.valueTextSize = 13f
        set.valueTextColor = getColor(context!!, R.color.color_141414)
        set.valueTypeface = getFont(context!!, R.font.opensans_extrabold)
        set.color = data.color
        set.lineWidth = 0.5f
        set.isDrawValueLabel = true
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.isHighlightEnabled = false
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        return set
    }
    private fun generateMountainData(dataList: ArrayList<MarketWatchProductWeekDetailChartList.WeekItems>, useValueLabel: Boolean): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()

        var offset = 0
        if(dataList.size < 10)
            offset = 10 - dataList.size

        for ((index, data) in dataList.withIndex()) {

            entries.add(Entry(index + 1f + offset,
                    data.close.toFloat(),data))
            data.xVal = index + 1f + offset
        }
        val set = LineDataSet(entries, "Mountain DataSet")
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.valueTextSize = 20f
        set.color = getColor(context!!, R.color.color_chart_mountain_line)
        set.lineWidth = 1f
        set.setDrawFilled(true)
        set.fillDrawable = getDrawable(context!!, R.drawable.fade_chart_bfbfbf)
        set.highLightColor = getColor(context!!, R.color.white)
        set.enableDashedHighlightLine(Utils.convertDpToPixel(1f),10f,0f)
        set.setDrawCircles(false)
        set.setDrawValues(false)
        set.valueTextSize = 13f
        set.valueTextColor = getColor(context!!, R.color.color_141414)
        set.valueTypeface = getFont(context!!, R.font.opensans_extrabold)
        set.isDrawValueLabel = useValueLabel
        d.addDataSet(set)

        for(data in chartSetting.movingAverage!!) {
            if(data.selected?.not()!!)
                continue
            d.addDataSet(makeMovingAverageData(data))
        }

        return d
    }
    /*private fun getRandom(range: Float, start: Float): Float {
        return (Math.random() * range).toFloat() + start
    }*/
    inner class XAxisDayFormatter : ValueFormatter() {

        @SuppressLint("SimpleDateFormat")
        override fun getFormattedValue(value: Float): String {

            var label = ""
            val cal = Calendar.getInstance()
            val data : MarketWatchProductWeekDetailChartList.WeekItems
            try {
                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalTimestamp.toDate("yyyyMMddHHmmssSSSSSS")
                label = "${data.intervalTimestamp.substring(4,6)}-${data.intervalTimestamp.substring(6,8)}"
                if(cal.get(Calendar.DAY_OF_YEAR) == 1)
                    label+= "\n${SimpleDateFormat("yyyy").format(cal.time)}"
            }catch (e:Exception) { }
            return label

        }
    }

    inner class XAxisWeekFormatter : ValueFormatter() {

        @SuppressLint("SimpleDateFormat")
        override fun getFormattedValue(value: Float): String {

            var label = ""
            val cal = Calendar.getInstance()
            val data : MarketWatchProductWeekDetailChartList.WeekItems
            try {
                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalTimestamp.toDate("yyyyMMddHHmmssSSSSSS")
                label = "W${SimpleDateFormat("ww").format(cal.time)}"
            }catch (e:Exception) { }
            return label
        }

    }

    inner class XAxisMonthFormatter(count: Int) : ValueFormatter() {

        @SuppressLint("SimpleDateFormat")
        override fun getFormattedValue(value: Float): String {
            var label = ""
            val cal = Calendar.getInstance()
            val data : MarketWatchProductWeekDetailChartList.WeekItems
            try {
                data = dataList.find { it.xVal == value }!!
                cal.time = data.intervalTimestamp.toDate("yyyyMMddHHmmssSSSSSS")
                label = SimpleDateFormat("MMM").format(cal.time)
                if(cal.get(Calendar.MONTH) == 0)
                    label+= "\n${SimpleDateFormat("yyyy").format(cal.time)}"
            }catch (e:Exception) { }
            return label
        }
    }
    inner class RightAxisFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value >0) DecimalFormat("#,###").format(value.toLong()) else ""
        }
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(p0: Entry?, p1: Highlight?) {
        showMarkerView(p0?.data as MarketWatchProductWeekDetailChartList.WeekItems)
    }

    private var mPosX : Int = 0
    private var mPosY : Int = 0
    private lateinit var mPopupWindow : PopupWindow
    private lateinit var mView : View
    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat", "SetTextI18n")
    private fun showMarkerView(data: MarketWatchProductWeekDetailChartList.WeekItems) {
        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.minimumFractionDigits = 0
        val cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy.MM.dd")

        cal.time =data.intervalTimestamp.toDate("yyyyMMddHHmmssSSSSSS")
        mView.tv_marker_dealdate.text =  df.format(cal.time)
        mView.tv_marker_volume.text = "${data.volume.toInt()}T"
        mView.tv_marker_open.text = "${currencyFormat.format(data.open.toInt())}/T"
        mView.tv_marker_high.text = "${currencyFormat.format(data.high.toInt())}/T"
        mView.tv_marker_low.text = "${currencyFormat.format(data.low.toInt())}/T"
        mView.tv_marker_closed.text = "${currencyFormat.format(data.close.toInt())}/T"
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
                    mPopupWindow.update(mPosX + dx, if(limitLocation[1] > (mPosY + dy)) limitLocation[1] else mPosY + dy,-1,-1,true)
                }
                MotionEvent.ACTION_UP -> {
                    mPosX += dx
                    mPosY += dy

                }
            }
            true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketWatchDealFragment {
            return MarketWatchDealFragment(viewModel)
        }
    }

    override fun onChartGestureEnd(p0: MotionEvent?, p1: ChartTouchListener.ChartGesture?) {
        refreshCandleRange()
    }
    override fun onChartFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float) {}

    override fun onChartSingleTapped(p0: MotionEvent?) {}

    override fun onChartGestureStart(p0: MotionEvent?, p1: ChartTouchListener.ChartGesture?) {}

    override fun onChartScale(p0: MotionEvent?, p1: Float, p2: Float) {}

    override fun onChartLongPressed(p0: MotionEvent?) {}

    override fun onChartDoubleTapped(p0: MotionEvent?) {}

    override fun onChartTranslate(p0: MotionEvent?, p1: Float, p2: Float) {

        refreshCandleRange()
    }

    private fun refreshCandleRange() {
        if(!dataList.isNullOrEmpty()) {
            if(abs(candle_chart.xChartMax/candle_chart.scaleX -(candle_chart.highestVisibleX - candle_chart.lowestVisibleX)) > 1)
                return

            var minX = floor(candle_chart.lowestVisibleX)
            var maxX = candle_chart.highestVisibleX.roundToInt().toFloat()
            if(minX < 0)
                minX = 0f
            if(maxX > dataList.size)
                maxX = dataList.size.toFloat()

            if(minX >= maxX)
                return
            var max = dataList.subList(minX.toInt(), maxX.toInt())
                    .maxBy { it.high }!!.high

            val min = -(max) / (10-volumeChartRatio*2) * volumeChartRatio*2

            max += max / 2
            if (!(candle_chart.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum == max.toFloat() && candle_chart.getAxis(YAxis.AxisDependency.RIGHT).axisMinimum == min.toFloat())) {
                candle_chart.getAxis(YAxis.AxisDependency.RIGHT).axisMaximum = max.toFloat()
                candle_chart.getAxis(YAxis.AxisDependency.RIGHT).axisMinimum = min.toFloat()
                candle_chart.invalidate()
            }
        }
    }
}

private operator fun OnChartGestureListener.invoke() {

}
