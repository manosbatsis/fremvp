package com.cyberlogitec.freight9.lib.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.cyberlogitec.freight9.R


class NormalTwoBtnDialog(internal var title: String, private var desc: String,
                         internal  var leftBtnText: String = "", private var rightBtnText: String = "") : DialogFragment() {
    companion object val CLASS_NAME = NormalTwoBtnDialog::class.java.name
    private var mOnClickListener: View.OnClickListener? = null
    private var mOnDismissListener: DialogInterface.OnDismissListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setDimAmount(0.9f)
        dialog.setContentView(R.layout.dialog_normal_twobtn)
        dialog.window!!.attributes.width = (activity!!.resources.displayMetrics.widthPixels * 0.82).toInt()

        val v = dialog.window!!.decorView

        val tvTitle = v.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = title
        val tvDesc = v.findViewById<TextView>(R.id.tv_desc)
        tvDesc.text = desc
        val btnLeft = v.findViewById<Button>(R.id.btn_left)
        if (leftBtnText.isNotEmpty()) btnLeft.text = leftBtnText
        btnLeft.setOnClickListener { view -> mOnClickListener!!.onClick(view) }
        val btnRight = v.findViewById<Button>(R.id.btn_right)
        if (rightBtnText.isNotEmpty()) btnRight.text = rightBtnText
        btnRight.setOnClickListener { view -> mOnClickListener!!.onClick(view) }

        dialog.setCancelable(false)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mOnDismissListener != null) mOnDismissListener!!.onDismiss(dialog)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        this.mOnClickListener = onClickListener
    }

//    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
//        this.mOnDismissListener = onDismissListener
//    }
}
