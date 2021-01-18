package com.cyberlogitec.freight9.lib.ui.datepicker.date

import android.content.ContentValues
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.cyberlogitec.freight9.lib.ui.datepicker.WheelPicker
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


/**
 * 日期选择
 * Created by ycuwq on 17-12-28.
 */
class DayPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : WheelPicker<Int?>(context, attrs, defStyleAttr) {

    private var mMinDay: Int
    private var mMaxDay: Int
    private var mSelectedDay: Int
    private val mYear = 0
    private val mMonth = 0
    private var mMaxDate: Long = 0
    private var mMinDate: Long = 0
    private var mIsSetMaxDate = false
    private var mOnDaySelectedListener: OnDaySelectedListener? = null

    fun setMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mMaxDate
        val maxYear = calendar[Calendar.YEAR]
        val maxMonth = calendar[Calendar.MONTH] + 1
        val maxDay = calendar[Calendar.DAY_OF_MONTH]

        //如果不判断mIsSetMaxDate，则long 为0，则选择1970-01-01 时会有问题
        if (mIsSetMaxDate && maxYear == year && maxMonth == month) {
            mMaxDay = maxDay
        } else {
            calendar[year, month - 1] = 1
            mMaxDay = calendar.getActualMaximum(Calendar.DATE)
        }

        Timber.d("setMonth: year:$year month: $month day:$mMaxDay")

        calendar.timeInMillis = mMinDate
        val minYear = calendar[Calendar.YEAR]
        val minMonth = calendar[Calendar.MONTH] + 1
        val minDay = calendar[Calendar.DAY_OF_MONTH]

        mMinDay = if (minYear == year && minMonth == month) {
            minDay
        } else {
            1
        }

        updateDay()

        if (mSelectedDay < mMinDay) {
            setSelectedDay(mMinDay, false)
        } else if (mSelectedDay > mMaxDay) {
            setSelectedDay(mMaxDay, false)
        } else {
            setSelectedDay(mSelectedDay, false)
        }
    }

    fun getSelectedDay(): Int {
        return mSelectedDay
    }

    fun setSelectedDay(selectedDay: Int) {
        setSelectedDay(selectedDay, true)
    }


    fun setSelectedDay(selectedDay: Int, smoothScroll: Boolean) {
        setCurrentPosition(selectedDay - mMinDay, smoothScroll)
        mSelectedDay = selectedDay
    }

    fun setMaxDate(date: Long) {
        mMaxDate = date
        mIsSetMaxDate = true
    }

    fun setMinDate(date: Long) {
        mMinDate = date
    }

    fun setOnDaySelectedListener(onDaySelectedListener: OnDaySelectedListener?) {
        mOnDaySelectedListener = onDaySelectedListener
    }

    private fun updateDay() {
        val list: MutableList<Int> = ArrayList()
        for (i in mMinDay..mMaxDay) {
            list.add(i)
        }
        setDataList(list)
    }

    interface OnDaySelectedListener {
        fun onDaySelected(day: Int)
    }

    init {
        setItemMaximumWidthText("00")
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.minimumIntegerDigits = 2
        setDataFormat(numberFormat)
        mMinDay = 1
        mMaxDay = Calendar.getInstance().getActualMaximum(Calendar.DATE)
        updateDay()
        mSelectedDay = Calendar.getInstance()[Calendar.DATE]
        setSelectedDay(mSelectedDay, false)
        setOnWheelChangeListener(object : OnWheelChangeListener<Int?> {
            override fun onWheelSelected(item: Int?, position: Int) {
                item?.let { selectedItem ->
                    mSelectedDay = selectedItem
                    mOnDaySelectedListener?.onDaySelected(selectedItem)
                }
            }
        })
    }
}
