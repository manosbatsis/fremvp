package com.cyberlogitec.freight9.lib.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.cyberlogitec.freight9.R
import kotlinx.android.synthetic.main.body_batch.view.*

/**
 * valueInt : edittext 에 설정될 값
 * isOver : true (over), false (not over)
 */
fun Context.setBatchInputEditTextIsOver(
        etBatchInput: EditText,
        value: String,
        isOver: Boolean) {

    val etBackground = if (isOver) {
        R.drawable.bg_order_edittext_orangey_red_border
    } else {
        R.drawable.bg_order_edittext_blue_border
    }

    val etTextColor = if (isOver) {
        R.color.orangey_red
    } else {
        R.color.blue_violet
    }

    etBatchInput.setText(value)
    etBatchInput.setBackgroundResource(etBackground)
    etBatchInput.setTextColor(getColor(etTextColor))
    etBatchInput.elevation = 3.0f
    if (isOver) {
        val shake = AnimationUtils.loadAnimation(this, R.anim.edittext_shake)
        etBatchInput.startAnimation(shake)
    }
}

/**
 * isNormal : true (disable), false (enable)
 * isValueSet : true (value 설정), false (value 미설정)
 * value : 설정될 값
 */
fun Context.setBatchInputEditTextIsNormal(
        etBatchInput: EditText,
        isNormal: Boolean,
        isValueSet: Boolean,
        value: String = "") {

    val etBackground = if (isNormal) {
        R.drawable.bg_order_edittext_gray_border
    } else {
        R.drawable.bg_order_edittext_blue_border
    }

    val etTextColor = if (isNormal) {
        R.color.color_333333
    } else {
        R.color.blue_violet
    }

    val elevation = if (isNormal) {
        0.0F
    } else {
        3.0F
    }

    etBatchInput.setBackgroundResource(etBackground)
    etBatchInput.setTextColor(getColor(etTextColor))
    etBatchInput.elevation = elevation
    if (isValueSet) {
        etBatchInput.setText(value)
    }
}

/**
 * Batch Input, Row Input 시 Layout의 선택한 EditText 를 keypad 위로 올리기 위해
 * Height 를 계산하고, NestedScrollView 를 Scroll 시킴
 */
@SuppressLint("RestrictedApi")
fun Context.setScrollHeight(
        visibleDisplayFrameHeight: Int = 0,
        adapterCurrentPosition: Int = 0,
        referencedLayout: ReferencedCalcHeightLayout) {

    val location: IntArray = intArrayOf(0, 0)

    with(referencedLayout) {
        // Batch Input 인 경우
        if ((batchInputLayout.visibility == View.VISIBLE) and batchInputCheckBox.isChecked) {
            // Recycler view 의 타이틀의 위치
            recyclerTitleLayout.getLocationOnScreen(location)
        }
        // 개별입력, Max 인 경우
        else {
            // Recycler view 의 위치
            recyclerView.getLocationOnScreen(location)
        }

        var itemPosition = location[1]

        // 개별입력인 경우 선택한 item 의 위치까지 add
        if (batchLayout.visibility == View.VISIBLE) {
            val count = recyclerView.layoutManager!!.childCount
            for (x in 0 until count) {
                val itemView = recyclerView.getChildAt(x)
                if (itemView is CardView) {
                    itemPosition += itemView.height
                    if (x == adapterCurrentPosition) {
                        break
                    }
                }
            }
        }

        var addedOtherHeight = 0
        // 인벤토리 정보 layout 이 보이는 경우 높이
        if (inventoryFloatingLayout.visibility == View.VISIBLE) {
            addedOtherHeight = inventoryFloatingLayout.height
        }
        // 입력 layout 이 보이는 경우 높이
        if (quantityInputFloatingLayout.visibility == View.VISIBLE) {
            addedOtherHeight += quantityInputFloatingLayout.height
        }

        var statusHeight = 0
        val statusbarId = resources.getIdentifier("status_bar_height", "dimen", "android")

        // 상태바의 높이
        if (statusbarId > 0) statusHeight = resources.getDimensionPixelSize(statusbarId)

        // 키패드 + Input 영역을 제외한 위치(높이)
        val keypadOutterLayoutPosition = visibleDisplayFrameHeight + statusHeight - addedOtherHeight

        // NestedScrollView 의 Offset
        val svOffset = nsvBodyRoot.computeVerticalScrollOffset()
        val diffPosition = svOffset + itemPosition - keypadOutterLayoutPosition
        if (diffPosition > 0) {
            nsvBodyRoot.scrollTo(0, diffPosition)
        }
    }
}

/**
 * Batch Input, Row Input 시 Keypad가 Show 이면 입력 EditText을 가진 layout을 keypad 위에 올리고
 * Keypad가 hide 이면 해당 layout을 gone 시킴
 */
fun Context.showQuantityEditTextKeypad(
        im: InputMethodManager,
        show: Boolean,
        value: String,
        referencedShowKeypadLayout: ReferencedShowKeypadLayout) {

    with(referencedShowKeypadLayout) {
        when (show) {
            true -> {
                quantityInputEditText.requestFocus()
                quantityInputEditText.setText(value)
                quantityInputEditText.setSelection(quantityInputEditText.text.length)
                quantityInputEditText.showSoftInputOnFocus = false
                im.showSoftInput(quantityInputEditText, InputMethodManager.SHOW_IMPLICIT)
            }
            else -> {
                im.hideSoftInputFromWindow(quantityInputEditText.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                quantityInputEditText.clearFocus()
            }
        }
        showLayoutByKeypad(show, referencedShowKeypadLayout)
    }
}

fun Context.showLayoutByKeypad(
        showKeypad: Boolean,
        referencedShowKeypadLayout: ReferencedShowKeypadLayout) {

    with(referencedShowKeypadLayout) {
        val isShow = if (chkBatchMax.isChecked) false else showKeypad

        bottomFloatingLayout.visibility = if (isShow) View.VISIBLE else View.GONE
        bottomBtnFloatingLayout.visibility = if (bottomFloatingLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE

        val llInventory = inventoryFloatingLayout.visibility
        val bottomMargin = if (isShow) {
            if (llInventory != View.VISIBLE) 64.toDp().toInt() else 104.toDp().toInt()
        } else {
            0
        }
        val params = nsvBodyRoot.layoutParams as CoordinatorLayout.LayoutParams
        params.setMargins(0, 0, 0, bottomMargin)
        nsvBodyRoot.layoutParams = params
    }
}

/**
 * Batch input 관련 공통 UI 처리
 */
fun Context.setBatchCheck(isChecked: Boolean, layoutView: View) {
    layoutView.ll_batch.visibility = if (isChecked) View.GONE else View.VISIBLE
    layoutView.ll_batch_input.visibility = if (isChecked) View.VISIBLE else View.GONE
    if (isChecked) {
        layoutView.chk_batch_input.isChecked = true
    }
}

fun Context.setBatchInputCheck(isChecked: Boolean, value: String, layoutView: View) {
    if (isChecked) {
        if (layoutView.chk_batch_max.isChecked) {
            layoutView.chk_batch_max.isChecked = false
        }
        // EditText 에 Focus 표시
        setBatchInputEditTextIsNormal(layoutView.et_batch_input, isNormal = false, isValueSet = true, value = value)
    } else {
        // EditText 에 Focus 표시 안함
        setBatchInputEditTextIsNormal(layoutView.et_batch_input, isNormal = true, isValueSet = true, value = value)
        if (layoutView.chk_batch_max.isChecked.not()) {
            layoutView.chk_batch.isChecked = false
        }
    }
}

fun Context.setBatchMaxCheck(isChecked: Boolean, layoutView: View) {
    if (isChecked) {
        if (layoutView.chk_batch_input.isChecked) {
            layoutView.chk_batch_input.isChecked = false
        }
    } else {
        if (layoutView.chk_batch_input.isChecked.not()) {
            layoutView.chk_batch.isChecked = false
        }
    }

    layoutView.tv_batch_max.setTextColor(getColor(
            if (isChecked) {
                R.color.greyish_brown
            } else {
                R.color.very_light_pink
            }))
}

/**
 * Batch Input, Row Input 시 Layout의 선택한 EditText 를 keypad 위로 올리기 위해
 * Height 를 계산하고, NestedScrollView 를 Scroll 시킴
 * [관련 layout 구조, Type Fix]
 */
data class ReferencedCalcHeightLayout(
        val batchLayout: LinearLayout,                  // Batch layout (ll_batch)
        val batchInputLayout: LinearLayout,             // Batch input layout (ll_batch_input)
        val batchInputCheckBox: CheckBox,               // Batch input checkbox (chk_batch_input)
        val recyclerTitleLayout: LinearLayout,          // Top layout of Recycler + title (ll_volume_set)
        val recyclerView: RecyclerView,                 // RecyclerView (recycler_volume)
        val inventoryFloatingLayout: LinearLayout,      // Inventory floating layout (ll_inventory_floating)
        val quantityInputFloatingLayout: LinearLayout,  // Input floating layout (ll_quantity_input_floating)
        val nsvBodyRoot: NestedScrollView               // NestedScrollView layout (sv_body_root)
)

/**
 * Batch Input, Row Input 시 Keypad가 Show 이면 입력 EditText을 가진 layout을 keypad 위에 올리고
 * Keypad가 hide 이면 해당 layout을 gone 시킴
 * [관련 layout 구조, Type Fix]
 */
data class ReferencedShowKeypadLayout(
        val quantityInputEditText: EditText,            // Input EditText (et_quantity_input_floating)
        val chkBatchMax: CheckBox,                      // Batch input Max checkbox (chk_batch_max)
        val bottomFloatingLayout: LinearLayout,         // Bottom Floating layout (ll_bottom_floating)
        val bottomBtnFloatingLayout: LinearLayout,      // Bottom Button Floating layout (ll_bottom_btn_floating)
        val inventoryFloatingLayout: LinearLayout,      // Inventory Floating layout (ll_inventory_floating)
        val nsvBodyRoot: NestedScrollView               // NestedScrollView layout (sv_body_root)
)