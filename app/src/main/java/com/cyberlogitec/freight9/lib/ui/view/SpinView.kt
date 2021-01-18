package com.cyberlogitec.freight9.lib.ui.view

import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.widget.AppCompatImageView

class SpinView(context: Context, resId: Int) : AppCompatImageView(context) {

    private var mRotateDegrees = 0.0F
    private var mFrameTime = 0L
    private var mNeedToUpdateView = false
    private var mUpdateViewRunnable = Runnable { updateView() }

    init {
        setImageResource(resId);
        mFrameTime = 1000 / 9;
    }

    fun setAnimationSpeed(scale: Float) {
        mFrameTime = (1000 / 9 / scale).toLong()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(mRotateDegrees, width / 2.toFloat(), height / 2.toFloat())
        super.onDraw(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mNeedToUpdateView = true
        post(mUpdateViewRunnable)
    }

    override fun onDetachedFromWindow() {
        mNeedToUpdateView = false
        super.onDetachedFromWindow()
    }

    private fun updateView() {
        mRotateDegrees += 30;
        mRotateDegrees = if (mRotateDegrees < 360) mRotateDegrees else mRotateDegrees - 360
        invalidate();
        if (mNeedToUpdateView) {
            postDelayed(mUpdateViewRunnable, mFrameTime);
        }
    }
}