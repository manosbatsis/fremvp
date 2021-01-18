package com.cyberlogitec.freight9.lib.ui.split

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import androidx.coordinatorlayout.widget.CoordinatorLayout

import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheetBehavior<V : View>  constructor(context: Context, attrs: AttributeSet?) : BottomSheetBehavior<V>(context, attrs) {

    private var allowDragging = true

    fun setAllowDragging(allowDragging: Boolean) {
        this.allowDragging = allowDragging
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return if (!allowDragging) {
            false
        } else super.onInterceptTouchEvent(parent, child, event)

    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return if (!allowDragging) {
            false
        } else super.onTouchEvent(parent, child, event)
    }
}
