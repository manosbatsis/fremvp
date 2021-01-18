package com.cyberlogitec.freight9.lib.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import com.cyberlogitec.freight9.R


class GradientTextView: androidx.appcompat.widget.AppCompatTextView {

    var mFadeWidth: Int = 0
    var mFadeColor: Int = R.color.black

    constructor(context: Context?): super(context)

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs){
        mFadeWidth = context?.obtainStyledAttributes(attrs, R.styleable.GradientTextView)
                ?.getDimensionPixelSize(R.styleable.GradientTextView_fading_width, 0)!!
        mFadeColor = context?.obtainStyledAttributes(attrs,R.styleable.GradientTextView)
                ?.getColor(R.styleable.GradientTextView_fading_color, currentTextColor)!!

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas?) {
        if(paint.measureText(text.toString()) > width) {
            paint.setShader(LinearGradient((right-mFadeWidth).toFloat(),top.toFloat(), right.toFloat(), top.toFloat(), currentTextColor, mFadeColor, Shader.TileMode.CLAMP))
        }else {
            paint.setShader(LinearGradient(0f,0f, width.toFloat(), height.toFloat(), currentTextColor, currentTextColor, Shader.TileMode.CLAMP))
        }
        super.onDraw(canvas)
    }

}