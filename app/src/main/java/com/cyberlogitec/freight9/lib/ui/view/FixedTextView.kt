package com.cyberlogitec.freight9.lib.ui.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import com.cyberlogitec.freight9.ui.inventory.progressview.px2Sp
import com.cyberlogitec.freight9.ui.inventory.progressview.sp2Px


class FixedTextView: androidx.appcompat.widget.AppCompatTextView {

    constructor(context: Context?): super(context)

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)



    private fun refitText(text: String, textWidth: Int) {
        var minFontSize = 1
        var maxFontSize = 13
        val testArray = text.lineSequence()
        //find max length
        val maxText = testArray.maxBy { it.length }

        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.typeface = paint.typeface
        mPaint.textSize = textSize

        var toSize = minFontSize

        // find size

        for (size in  sp2Px(maxFontSize.toFloat()).toInt() downTo sp2Px(minFontSize.toFloat()).toInt()) {
            mPaint.textSize = size.toFloat()
            if(mPaint.measureText(maxText) <= textWidth) {
                toSize = size
                break
            }
        }

        setTextSize(TypedValue.COMPLEX_UNIT_SP, px2Sp(toSize.toFloat()))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        refitText(this.text.toString(), parentWidth)
        setMeasuredDimension(parentWidth, parentHeight)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        refitText(text.toString(), this.width);
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw) {
            refitText(this.text.toString(), w);
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

}