package com.cyberlogitec.freight9.lib.ui.datepicker.date

import android.content.Context
import android.util.AttributeSet
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.datepicker.WheelPicker
import java.util.*


/**
 * 年份选择器
 * Created by ycuwq on 17-12-27.
 */
class YearPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : WheelPicker<Int?>(context, attrs, defStyleAttr) {

    private var mStartYear = 0
    private var mEndYear = 0
    private var mSelectedYear = 0
    private var mOnYearSelectedListener: OnYearSelectedListener? = null

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        mSelectedYear = Calendar.getInstance()[Calendar.YEAR]
        val a = context.obtainStyledAttributes(attrs, R.styleable.YearPicker)
        mStartYear = a.getInteger(R.styleable.YearPicker_startYear, 1900)
        mEndYear = a.getInteger(R.styleable.YearPicker_endYear, 2100)
        a.recycle()
    }

    private fun updateYear() {
        val list: MutableList<Int> = ArrayList()
        for (i in mStartYear..mEndYear) {
            list.add(i)
        }
        setDataList(list)
    }

    fun setStartYear(startYear: Int) {
        mStartYear = startYear
        updateYear()
        if (mStartYear > mSelectedYear) {
            setSelectedYear(mStartYear, false)
        } else {
            setSelectedYear(mSelectedYear, false)
        }
    }

    fun setEndYear(endYear: Int) {
        mEndYear = endYear
        updateYear()
        if (mSelectedYear > endYear) {
            setSelectedYear(mEndYear, false)
        } else {
            setSelectedYear(mSelectedYear, false)
        }
    }

    fun setYear(startYear: Int, endYear: Int) {
        setStartYear(startYear)
        setEndYear(endYear)
    }

    fun setSelectedYear(selectedYear: Int, smoothScroll: Boolean) {
        setCurrentPosition(selectedYear - mStartYear, smoothScroll)
    }

    fun getSelectedYear(): Int {
        return mSelectedYear
    }

    fun setOnYearSelectedListener(onYearSelectedListener: OnYearSelectedListener?) {
        mOnYearSelectedListener = onYearSelectedListener
    }

    interface OnYearSelectedListener {
        fun onYearSelected(year: Int)
    }

    init {
        initAttrs(context, attrs)
        setItemMaximumWidthText("0000")
        updateYear()
        setSelectedYear(mSelectedYear, false)
        setOnWheelChangeListener(object : OnWheelChangeListener<Int?> {
            override fun onWheelSelected(item: Int?, position: Int) {
                item?.let { selectedItem ->
                    mSelectedYear = selectedItem
                    mOnYearSelectedListener?.onYearSelected(selectedItem)
                }
            }
        })
    }
}