package com.cyberlogitec.freight9.ui.inventory

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.lib.model.InventoryDetails
import com.cyberlogitec.freight9.lib.model.MasterContractWithInventory
import com.cyberlogitec.freight9.lib.model.RxBusEvent
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.EVENT_FINISH
import com.cyberlogitec.freight9.lib.model.RxBusEvent.Companion.FINISH_INVENTORY
import com.cyberlogitec.freight9.lib.rx.RxBus
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.ui.booking.BookingDashboardActivity
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity
import com.cyberlogitec.freight9.ui.youroffers.YourOffersActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.popup_inventory_detail_jump.view.*
import timber.log.Timber


class InventoryDetailJumpPopup(var view: View, width: Int, height: Int, focusable: Boolean,
                               private var viewModel: InventoryViewModel) :
        PopupWindow(view, width, height, focusable) {

    private lateinit var masterContractWithInventory: MasterContractWithInventory
    private lateinit var inventoryDetailItem: InventoryDetails.InventoryDetail
    private var goto: InventoryDetailPopupActivity.Goto = InventoryDetailPopupActivity.Goto.GO_IN_STOCK


    /**
     * [In-Stock] : NEW SELL OFFER, NEW BOOKING
     * [on Market] : YOUR SELL OFFER LIST, YOUR BUY OFFER LIST
     * [Expired] : TRANSACTION STATEMENT
     * [Booked] : BOOKING DASHBOARD
     * [Sold] : TRANSACTION STATEMENT
     */

    init {
        this.viewModel.outPuts.gotoDetailJumpPopupFirstItem()
                .bindToLifecycle(view)
                .subscribe { gotoMove ->
                    with(view.context) {
                        when (gotoMove) {
                            InventoryDetailPopupActivity.Goto.GO_IN_STOCK -> {
                                startActivity(Intent(this, SofWizardActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        .putExtra(Intents.MSTR_CTRK_NR, masterContractWithInventory.masterContractNumber))
                            }
                            InventoryDetailPopupActivity.Goto.GO_ON_MARKET -> {
                                startActivity(Intent(this, YourOffersActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        .putExtra(Intents.YOUR_OFFER_TYPE, OFFER_TYPE_CODE_SELL))
                            }
                            InventoryDetailPopupActivity.Goto.GO_EXPIRED -> {
                                showToast(getString(R.string.your_inventory_next_transaction_statement))
                            }
                            InventoryDetailPopupActivity.Goto.GO_BOOKED -> {
                                startActivity(Intent(this, BookingDashboardActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) )
                            }
                            InventoryDetailPopupActivity.Goto.GO_SOLD -> {
                                showToast(getString(R.string.your_inventory_next_transaction_statement))
                            }
                            else -> { }
                        }
                        dismiss()
                        RxBus.publish(RxBusEvent(EVENT_FINISH, FINISH_INVENTORY))
                    }
                }
        this.viewModel.outPuts.gotoDetailJumpPopupSecondItem()
                .bindToLifecycle(view)
                .subscribe {
                    with(view.context) {
                        when (it) {
                            InventoryDetailPopupActivity.Goto.GO_IN_STOCK -> {
                                showToast(getString(R.string.your_inventory_next_new_booking))
                            }
                            InventoryDetailPopupActivity.Goto.GO_ON_MARKET -> {
                                startActivity(Intent(this, YourOffersActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        .putExtra(Intents.YOUR_OFFER_TYPE, OFFER_TYPE_CODE_BUY))
                            }
                            else -> { }
                        }
                        dismiss()
                        RxBus.publish(RxBusEvent(EVENT_FINISH, FINISH_INVENTORY))
                    }
                }
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.CENTER, 0, 0 )

    }

    fun initValue(masterContractWithInventory: MasterContractWithInventory,
                  inventoryDetail: InventoryDetails.InventoryDetail,
                  goto: InventoryDetailPopupActivity.Goto) {
        Timber.d("f9: initValue : $masterContractWithInventory")
        this.masterContractWithInventory = masterContractWithInventory
        this.inventoryDetailItem = inventoryDetail
        this.goto = goto
        setData()
        setListener()
    }

    private fun setData() {
        with(view) {
            btn_inventory_detail_jump_1.visibility = View.VISIBLE
            btn_inventory_detail_jump_2.visibility = View.INVISIBLE
            when (goto) {
                InventoryDetailPopupActivity.Goto.GO_IN_STOCK -> {
                    tv_inventory_detail_jump_title.text = context.getString(R.string.your_inventory_in_stock)
                    btn_inventory_detail_jump_1.text = context.getString(R.string.your_inventory_next_new_sell_offer)
                    btn_inventory_detail_jump_2.visibility = View.VISIBLE
                    btn_inventory_detail_jump_2.text = context.getString(R.string.your_inventory_next_new_booking)
                }
                InventoryDetailPopupActivity.Goto.GO_ON_MARKET -> {
                    tv_inventory_detail_jump_title.text = context.getString(R.string.your_inventory_on_market)
                    btn_inventory_detail_jump_1.text = context.getString(R.string.your_inventory_next_your_sell_offer_list)
                    btn_inventory_detail_jump_2.visibility = View.VISIBLE
                    btn_inventory_detail_jump_2.text = context.getString(R.string.your_inventory_next_your_buy_offer_list)
                }
                InventoryDetailPopupActivity.Goto.GO_EXPIRED -> {
                    tv_inventory_detail_jump_title.text = context.getString(R.string.your_inventory_expired)
                    btn_inventory_detail_jump_1.text = context.getString(R.string.your_inventory_next_transaction_statement)
                }
                InventoryDetailPopupActivity.Goto.GO_BOOKED -> {
                    tv_inventory_detail_jump_title.text = context.getString(R.string.your_inventory_booked)
                    btn_inventory_detail_jump_1.text = context.getString(R.string.your_inventory_next_booking_dashboard)
                }
                InventoryDetailPopupActivity.Goto.GO_SOLD -> {
                    tv_inventory_detail_jump_title.text = context.getString(R.string.your_inventory_sold)
                    btn_inventory_detail_jump_1.text = context.getString(R.string.your_inventory_next_transaction_statement)
                }
            }
        }
    }

    private fun setListener() {
        with(view) {
            btn_inventory_detail_jump_1.setSafeOnClickListener {
                viewModel.inPuts.clickDetailJumpPopupFirstItem(goto)
            }
            btn_inventory_detail_jump_2.setSafeOnClickListener {
                viewModel.inPuts.clickDetailJumpPopupSecondItem(goto)
            }
        }
    }
}

