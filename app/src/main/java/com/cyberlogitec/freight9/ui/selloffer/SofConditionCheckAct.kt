package com.cyberlogitec.freight9.ui.selloffer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_CCT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.SellOffer.SELL_OFFER_WIZARD
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.cyberlogitec.freight9.lib.util.startActivityWithFinish
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_PLAN
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_sof_condition_check.*
import kotlinx.android.synthetic.main.body_sof_condition_check.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = SofConditionCheckVm::class)
class SofConditionCheckAct : BaseActivity<SofConditionCheckVm>() {

    private var conditionNo = "1"
    private var totAmonut = 0

    companion object {
        var mContract = Contract()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_condition_check)

        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    private fun initData() {


        setListener()
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)

        // set custom toolbar
        defaultbarInit(appbar_sof_condition_check, menuType = MenuType.CROSS, title = getString(R.string.selloffer_plan), isEnableNavi = false)
    }

    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // onClickPlanDetail

        viewModel.outPuts.onClickPlanDetail()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: startActivity(SofConditionDetailAct)")
                        Toast.makeText(this, "Not supported on MVP !!", Toast.LENGTH_SHORT).show()
                        //startActivity(Intent(this, SofConditionDetailAct::class.java).putExtra(Intents.MSTR_CTRK, it as Contract))
                    }
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // onClickEdit

        viewModel.outPuts.onClickEdit()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(SofConditionAct)")
                    if (SELL_OFFER_WIZARD) {
                        // For Sell Offer Wizard
                        val intent = Intent()
                        intent.putExtra(Intents.SELL_OFFER_STEP, STEP_PLAN)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    } else {
                        // For Sell Offer activity(original)
                        startActivityWithFinish(Intent(this, SofConditionAct::class.java)
                                .putExtra(Intents.MSTR_CTRK, it as Contract)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }

        //------------------------------------------------------------------------------------------

        // etc : java.net.SocketTimeoutException: failed to connect
        viewModel.error
                .bindToLifecycle(this)
                .subscribe {
                    loadingDialog.dismiss()
                    Timber.e("f9: error : $it")
                    showToast("Fail (Throwable)\n" + it.message)
                    finish()
                }

        //------------------------------------------------------------------------------------------

        viewModel.showLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: showLoadingDialog()")
                    loadingDialog.show(this)
                }

        viewModel.hideLoadingDialog
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: hideLoadingDialog() : $it")
                    loadingDialog.dismiss()
                }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // onSuccessRefresh

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh( ${it} ) -> Total Deposit Amount")
                    it?.let { contract ->
                        mContract = contract
                    }
                }

        viewModel.outPuts.onSuccessRequestPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            val currentPaymentPlan = it.body() as PaymentPlan
                            mContract.let { contract ->
                                // total amount = sum of prices * quantities
                                var totalPrice = 0
                                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                                currencyFormat.minimumFractionDigits = 0
                                contract.masterContractLineItems?.let { lineItems ->
                                    totalPrice = lineItems.filter { it.deleteYn != "1" }.map { it.offerPrice * it.offerQty }.sum()
                                    tv_deal_amount.text = currencyFormat.format(totalPrice).toString()
                                }

                                // tv_payment_term (01: PPD, 02:CCT)
                                contract.paymentTermCode.let {
                                    when (it) {
                                        OFFER_PAYMENT_TERM_CODE_PPD -> {
                                            tv_payment_term.text = getString(R.string.payplan_prepaid)
                                        }
                                        OFFER_PAYMENT_TERM_CODE_CCT -> {
                                            tv_payment_term.text = getString(R.string.payplan_collect)
                                        }
                                    }
                                }

                                // paymentPlan
                                currentPaymentPlan.let { paymentPlan ->
                                    tv_initial_payment.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString())
                                    tv_midterm_payment.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                                    tv_ramainder_payment.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())

                                    tv_initial_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.initialPaymentRatio), totalPrice))
                                    tv_midterm_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.middlePaymentRatio), totalPrice))
                                    tv_ramainder_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.balancePaymentRatio), totalPrice))
                                }
                            }
                        } else {
                            showToast("Fail Get Payment(Http)\n" + it.errorBody())
                        }
                    }
                }
    }

    private fun setListener() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // button click event

        appbar_sof_condition_check.toolbar_right_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            onBackPressed()
        }

        tv_collect_condition_on_calendar.setSafeOnClickListener {
            Timber.d("tv_collect_condition_on_calendar click")
            viewModel.inPuts.clickToPlanDetail(Parameter.CLICK)
        }

        btn_edit.setSafeOnClickListener {
            btn_edit.isEnabled = false
            viewModel.inPuts.clickToEdit(Parameter.CLICK)
        }
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()
}