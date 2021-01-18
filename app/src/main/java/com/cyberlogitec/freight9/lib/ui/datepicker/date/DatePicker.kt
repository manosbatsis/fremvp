package com.cyberlogitec.freight9.lib.ui.datepicker.date

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.cyberlogitec.freight9.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DatePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr),
        YearPicker.OnYearSelectedListener,
        MonthPicker.OnMonthSelectedListener,
        DayPicker.OnDaySelectedListener {

    /**
     * Gets year picker.
     *
     * @return the year picker
     */
    private var yearPicker: YearPicker? = null

    /**
     * Gets month picker.
     *
     * @return the month picker
     */
    private var monthPicker: MonthPicker? = null

    /**
     * Gets day picker.
     *
     * @return the day picker
     */
    private var dayPicker: DayPicker? = null

    private var mMaxDate: Long? = null
    private var mMinDate: Long? = null
    private var mOnDateSelectedListener: OnDateSelectedListener? = null

    private fun initAttrs(context: Context, attrs: AttributeSet?) {

        if (attrs == null) {
            return
        }

        val a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker)
        val textSize = a.getDimensionPixelSize(R.styleable.DatePicker_itemTextSize,
                resources.getDimensionPixelSize(R.dimen.WheelItemTextSize))
        val textColor = a.getColor(R.styleable.DatePicker_itemTextColor, Color.parseColor("#9d9d9d"))
        val isTextGradual = a.getBoolean(R.styleable.DatePicker_textGradual, false)
        val isCyclic = a.getBoolean(R.styleable.DatePicker_wheelCyclic, false)
        val halfVisibleItemCount = a.getInteger(R.styleable.DatePicker_halfVisibleItemCount, 2)
        val selectedItemTextColor = a.getColor(R.styleable.DatePicker_selectedTextColor, Color.parseColor("#292929"))
        val selectedItemTextSize = a.getDimensionPixelSize(R.styleable.DatePicker_selectedTextSize,
                resources.getDimensionPixelSize(R.dimen.WheelSelectedItemTextSize))
        val itemWidthSpace = a.getDimensionPixelSize(R.styleable.DatePicker_itemWidthSpace,
                resources.getDimensionPixelOffset(R.dimen.WheelItemWidthSpace))
        val itemHeightSpace = a.getDimensionPixelSize(R.styleable.DatePicker_itemHeightSpace,
                resources.getDimensionPixelOffset(R.dimen.WheelItemHeightSpace))
        val isZoomInSelectedItem = a.getBoolean(R.styleable.DatePicker_zoomInSelectedItem, false)
        val isShowCurtain = a.getBoolean(R.styleable.DatePicker_wheelCurtain, true)
        val curtainColor = a.getColor(R.styleable.DatePicker_wheelCurtainColor, Color.WHITE)
        val isShowCurtainBorder = a.getBoolean(R.styleable.DatePicker_wheelCurtainBorder, true)
        val curtainBorderColor = a.getColor(R.styleable.DatePicker_wheelCurtainBorderColor,
                resources.getColor(R.color.datepicker_divider))
        val areaAlign = a.getInteger(R.styleable.DatePicker_areaAlign, 1);
        a.recycle()

        setTextSize(textSize)
        setTextColor(textColor)
        setTextGradual(isTextGradual)
        setCyclic(isCyclic)
        setHalfVisibleItemCount(halfVisibleItemCount)
        setSelectedItemTextColor(selectedItemTextColor)
        setSelectedItemTextSize(selectedItemTextSize)
        setItemWidthSpace(itemWidthSpace)
        setItemHeightSpace(itemHeightSpace)
        setZoomInSelectedItem(isZoomInSelectedItem)
        setShowCurtain(isShowCurtain)
        setCurtainColor(curtainColor)
        setShowCurtainBorder(isShowCurtainBorder)
        setCurtainBorderColor(curtainBorderColor)
        setSelectedItemTextAlign()
        //setAreaAlign(areaAlign)
    }

    private fun initChild() {
        yearPicker = findViewById(R.id.yearPicker_layout_date)
        yearPicker!!.setOnYearSelectedListener(this)
        monthPicker = findViewById(R.id.monthPicker_layout_date)
        monthPicker!!.setOnMonthSelectedListener(this)
        dayPicker = findViewById(R.id.dayPicker_layout_date)
        dayPicker!!.setOnDaySelectedListener(this)
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        if (yearPicker != null && monthPicker != null && dayPicker != null) {
            yearPicker!!.setBackgroundColor(color)
            monthPicker!!.setBackgroundColor(color)
            dayPicker!!.setBackgroundColor(color)
        }
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        if (yearPicker != null && monthPicker != null && dayPicker != null) {
            yearPicker!!.setBackgroundResource(resid)
            monthPicker!!.setBackgroundResource(resid)
            dayPicker!!.setBackgroundResource(resid)
        }
    }

    override fun setBackgroundDrawable(background: Drawable) {
        super.setBackgroundDrawable(background)
        if (yearPicker != null && monthPicker != null && dayPicker != null) {
            yearPicker!!.setBackgroundDrawable(background)
            monthPicker!!.setBackgroundDrawable(background)
            dayPicker!!.setBackgroundDrawable(background)
        }
    }

    private fun onDateSelected() {
        if (mOnDateSelectedListener != null) {
            mOnDateSelectedListener!!.onDateSelected(getYear(), getMonth(), getDay())
        }
    }

    override fun onMonthSelected(month: Int) {
        dayPicker!!.setMonth(getYear(), month)
        onDateSelected()
    }

    override fun onDaySelected(day: Int) {
        onDateSelected()
    }

    override fun onYearSelected(year: Int) {
        monthPicker!!.setYear(year)
        dayPicker!!.setMonth(year, getMonth())
        onDateSelected()
    }

    /**
     * Sets date.
     *
     * @param year  the year
     * @param month the month
     * @param day   the day
     */
    fun setDate(year: Int, month: Int, day: Int) {
        setDate(year, month, day, true)
    }

    /**
     * Sets date.
     *
     * @param year         the year
     * @param month        the month
     * @param day          the day
     * @param smoothScroll the smooth scroll
     */
    fun setDate(year: Int, month: Int, day: Int, smoothScroll: Boolean) {
        yearPicker!!.setSelectedYear(year, smoothScroll)
        monthPicker!!.setSelectedMonth(month, smoothScroll)
        dayPicker!!.setSelectedDay(day, smoothScroll)
    }

    fun setMaxDate(date: Long) {
        setCyclic(false)
        mMaxDate = date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        yearPicker!!.setEndYear(calendar[Calendar.YEAR])
        monthPicker!!.setMaxDate(date)
        dayPicker!!.setMaxDate(date)
        monthPicker!!.setYear(yearPicker!!.getSelectedYear())
        dayPicker!!.setMonth(yearPicker!!.getSelectedYear(), monthPicker!!.getSelectedMonth())
    }

    fun setMinDate(date: Long) {
        setCyclic(false)
        mMinDate = date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        yearPicker!!.setStartYear(calendar[Calendar.YEAR])
        monthPicker!!.setMinDate(date)
        dayPicker!!.setMinDate(date)
        monthPicker!!.setYear(yearPicker!!.getSelectedYear())
        dayPicker!!.setMonth(yearPicker!!.getSelectedYear(), monthPicker!!.getSelectedMonth())
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    fun getDate(): String? {
        val format = SimpleDateFormat.getDateInstance()
        return getDate(format)
    }

    /**
     * Gets date.
     *
     * @param dateFormat the date format
     * @return the date
     */
    fun getDate(dateFormat: DateFormat): String {
        val year = getYear()
        val month = getMonth()
        val day = getDay()
        val calendar = Calendar.getInstance()
        calendar[year, month - 1] = day
        return dateFormat.format(calendar.time)
    }

    /**
     * Gets year.
     *
     * @return the year
     */
    fun getYear(): Int {
        return yearPicker!!.getSelectedYear()
    }

    /**
     * Gets month.
     *
     * @return the month
     */
    fun getMonth(): Int {
        return monthPicker!!.getSelectedMonth()
    }

    /**
     * Gets day.
     *
     * @return the day
     */
    fun getDay(): Int {
        return dayPicker!!.getSelectedDay()
    }

    /**
     * Gets year picker.
     *
     * @return the year picker
     */
    fun getYearPicker() = yearPicker!!

    /**
     * Gets month picker.
     *
     * @return the month picker
     */
    fun getMonthPicker() = monthPicker!!

    /**
     * Gets day picker.
     *
     * @return the day picker
     */
    fun getDayPicker() = dayPicker!!

    /**
     * 一般列表的文本颜色
     *
     * @param textColor 文本颜色
     */
    fun setTextColor(textColor: Int) {
        dayPicker!!.setTextColor(textColor)
        monthPicker!!.setTextColor(textColor)
        yearPicker!!.setTextColor(textColor)
    }

    /**
     * 一般列表的文本大小
     *
     * @param textSize 文字大小
     */
    private fun setTextSize(textSize: Int) {
        dayPicker!!.setTextSize(textSize)
        monthPicker!!.setTextSize(textSize)
        yearPicker!!.setTextSize(textSize)
    }

    /**
     * 设置被选中时候的文本颜色
     *
     * @param selectedItemTextColor 文本颜色
     */
    private fun setSelectedItemTextColor(selectedItemTextColor: Int) {
        dayPicker!!.setSelectedItemTextColor(selectedItemTextColor)
        monthPicker!!.setSelectedItemTextColor(selectedItemTextColor)
        yearPicker!!.setSelectedItemTextColor(selectedItemTextColor)
    }

    private fun setSelectedItemTextAlign() {
        dayPicker!!.setSelectedItemTextAlign(Paint.Align.CENTER)
        monthPicker!!.setSelectedItemTextAlign(Paint.Align.CENTER)
        yearPicker!!.setSelectedItemTextAlign(Paint.Align.CENTER)
    }

    /**
     * 设置被选中时候的文本大小
     *
     * @param selectedItemTextSize 文字大小
     */
    private fun setSelectedItemTextSize(selectedItemTextSize: Int) {
        dayPicker!!.setSelectedItemTextSize(selectedItemTextSize)
        monthPicker!!.setSelectedItemTextSize(selectedItemTextSize)
        yearPicker!!.setSelectedItemTextSize(selectedItemTextSize)
    }

    /**
     * 设置显示数据量的个数的一半。
     * 为保证总显示个数为奇数,这里将总数拆分，itemCount = mHalfVisibleItemCount * 2 + 1
     *
     * @param halfVisibleItemCount 总数量的一半
     */
    private fun setHalfVisibleItemCount(halfVisibleItemCount: Int) {
        dayPicker!!.setHalfVisibleItemCount(halfVisibleItemCount)
        monthPicker!!.setHalfVisibleItemCount(halfVisibleItemCount)
        yearPicker!!.setHalfVisibleItemCount(halfVisibleItemCount)
    }

    /**
     * Sets item width space.
     *
     * @param itemWidthSpace the item width space
     */
    private fun setItemWidthSpace(itemWidthSpace: Int) {
        dayPicker!!.setItemWidthSpace(itemWidthSpace)
        monthPicker!!.setItemWidthSpace(itemWidthSpace)
        yearPicker!!.setItemWidthSpace(itemWidthSpace)
    }

    /**
     * 设置两个Item之间的间隔
     *
     * @param itemHeightSpace 间隔值
     */
    private fun setItemHeightSpace(itemHeightSpace: Int) {
        dayPicker!!.setItemHeightSpace(itemHeightSpace)
        monthPicker!!.setItemHeightSpace(itemHeightSpace)
        yearPicker!!.setItemHeightSpace(itemHeightSpace)
    }

    /**
     * Set zoom in center item.
     *
     * @param zoomInSelectedItem the zoom in center item
     */
    private fun setZoomInSelectedItem(zoomInSelectedItem: Boolean) {
        dayPicker!!.setZoomInSelectedItem(zoomInSelectedItem)
        monthPicker!!.setZoomInSelectedItem(zoomInSelectedItem)
        yearPicker!!.setZoomInSelectedItem(zoomInSelectedItem)
    }

    /**
     * 设置是否循环滚动。
     * set wheel cyclic
     * @param cyclic 上下边界是否相邻
     */
    private fun setCyclic(cyclic: Boolean) {
        dayPicker!!.setCyclic(cyclic)
        monthPicker!!.setCyclic(cyclic)
        yearPicker!!.setCyclic(cyclic)
    }

    /**
     * 设置文字渐变，离中心越远越淡。
     * Set the text color gradient
     * @param textGradual 是否渐变
     */
    private fun setTextGradual(textGradual: Boolean) {
        dayPicker!!.setTextGradual(textGradual)
        monthPicker!!.setTextGradual(textGradual)
        yearPicker!!.setTextGradual(textGradual)
    }

    /**
     * 设置中心Item是否有幕布遮盖
     * set the center item curtain cover
     * @param showCurtain 是否有幕布
     */
    private fun setShowCurtain(showCurtain: Boolean) {
        dayPicker!!.setShowCurtain(showCurtain)
        monthPicker!!.setShowCurtain(showCurtain)
        yearPicker!!.setShowCurtain(showCurtain)
    }

    /**
     * 设置幕布颜色
     * set curtain color
     * @param curtainColor 幕布颜色
     */
    fun setCurtainColor(curtainColor: Int) {
        dayPicker!!.setCurtainColor(curtainColor)
        monthPicker!!.setCurtainColor(curtainColor)
        yearPicker!!.setCurtainColor(curtainColor)
    }

    /**
     * 设置幕布是否显示边框
     * set curtain border
     * @param showCurtainBorder 是否有幕布边框
     */
    fun setShowCurtainBorder(showCurtainBorder: Boolean) {
        dayPicker!!.setShowCurtainBorder(showCurtainBorder)
        monthPicker!!.setShowCurtainBorder(showCurtainBorder)
        yearPicker!!.setShowCurtainBorder(showCurtainBorder)
    }

    /**
     * 幕布边框的颜色
     * curtain border color
     * @param curtainBorderColor 幕布边框颜色
     */
    fun setCurtainBorderColor(curtainBorderColor: Int) {
        dayPicker!!.setCurtainBorderColor(curtainBorderColor)
        monthPicker!!.setCurtainBorderColor(curtainBorderColor)
        yearPicker!!.setCurtainBorderColor(curtainBorderColor)
    }

    /**
     * 设置选择器的指示器文本
     * set indicator text
     * @param yearText  年指示器文本
     * @param monthText 月指示器文本
     * @param dayText   日指示器文本
     */
    fun setIndicatorText(yearText: String?, monthText: String?, dayText: String?) {
        yearPicker!!.setIndicatorText(yearText)
        monthPicker!!.setIndicatorText(monthText)
        dayPicker!!.setIndicatorText(dayText)
    }

    /**
     * 设置指示器文字的颜色
     * set indicator text color
     * @param textColor 文本颜色
     */
    fun setIndicatorTextColor(textColor: Int) {
        yearPicker!!.setIndicatorTextColor(textColor)
        monthPicker!!.setIndicatorTextColor(textColor)
        dayPicker!!.setIndicatorTextColor(textColor)
    }

    /**
     * 设置指示器文字的大小
     * indicator text size
     * @param textSize 文本大小
     */
    fun setIndicatorTextSize(textSize: Int) {
        yearPicker!!.setTextSize(textSize)
        monthPicker!!.setTextSize(textSize)
        dayPicker!!.setTextSize(textSize)
    }

    fun setAreaAlign(areaAlign: Int) {
        dayPicker!!.setAreaAlign(areaAlign)
        monthPicker!!.setAreaAlign(areaAlign)
        yearPicker!!.setAreaAlign(areaAlign)
    }

    /**
     * Sets on date selected listener.
     *
     * @param onDateSelectedListener the on date selected listener
     */
    fun setOnDateSelectedListener(onDateSelectedListener: OnDateSelectedListener?) {
        mOnDateSelectedListener = onDateSelectedListener
    }

    /**
     * The interface On date selected listener.
     */
    interface OnDateSelectedListener {
        /**
         * On date selected.
         *
         * @param year  the year
         * @param month the month
         * @param day   the day
         */
        fun onDateSelected(year: Int, month: Int, day: Int)
    }
    /**
     * Instantiates a new Date picker.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    /**
     * Instantiates a new Date picker.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    /**
     * Instantiates a new Date picker.
     *
     * @param context the context
     */
    init {
        LayoutInflater.from(context).inflate(R.layout.datepicker_layout_date, this)
        initChild()
        initAttrs(context, attrs)
        yearPicker!!.setBackgroundDrawable(background)
        monthPicker!!.setBackgroundDrawable(background)
        dayPicker!!.setBackgroundDrawable(background)
    }
}