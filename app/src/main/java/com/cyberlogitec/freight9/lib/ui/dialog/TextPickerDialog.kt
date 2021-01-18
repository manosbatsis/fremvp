package com.cyberlogitec.freight9.lib.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.textpicker.TextAdapter
import com.cyberlogitec.freight9.lib.ui.textpicker.TextItem
import com.cyberlogitec.freight9.lib.ui.textpicker.TextPicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.*
import kotlinx.android.synthetic.main.textpicker_bottom_sheet_dialog.view.*

data class TextPickerDialog constructor(private val mContext: Context) {
    var mItems: List<String>? = null
    var mOnDismissedListener: OnDismissedListener? = null
    var mOnValueChangeListener: OnValueChangeListener? = null

    class Builder(context: Context) {
        internal var TextPickerDialog: TextPickerDialog

        init {
            this.TextPickerDialog = TextPickerDialog(context)
        }

        fun withItems(items: List<String>) : Builder {
            TextPickerDialog.setItems(items)
            return this
        }
        fun withDismissedRequted(listner: OnDismissedListener) : Builder {
            TextPickerDialog.setOnDismissedListener(listner)
            return this
        }
        fun withValueChangeRequested(listner: OnValueChangeListener) : Builder {
            TextPickerDialog.setOnValueChangeListener(listner)
            return this
        }
        fun build() : TextPickerDialog {
            return this.TextPickerDialog
        }
    }

    private lateinit var dialog: BottomSheetDialog

    private fun setItems(items: List<String>) {
        this.mItems = items
    }

    private fun setOnDismissedListener(onDismissedListener: OnDismissedListener) {
        this.mOnDismissedListener = onDismissedListener
    }

    private fun setOnValueChangeListener(onValueChangeListener: OnValueChangeListener) {
        this.mOnValueChangeListener = onValueChangeListener
    }

    fun show() {
        dialog = BottomSheetDialog(mContext)

        val view = LayoutInflater.from(mContext).inflate(R.layout.textpicker_bottom_sheet_dialog, null)

        dialog.setCancelable(true)
        dialog.setContentView(view)

        dialog.setOnCancelListener{
            //println("setOnCancelListener")
            mOnDismissedListener?.onDismissed()
        }

        dialog.setOnDismissListener{
            println("setOnDismissListener")
            mOnDismissedListener?.onDismissed()
        }

        view.picker.setTextAdapter(TextAdapter(layoutId = R.layout.item_text))
        view.picker.addOnValueChangeListener(object : TextPicker.OnValueChangeListener {
            override fun onValueChange(textPicker: TextPicker, value: String, index: Int) {
                //println("jk: MainActivity: onValueChange: index: ${index}, value: ${value}")
                //Toast.makeText(mContext, "$index: $value", Toast.LENGTH_SHORT).show()

                view.picker.setSelected( index )

                mOnValueChangeListener?.onValueChange(value, index)
            }
        })

        dialog.btn_done.setOnClickListener {
            dialog.hide()
        }

        // set picker test data +
        var textItems = mutableListOf<TextItem>()

        for (i in 0..mItems!!.size-1) {
            textItems.add(TextItem(mItems!![i], i == 2, i))
        }
        view.picker.setItems( textItems )

        view.picker.index = 2
        // set picker test data -

        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }


    //
    // listner interface to call back
    //
    interface OnValueChangeListener {
        fun onValueChange(value: String, index: Int)
    }

    interface OnDismissedListener {
        fun onDismissed()
    }


}