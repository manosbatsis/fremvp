package com.cyberlogitec.freight9.lib.ui.datepicker.date

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.cyberlogitec.freight9.R

class DatePickerDialogFragment : DialogFragment() {

    protected var mDatePicker: DatePicker? = null
    private var mSelectedYear = -1
    private var mSelectedMonth = -1
    private var mSelectedDay = -1
    private var mOnDateChooseListener: OnDateChooseListener? = null
    private var mIsShowAnimation = true
    protected var mCancelButton: Button? = null
    protected var mDecideButton: Button? = null

    fun setOnDateChooseListener(onDateChooseListener: OnDateChooseListener?) {
        mOnDateChooseListener = onDateChooseListener
    }

    fun showAnimation(show: Boolean) {
        mIsShowAnimation = show
    }

    protected fun initChild() {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity, R.style.DatePickerBottomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 设置Content前设定
        dialog.setContentView(R.layout.datepicker_dialog)
        dialog.setCanceledOnTouchOutside(true) // 外部点击取消
        val window = dialog.window
        if (window != null) {
            mDatePicker = window.findViewById(R.id.dayPicker_dialog);
            val btnDone = window.findViewById<Button>(R.id.btn_dialog_date_decide)
            btnDone.setOnClickListener { v ->
                mDatePicker?.let { datePicker ->
                    mOnDateChooseListener?.onDateChoose(
                            datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDay()
                    )
                }
                dismiss()
            }
            if (mIsShowAnimation) {
                window.attributes.windowAnimations = R.style.DatePickerDialogAnim
            }
            val lp = window.attributes
            lp.gravity = Gravity.BOTTOM // 紧贴底部
            lp.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
            lp.dimAmount = 0.35f
            window.attributes = lp
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            if (mSelectedYear > 0) {
                setSelectedDate()
            }
            initChild()
        }
        return dialog
    }

    fun setSelectedDate(year: Int, month: Int, day: Int) {
        mSelectedYear = year
        mSelectedMonth = month
        mSelectedDay = day
        setSelectedDate()
    }

    private fun setSelectedDate() {
        mDatePicker?.setDate(mSelectedYear, mSelectedMonth, mSelectedDay, false)
    }

    interface OnDateChooseListener {
        fun onDateChoose(year: Int, month: Int, day: Int)
    }
}