package com.cyberlogitec.freight9.lib.ui.view

import android.os.SystemClock
import android.view.View
import com.cyberlogitec.freight9.config.Constant.CLICK_INTERVAL

class SafeClickListener(
        private var defaultInterval: Int = CLICK_INTERVAL,
        private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}