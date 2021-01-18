package com.cyberlogitec.freight9.ui.finance

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import com.cyberlogitec.freight9.config.Constant.EmptyString
import kotlinx.android.synthetic.main.popup_search_common.view.*


class FinanceSearchPopup(
        var view: View,
        width: Int,
        height: Int,
        focusable: Boolean,
        searchString: String,
        hintString: String,
        onSearchWordInput: ((String) -> Unit)) : PopupWindow(view, width, height, focusable) {

    private var currentEditText: View
    private var im = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    init {
        this.isTouchable = true

        with(view) {
            currentEditText = et_search
            et_search.hint = hintString
            et_search.requestFocus()
            et_search.setText(searchString)
            et_search.setSelection(et_search.text.length)
            et_search.showSoftInputOnFocus = true
            showHideClear(searchString)
            showHideKeypad(true)

            view_search_top.setOnClickListener {
                it?.let {
                    dismiss()
                }
            }

            view_search_bottom.setOnClickListener {
                it?.let {
                    dismiss()
                }
            }

            et_search.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    showHideKeypad(true)
                    v.performClick()
                }
                true
            }

            et_search.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSearchWordInput(et_search.text.toString())
                    dismiss()
                }
                false
            }

            et_search.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    showHideClear(p0.toString())
                }
            })

            iv_search_clear.setOnClickListener {
                et_search.setText(EmptyString)
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        showHideKeypad(false)
    }

    private fun showHideClear(searchWord: String) {
        view.iv_search_clear.visibility = if (searchWord.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun showHideKeypad(isShow: Boolean) {
        if (isShow) {
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            im.showSoftInput(currentEditText, 0)
        } else {
            im.hideSoftInputFromWindow(currentEditText.windowToken, 0)
        }
    }
}

