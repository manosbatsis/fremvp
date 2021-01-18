package com.cyberlogitec.freight9.lib.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.FrameLayout
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.view.SpinView
import kotlinx.android.synthetic.main.dialog_progress.view.*
import timber.log.Timber

class ProgressDialog(val context: Context) {

    enum class Style {
        SPIN_DARK,
        SPIN_LIGHT,
        SPIN_LIGHT_CENTER_TITLE,
        SPIN_LIGHT_BOTTOM_TITLE
    }

    private var view: View?
    private var dialog: Dialog? = Dialog(context)
    private val animateSpeed = 1.0F

    fun setStyle(style: Style): ProgressDialog {
        var spinView: View? = null

        view!!.tv_loading_center.visibility = View.GONE

        // default
        dialog!!.window!!.setDimAmount(0.95f)

        when (style) {
            Style.SPIN_DARK -> {
                spinView = SpinView(context, R.drawable.ic_loading_d)
                view!!.ll_loading_center.visibility = View.VISIBLE
                view!!.ll_loading_bottom.visibility = View.GONE
                view!!.tv_loading_center.visibility = View.GONE
            }
            Style.SPIN_LIGHT -> {
                spinView = SpinView(context, R.drawable.ic_loading_l)
                view!!.ll_loading_center.visibility = View.VISIBLE
                view!!.ll_loading_bottom.visibility = View.GONE
                view!!.tv_loading_center.visibility = View.GONE
            }
            Style.SPIN_LIGHT_CENTER_TITLE -> {
                spinView = SpinView(context, R.drawable.ic_loading_l)
                view!!.ll_loading_center.visibility = View.VISIBLE
                view!!.ll_loading_bottom.visibility = View.GONE
                view!!.tv_loading_center.visibility = View.VISIBLE
            }
            Style.SPIN_LIGHT_BOTTOM_TITLE -> {
                dialog!!.window!!.setDimAmount(0.0f)
                spinView = SpinView(context, R.drawable.ic_loading_l)
                view!!.ll_loading_center.visibility = View.GONE
                view!!.ll_loading_bottom.visibility = View.VISIBLE
            }
        }
        spinView.setAnimationSpeed(animateSpeed)

        val containerCenterFrame = view!!.findViewById<View>(R.id.container_center) as FrameLayout
        val containerBottomFrame = view!!.findViewById<View>(R.id.container_bottom) as FrameLayout
        val wrapParam = ViewGroup.LayoutParams.WRAP_CONTENT
        val containerParams = ViewGroup.LayoutParams(wrapParam, wrapParam)

        containerCenterFrame.removeAllViews()
        containerBottomFrame.removeAllViews()

        if (style == Style.SPIN_LIGHT_BOTTOM_TITLE) {
            containerBottomFrame.addView(spinView, containerParams)
        } else {
            containerCenterFrame.addView(spinView, containerParams)
        }

        return this
    }

    fun show(context: Context? = null, displayText: String = "", displayTextCenter: Boolean = true) {
        Timber.d("f9: progressDialog show : $context")
        try {
            if (displayTextCenter) {
                view!!.tv_loading_center.text = displayText
            } else {
                view!!.tv_loading_bottom.text = displayText
            }
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.setCancelable(false)
            dialog!!.show()
        } catch( e: Exception ) { }
    }

    fun dismiss() {
        Timber.d("f9: progressDialog dismiss : $dialog")
        dialog?.dismiss()
    }

    fun isShowing() = dialog?.isShowing ?: false

    companion object {
        fun create(context: Context): ProgressDialog {
            return ProgressDialog(context)
        }
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.dialog_progress, null)

        try {
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.setDimAmount(0.95f)
            dialog!!.setContentView(view!!)
        } catch(e: Exception) { }
    }
}