package com.cyberlogitec.freight9.ui.routeselect.featured

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import com.cyberlogitec.freight9.R
import kotlinx.android.synthetic.main.act_featured_route_delete.*
import timber.log.Timber

class FeaturedRouteDeleteDialog(context: Context, val listener: DialogEventListener) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("onCreate")
        setContentView(R.layout.act_featured_route_delete)

        val layout = WindowManager.LayoutParams()
        layout.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layout.dimAmount = 0.8f
        window?.let {
            it.attributes = layout
            it.setBackgroundDrawableResource(android.R.color.transparent)
            it.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
        }

        tv_btn_close.setOnClickListener {
            this.dismiss()
        }

        tv_btn_delete.setOnClickListener {
            listener.onDeleteEvent()
            this.dismiss()
        }
    }

    interface DialogEventListener {
        fun onDeleteEvent()
    }

}