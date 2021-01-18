package com.cyberlogitec.freight9.ui.trademarket

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.MarketWeekDeal
import com.cyberlogitec.freight9.lib.util.getWeekFull
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.trello.rxlifecycle3.components.support.RxFragment
import kotlinx.android.synthetic.main.frag_market_week_deal.*
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

class MarketWeekDealFragment constructor(val viewModel: BaseViewModel): RxFragment(), OnChartValueSelectedListener, OnChartGestureListener {

    private val volumeChartRatio = 1.5f

    private var mDataList: ArrayList<MarketWeekDeal> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View = inflater.inflate(R.layout.frag_market_week_deal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.v("f9: onViewCreated")

        chartInitialize()

        makeData()
        //makeManualData()
        refreshCandleRange()

        tv_deal_price.setSafeOnClickListener {
            makeData()
            //makeManualData()
            refreshCandleRange()

        }

//        var x = mDataList.size/2.toFloat()
//        candle_chart.highlightValue(candle_chart.highlighter.getHighlight(x,0f),true)
        updateUi(mDataList[mDataList.size/2])
    }

    private fun makeManualData(){
        val list: ArrayList<MarketWeekDeal> = java.util.ArrayList()
        val volumeMax = 0
        val highMax = 0

        context?.let { context->
            val simpleText = context.assets.open("chart_temp.json").bufferedReader().use { it.readText() }
            val chartData = JsonParser().parse(simpleText) as JsonArray
            chartData.map { element->
                element.asJsonObject.also { data->
                    list.add(MarketWeekDeal(
                            data["week"].asString,
                            Date(),
                            data["close"].asDouble,
                            data["open"].asDouble,
                            data["high"].asDouble,
                            data["low"].asDouble,
                            data["volume"].asInt
                    ))
                }
            }
            updateData(list)
            val yAxisLeft = candle_chart.getAxis(YAxis.AxisDependency.LEFT)
            yAxisLeft.axisMaximum = (volumeMax*10)/volumeChartRatio
            val yAxisRight = candle_chart.getAxis(YAxis.AxisDependency.RIGHT)
            yAxisRight.axisMinimum = -(highMax)*volumeChartRatio/(10-volumeChartRatio)
            refreshCandleRange()
        }
    }

    private fun makeData() {
        val list: ArrayList<MarketWeekDeal> = java.util.ArrayList()

        for (i in 0..110) {
            val low: Float = getRandom(500f,10f)
            val high: Float = getRandom(500f,low)
            val open: Float = getRandom(high - low,low)
            val deal: Float = getRandom(high - low,low)

            list.add(MarketWeekDeal("201901", Date(),
                    deal.toDouble(),
                    open.toDouble(),
                    high.toDouble(),
                    low.toDouble(),
                    getRandom(1200f,100f).toInt()))
        }
        updateData(list)
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
        candle_chart.setBackgroundColor(context?.let { getColor(it, R.color.color_1a1a1a)}!!)
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
        candle_chart.extraBottomOffset = 10f

        candle_chart.drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE)

        val rightAxis = candle_chart.axisRight
        rightAxis.setDrawGridLines(false)
//        rightAxis.axisMinimum = 0f // this replaces setStartAt+Zero(true)
        rightAxis.typeface = ResourcesCompat.getFont(context!!, R.font.opensans_bold)!!
        rightAxis.textColor = getColor(context!!,R.color.greyish_brown)
        rightAxis.textSize = 13f
        rightAxis.minWidth = 48f
        rightAxis.setDrawAxisLine(false)
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
        xAxis.setLabelCount(3, true)
        xAxis.typeface = ResourcesCompat.getFont(context!!, R.font.opensans_bold)!!
        xAxis.textColor = getColor(context!!, R.color.greyish_brown)
        xAxis.textSize = 13f
        xAxis.axisMinimum = 1f
        xAxis.mLabelWidth = 30

        xAxis.setAvoidFirstLastClipping(true)
        candle_chart.setOnChartValueSelectedListener(this)
        candle_chart.onChartGestureListener = this
    }

    private fun updateData(dataList: ArrayList<MarketWeekDeal>) {
        mDataList = dataList
        candle_chart.clear()
        candle_chart.resetZoom()
        candle_chart.zoom(1f, 0f, 0f, 0f)
        candle_chart.moveViewToX(0f)

        val data = CombinedData()
        data.setData(generateBarData(dataList))
        data.setData(generateCandleData(dataList))

        candle_chart.xAxis.axisMaximum = data.xMax + 0.25f
        candle_chart.xAxis.axisMinimum = data.xMin - 0.25f
        //거래량 차트 y 최대값
        val yAxisLeft = candle_chart.getAxis(YAxis.AxisDependency.LEFT)
        val max = dataList.maxBy { it.volume }
        yAxisLeft.axisMaximum = max!!.volume/volumeChartRatio *10
        val yAxisRight = candle_chart.getAxis(YAxis.AxisDependency.RIGHT)
        yAxisRight.axisMinimum = -1300f
        candle_chart.axisLeft.spaceTop = 100f
        candle_chart.xAxis.valueFormatter = XAxisFormatter(data.xMax.toInt())

        candle_chart.axisRight.valueFormatter = RightAxisFormatter()

        candle_chart.data = data
        val ratio = dataList.size / 20
        candle_chart.zoom(ratio.toFloat(), 0f, 0f, 0f)
        candle_chart.moveViewToX(data.xMax)
        candle_chart.invalidate()

    }
    private fun generateBarData(dataList: ArrayList<MarketWeekDeal>): BarData {
        val entries1 = ArrayList<BarEntry>()
        for ((index, data) in dataList.withIndex()) {
            entries1.add(BarEntry(index+1f, data.volume.toFloat()))
        }

        val set1 = BarDataSet(entries1, "Bar 1")
        set1.color = Color.rgb(76, 76, 76)
        set1.axisDependency = YAxis.AxisDependency.LEFT
        set1.setDrawValues(false)

        val barWidth = 0.45f // x2 dataset
        val d = BarData(set1)
        d.isHighlightEnabled = false
        d.barWidth = barWidth

        return d
    }
    private fun generateCandleData(dataList: ArrayList<MarketWeekDeal>): CandleData {
        val d = CandleData()
        val entries = ArrayList<CandleEntry>()

        for ((index, data) in dataList.withIndex()) {

            entries.add(CandleEntry(index + 1f,
                    data.highprice.toFloat(),
                    data.lowprice.toFloat(),
                    data.openprice.toFloat(),
                    data.lastprice.toFloat(),data))
        }

        val set = CandleDataSet(entries, "Candle DataSet")
        set.decreasingColor = Color.rgb(0, 58, 255)
        set.increasingColor = Color.rgb(0, 179, 160)
        set.neutralColor = getColor(context!!, R.color.greyish_brown)
        set.highLightColor = getColor(context!!, R.color.white)
        set.highlightLineWidth = 1f
        set.increasingPaintStyle = Paint.Style.FILL
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.shadowColor = Color.DKGRAY
        set.barSpace = 0.3f
        set.valueTextSize = 20f
        set.setDrawValues(false)
        d.addDataSet(set)

        return d
    }
    private fun getRandom(range: Float, start: Float): Float {
        return (Math.random() * range).toFloat() + start
    }
    inner class XAxisFormatter(count: Int) : ValueFormatter() {
        private val startYear : Int = Calendar.getInstance().get(Calendar.YEAR) - (count/53)

        override fun getFormattedValue(value: Float): String {
            val currentYear = startYear+(value / 53).toInt()
            return "W"+((value % 53).toInt()+1).toString() + ", " + currentYear.toString()
        }
    }

    inner class RightAxisFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value >0) DecimalFormat("#,###").format(value.toLong()) else ""
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateUi(data: MarketWeekDeal) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        currencyFormat.minimumFractionDigits = 0

        tv_week.text = "${context!!.getWeekFull(data.week)}, ${data.week.substring(0,4)}"
        tv_deal_price.text = currencyFormat.format(data.lastprice.toInt())
        when {
            (data.lastprice - data.openprice) > 0 -> {
                v_deal_zero.visibility = View.INVISIBLE
                v_deal_up_down.visibility = View.VISIBLE
                v_deal_up_down.background = getDrawable(context!!, R.drawable.ic_up_green)
                tv_deal_price_diff.setTextColor(getColor(context!!, R.color.green_blue))
                tv_deal_price_diff_percent.setTextColor(getColor(context!!, R.color.green_blue))
            }
            (data.lastprice - data.openprice) < 0 -> {
                v_deal_zero.visibility = View.INVISIBLE
                v_deal_up_down.visibility = View.VISIBLE
                v_deal_up_down.background = getDrawable(context!!, R.drawable.ic_down_blue)
                tv_deal_price_diff.setTextColor(getColor(context!!, R.color.color_003aff))
                tv_deal_price_diff_percent.setTextColor(getColor(context!!, R.color.color_003aff))
            }
            else -> {
                v_deal_zero.visibility = View.VISIBLE
                v_deal_up_down.visibility = View.INVISIBLE
                tv_deal_price_diff.setTextColor(getColor(context!!, R.color.white))
                tv_deal_price_diff_percent.setTextColor(getColor(context!!, R.color.white))
            }
        }
        tv_deal_price_diff.text = currencyFormat.format(abs(data.lastprice - data.openprice).toInt())
        tv_deal_price_diff_percent.text = "(${"%.2f".format(abs((data.lastprice - data.openprice).toFloat()/data.openprice*100))}%)"
        //todo date 는 수정이 필요
        val cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("d. MMM, yyyy HH:mm:ss")
        //date 표시
        tv_deal_date.text = df.format(cal.time)

        tv_open_price.text = "${currencyFormat.format(data.openprice.toInt())}/T"
        tv_low_price.text = "${currencyFormat.format(data.lowprice.toInt())}/T"
        tv_high_price.text = "${currencyFormat.format(data.highprice.toInt())}/T"
        tv_deal_volume.text = "${data.volume}T"
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(p0: Entry?, p1: Highlight?) {
        // ui를 없데이트 한다
        updateUi(p0!!.data as MarketWeekDeal)
    }
    companion object {
        @JvmStatic
        fun newInstance(viewModel: BaseViewModel) : MarketWeekDealFragment {
            return MarketWeekDealFragment(viewModel)
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
        if(!mDataList.isNullOrEmpty()) {
            if(abs(candle_chart.xChartMax/candle_chart.scaleX -(candle_chart.highestVisibleX - candle_chart.lowestVisibleX)) > 1)
                return

            var minX = floor(candle_chart.lowestVisibleX)
            var maxX = candle_chart.highestVisibleX.roundToInt().toFloat()
            if(minX < 0)
                minX = 0f
            if(maxX > mDataList.size)
                maxX = mDataList.size.toFloat()

            if(minX >= maxX)
                return
            val max = mDataList.subList(minX.toInt(), maxX.toInt())
                    .maxBy { it.highprice }!!.highprice

            val min = -(max) / (10-volumeChartRatio*2) * volumeChartRatio*2
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
