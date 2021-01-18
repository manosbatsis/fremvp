package com.cyberlogitec.freight9.ui.inventory.progressview

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

/** dp size to px size. */
internal fun View.dp2Px(dp: Int): Int {
  val scale = resources.displayMetrics.density
  return (dp * scale).toInt()
}

/** sp size to px size. */
internal fun View.sp2Px(sp: Float): Float {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
}

/** px size to sp size. */
internal fun View.px2Sp(px: Float): Float {
  return px / resources.displayMetrics.scaledDensity
}

/** gets color from the ContextCompat. */
internal fun View.compatColor(color: Int): Int {
  return ContextCompat.getColor(context, color)
}

/** updates [FrameLayout] params. */
internal fun ViewGroup.updateLayoutParams(block: ViewGroup.LayoutParams.() -> Unit) {
  layoutParams?.let {
    val params: ViewGroup.LayoutParams = (layoutParams as ViewGroup.LayoutParams).apply { block(this) }
    layoutParams = params
  }
}
