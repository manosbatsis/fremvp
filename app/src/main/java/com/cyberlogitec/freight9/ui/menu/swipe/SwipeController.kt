package com.cyberlogitec.freight9.ui.menu.swipe

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.util.capitalizeWords
import com.cyberlogitec.freight9.lib.util.toPx
import timber.log.Timber
import java.util.*

// https://github.com/FanFataL/swipe-controller-demo
internal enum class ButtonsState {
    GONE, RIGHT_VISIBLE
}

class SwipeController(private var context: Context, private var buttonsActions: SwipeControllerActions) : ItemTouchHelper.Callback() {
    private var swipeBack = false
    private var buttonShowedState = ButtonsState.GONE
    private var buttonInstance: RectF? = null
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = buttonShowedState != ButtonsState.GONE
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
            = makeMovementFlags(0, LEFT)

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        var dx = dX
        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dx = Math.min(dx, -buttonWidth)
                super.onChildDraw(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive)
            } else {
                setTouchListener(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive)
            }
        }
        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive)
        }
        currentItemViewHolder = viewHolder
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (dX < -buttonWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE
                if (buttonShowedState != ButtonsState.GONE) {
                    setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    setItemsClickable(recyclerView, false)
                }
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchDownListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchUpListener(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                   dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        Timber.d("f9: dx = $dX, dy = $dY")
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                super@SwipeController.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState, isCurrentlyActive)
                recyclerView.setOnTouchListener { _, _ -> false }
                setItemsClickable(recyclerView, true)
                swipeBack = false
                if (buttonInstance != null && buttonInstance!!.contains(event.x, event.y)) {
                    if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                        buttonsActions.onRightClicked(viewHolder.bindingAdapterPosition)
                    }
                }
                buttonShowedState = ButtonsState.GONE
                currentItemViewHolder = null
            }
            false
        }
    }

    private fun setItemsClickable(recyclerView: RecyclerView, isClickable: Boolean) {
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).isClickable = isClickable
        }
    }

    private fun drawButtons(canvas: Canvas, viewHolder: RecyclerView.ViewHolder) {
        val itemView: View = viewHolder.itemView
        val paint = Paint()
        val rightButton = RectF(itemView.right - buttonWidth, itemView.top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat())
        paint.color = ContextCompat.getColor(context, R.color.purpley_blue)
        canvas.drawRect(rightButton, paint)
        drawText(context.getString(R.string.delete).toLowerCase(Locale.getDefault()).capitalizeWords(),
                canvas, rightButton, paint)
        buttonInstance = null
       if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton
        }
    }

    private fun drawText(text: String, canvas: Canvas, button: RectF, paint: Paint) {
        val textSize = 13.toPx().toFloat()
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint.textSize = textSize
        paint.typeface = ResourcesCompat.getFont(context, R.font.opensans_bold)!!
        val textWidth = paint.measureText(text)
        canvas.drawText(text, button.centerX() - textWidth / 2, button.centerY() + textSize / 2, paint)
    }

    fun onDraw(c: Canvas) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder!!)
        }
    }

    companion object {
        private var buttonWidth = 76.toPx().toFloat()
    }
}