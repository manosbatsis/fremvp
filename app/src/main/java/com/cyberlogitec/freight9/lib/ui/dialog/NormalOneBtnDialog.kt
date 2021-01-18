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


class NormalOneBtnDialog(internal var title: String, private var desc: String,
                         private var btnText: String = "") : DialogFragment() {
    companion object val className = NormalOneBtnDialog::class.java.name
    private var mOnClickListener: View.OnClickListener? = null
    private var mOnDismissListener: DialogInterface.OnDismissListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setDimAmount(0.9f)
        dialog.setContentView(R.layout.dialog_normal_onebtn)
        dialog.window!!.attributes.width = (activity!!.resources.displayMetrics.widthPixels * 0.82).toInt()

        val decoView = dialog.window!!.decorView

        val tvTitle = decoView.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = title
        val tvDesc = decoView.findViewById<TextView>(R.id.tv_desc)
        tvDesc.text = desc
        val btnOk = decoView.findViewById<Button>(R.id.btn_ok)
        if (btnText.isNotEmpty()) btnOk.text = btnText
        btnOk.setOnClickListener { v -> mOnClickListener!!.onClick(v) }

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

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
        this.mOnDismissListener = onDismissListener
    }
}
