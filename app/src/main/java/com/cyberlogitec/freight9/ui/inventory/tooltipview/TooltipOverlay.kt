package com.cyberlogitec.freight9.ui.inventory.tooltipview


import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.cyberlogitec.freight9.R

class TooltipOverlay : AppCompatImageView {
    private var layoutMargins: Int = 0

    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = R.style.ToolTipOverlayDefaultStyle
               ) : super(context, attrs, defStyleAttr) {
        init(context, R.style.ToolTipLayoutDefaultStyle)
    }

    private fun init(context: Context, defStyleResId: Int) {
        val drawable = TooltipOverlayDrawable(context, defStyleResId)
        setImageDrawable(drawable)

        val array = context.theme.obtainStyledAttributes(defStyleResId, R.styleable.TooltipOverlay)
        layoutMargins = array.getDimensionPixelSize(R.styleable.TooltipOverlay_android_layout_margin, 0)
        array.recycle()

    }

    constructor(context: Context, defStyleAttr: Int, defStyleResId: Int) : super(context, null, defStyleAttr) {
        init(context, defStyleResId)
    }
}