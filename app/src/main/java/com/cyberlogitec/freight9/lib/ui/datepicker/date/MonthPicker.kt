package com.cyberlogitec.freight9.lib.ui.datepicker.date

import android.content.Context
import android.util.AttributeSet
import com.cyberlogitec.freight9.lib.ui.datepicker.WheelPicker
import java.text.NumberFormat
import java.util.*


/**
 * 月份选择器
 * Created by ycuwq on 17-12-28.
 */
class MonthPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : WheelPicker<Int?>(context, attrs, defStyleAttr) {

    private var mSelectedMonth: Int
    private var mOnMonthSelectedListener: OnMonthSelectedListener? = null
    private var mYear = 0
    private var mMaxDate: Long = 0
    private var mMinDate: Long = 0
    private var mMaxYear = 0
    private var mMinYear = 0
    private var mMinMonth = MIN_MONTH
    private var mMaxMonth = MAX_MONTH

    private fun updateMonth() {
        val list: MutableList<Int> = ArrayList()
        for (i in mMinMonth..mMaxMonth) {
            list.add(i)
        }
        setDataList(list)
    }

    fun setMaxDate(date: Long) {
        mMaxDate = date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        mMaxYear = calendar[Calendar.YEAR]
    }

    fun setMinDate(date: Long) {
        mMinDate = date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        mMinYear = calendar[Calendar.YEAR]
    }

    fun setYear(year: Int) {
        mYear = year
        mMinMonth = MIN_MONTH
        mMaxMonth = MAX_MONTH
        if (mMaxDate != 0L && mMaxYear == year) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = mMaxDate
            mMaxMonth = calendar[Calendar.MONTH] + 1
        }
        if (mMinDate != 0L && mMinYear == year) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = mMinDate
            mMinMonth = calendar[Calendar.MONTH] + 1
        }
        updateMonth()
        if (mSelectedMonth > mMaxMonth) {
            setSelectedMonth(mMaxMonth, false)
        } else if (mSelectedMonth < mMinMonth) {
            setSelectedMonth(mMinMonth, false)
        } else {
            setSelectedMonth(mSelectedMonth, false)
        }
    }

    fun getSelectedMonth(): Int {
        return mSelectedMonth
    }

    fun setSelectedMonth(selectedMonth: Int) {
        setSelectedMonth(selectedMonth, true)
    }

    fun setSelectedMonth(selectedMonth: Int, smoothScroll: Boolean) {
        setCurrentPosition(selectedMonth - mMinMonth, smoothScroll)
        mSelectedMonth = selectedMonth
    }

    fun setOnMonthSelectedListener(onMonthSelectedListener: OnMonthSelectedListener?) {
        mOnMonthSelectedListener = onMonthSelectedListener
    }

    interface OnMonthSelectedListener {
        fun onMonthSelected(month: Int)
    }

    companion object {
        private const val MAX_MONTH = 12
        private const val MIN_MONTH = 1
    }

    init {
        setItemMaximumWidthText("00")
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.minimumIntegerDigits = 2
        setDataFormat(numberFormat)
        Calendar.getInstance().clear()
        mSelectedMonth = Calendar.getInstance()[Calendar.MONTH] + 1
        updateMonth()
        setSelectedMonth(mSelectedMonth, false)
        setOnWheelChangeListener(object : OnWheelChangeListener<Int?> {
            override fun onWheelSelected(item: Int?, position: Int) {
                item?.let { selectedItem ->
                    mSelectedMonth = selectedItem
                    mOnMonthSelectedListener?.onMonthSelected(selectedItem)
                }
            }
        })
    }
}
