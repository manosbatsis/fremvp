package com.cyberlogitec.freight9.ui.youroffers

import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.fragment.app.FragmentManager
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import kotlinx.android.synthetic.main.popup_your_offers_detail_jump.view.*

class YourOffersDetailJumpPopup(
        var view: View,
        width: Int,
        height: Int,
        focusable: Boolean,
        var fragmetManager: FragmentManager,
        var onCancelOrRevise: ((Boolean) -> Unit)) : PopupWindow(view, width, height, focusable) {

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0)
    }

    fun initValue() {
        setListener()
    }

    private fun setListener() {
        with(view) {
            btn_your_offers_detail_discard.setSafeOnClickListener {
                showCancelDialog(this, true)
            }
            btn_your_offers_detail_revise.setSafeOnClickListener {
                showCancelDialog(this, false)
            }
        }
    }

    private fun showCancelDialog(view: View, isCancelOffer: Boolean) {
        with(view) {
            val dialog = NormalTwoBtnDialog(title = context.getString(
                    if (isCancelOffer) {
                        R.string.your_offer_edit_dialog_discard_title
                    } else {
                        R.string.your_offer_edit_dialog_revise_title
                    }),
                    desc = if (isCancelOffer) {
                        context.getString(R.string.your_offer_edit_dialog_discard_desc)
                    } else {
                        context.getString(R.string.your_offer_edit_dialog_revise_desc)
                    },
                    leftBtnText = context.getString(R.string.your_offer_edit_dialog_cancel),
                    rightBtnText = context.getString(R.string.your_offer_edit_dialog_yes))
            dialog.isCancelable = false
            dialog.setOnClickListener(View.OnClickListener {
                it?.let {
                    // dismiss Dialog
                    dialog.dismiss()
                    if (it.id == R.id.btn_right) {
                        // dismiss PopupWindow
                        dismiss()
                        /*
                        * isCancel Offer : true  - Discard Offer
                        * isCancel Offer : false - Revise Offer
                        * */
                        onCancelOrRevise(isCancelOffer)
                    }
                }
            })
            dialog.show(fragmetManager, dialog.CLASS_NAME)
        }
    }
}


