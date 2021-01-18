package com.cyberlogitec.freight9.ui.selloffer

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.App
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.RequiresActivityViewModel
import com.cyberlogitec.freight9.config.PaymentPlanCode.PF
import com.cyberlogitec.freight9.config.PaymentPlanCode.PL
import com.cyberlogitec.freight9.config.PaymentPlanCode.PS
import com.cyberlogitec.freight9.config.PaymentTermCode.CCT
import com.cyberlogitec.freight9.config.PaymentTermCode.PPD
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_sof_condition.*
import kotlinx.android.synthetic.main.appbar_sof_condition.*
import kotlinx.android.synthetic.main.body_sof_condition.*
import kotlinx.android.synthetic.main.toolbar_common.view.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


@RequiresActivityViewModel(value = SofConditionVm::class)
class SofConditionAct : BaseActivity<SofConditionVm>() {

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    companion object {
        var mContract = Contract()
        var mPaymentPlans: MutableList<PaymentPlan> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_sof_condition)
        (application as App).component.inject(this)

        setRxOutputs()
        initData()
        initView()
    }

    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
        // init type face (font)
        ResourcesCompat.getFont(this, R.font.opensans_regular)?.let{
            tfRegular = it
        }
        ResourcesCompat.getFont(this, R.font.opensans_bold)?.let {
            tfBold = it
        }
    }

    private fun initPaymentRatio() {
        for (paymentPlan in mPaymentPlans) {
            when (paymentPlan.paymentPlanCode) {
                PS -> {
                    tv_basic_rate_initial.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString ())
                    tv_basic_rate_midterm.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                    tv_basic_rate_remainder.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())
                }
                PF -> {
                    tv_first_rate_initial.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString())
                    tv_first_rate_midterm.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                    tv_first_rate_remainder.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())
                }
                PL -> {
                    tv_last_rate_initial.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString())
                    tv_last_rate_midterm.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                    tv_last_rate_remainder.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())
                }
            }
        }
    }

    private fun setPaymentValue(paymentPlan: String) {
        ////////////////////////////////////////////////////////////////////////////
        // total deal amount
        // filter offerQty smaller than one (1)
        var offerPrices = 0
        mContract.masterContractLineItems?.let{ lineItems ->
            offerPrices = lineItems.sumBy{ it.offerPrice * it.offerQty }
            tv_sof_conditiontotal_deal_amount.text = currencyFormat.format(offerPrices)
        }

        val findPaymentPlan = mPaymentPlans.find { it.paymentPlanCode == paymentPlan }
        findPaymentPlan?.let { plan ->
            when(plan.paymentPlanCode) {
                PS -> {
                    tv_basic_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(plan.initialPaymentRatio), offerPrices))
                    tv_basic_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(plan.middlePaymentRatio), offerPrices))
                    tv_basic_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(plan.balancePaymentRatio), offerPrices))
                }
                PF -> {
                    tv_first_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(plan.initialPaymentRatio), offerPrices))
                    tv_first_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(plan.middlePaymentRatio), offerPrices))
                    tv_first_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(plan.balancePaymentRatio), offerPrices))
                }
                PL -> {
                    tv_last_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(plan.initialPaymentRatio), offerPrices))
                    tv_last_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(plan.middlePaymentRatio), offerPrices))
                    tv_last_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(plan.balancePaymentRatio), offerPrices))
                }
            }
        }
    }

    private fun setPaymentUi(paymentPlanCode: String) {
        // init conditionCode view
        lo_condition1_body.visibility = View.GONE
        lo_condition2_body.visibility = View.GONE
        lo_condition3_body.visibility = View.GONE

        lo_condition1.setBackgroundResource(R.drawable.bg_round_corner_8_white)
        lo_condition2.setBackgroundResource(R.drawable.bg_round_corner_8_white)
        lo_condition3.setBackgroundResource(R.drawable.bg_round_corner_8_white)

        iv_check_condition1.setImageResource(R.drawable.btn_checkbox_default_l)
        iv_check_condition2.setImageResource(R.drawable.btn_checkbox_default_l)
        iv_check_condition3.setImageResource(R.drawable.btn_checkbox_default_l)

        tv_condition1_header.setTextColor(ContextCompat.getColor(this, R.color.greyish_brown))
        tv_condition2_header.setTextColor(ContextCompat.getColor(this, R.color.greyish_brown))
        tv_condition3_header.setTextColor(ContextCompat.getColor(this, R.color.greyish_brown))

        // set conditionCode view
        when (paymentPlanCode) {
            PS -> {
                lo_condition1_body.visibility = View.VISIBLE
                lo_condition1.setBackgroundResource(R.drawable.bg_rectangle_blue_border)
                iv_check_condition1.setImageResource(R.drawable.btn_checkbox_solid_selected_l)
                tv_condition1_header.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            PF -> {
                lo_condition2_body.visibility = View.VISIBLE
                lo_condition2.setBackgroundResource(R.drawable.bg_rectangle_blue_border)
                iv_check_condition2.setImageResource(R.drawable.btn_checkbox_solid_selected_l)
                tv_condition2_header.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            PL -> {
                lo_condition3_body.visibility = View.VISIBLE
                lo_condition3.setBackgroundResource(R.drawable.bg_rectangle_blue_border)
                iv_check_condition3.setImageResource(R.drawable.btn_checkbox_solid_selected_l)
                tv_condition3_header.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        currencyFormat.minimumFractionDigits = 0
        // set custom toolbar
        defaultbarInit(appbar_sof_condition, menuType = MenuType.DONE, title = getString(R.string.selloffer_wizard_plan))
        setListener()
    }

    private fun setRxOutputs() {
        viewModel.outPuts.onClickDone()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: onClickDone")
                        showSaveDialog()
                    }
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        Timber.d("f9: startActivity(SofMakeOfferAct) -> ${it}")
                        startActivity(Intent(this, SofMakeOfferAct::class.java).putExtra(Intents.MSTR_CTRK, it))
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

        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { contract ->
                        Timber.d("f9: onSuccessRefresh")

                        // keep received param
                        mContract = contract

                        setPaymentUi(contract.paymentPlanCode)

                        // payment term
                        when (contract.paymentTermCode) {
                            PPD -> {
                                rgPlanType.check( R.id.rbPrepaid )
                                rbPrepaid.typeface = tfBold
                                rbCollect.typeface = tfRegular
                            }
                            CCT -> {
                                rgPlanType.check(  R.id.rbCollect )
                                rbPrepaid.typeface = tfRegular
                                rbCollect.typeface = tfBold
                            }
                        }
                    }
                }

        viewModel.outPuts.onSuccessRequestPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            mPaymentPlans = it.body() as MutableList<PaymentPlan>
                            initPaymentRatio()
                            setPaymentValue(mContract.paymentPlanCode)
                        } else {
                            showToast("Fail Save Offer(Http)\n" + it.errorBody())
                        }
                    }
                }
    }

    private fun setListener() {
        // wait click event (toolbar left button)
        appbar_sof_condition.toolbar_left_btn.setSafeOnClickListener{
            Timber.d("f9: toolbar_left_btn clcick")
            onBackPressed()
        }

        // on click toolbar right button
        appbar_sof_condition.toolbar_done_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_right_btn click")
            //viewModel.inPuts.clickToMenu(Parameter.CLICK)
            viewModel.inPuts.clickToDone(Parameter.CLICK)
        }

        btn_condition_next.setSafeOnClickListener {
            Timber.d("f9: btn_condition_next")

            viewModel.inPuts.clickToNext( mContract )
        }

        // Basic Payment Plan
        lo_condition1.setSafeOnClickListener {
            Timber.d("f9: Basic Payment Plan click")
            clickToPlan(PS)
        }

        // Collect-First Payment Plan
        lo_condition2.setSafeOnClickListener {
            Timber.d("f9: Collect-First Payment Plan click")
            clickToPlan(PF)
        }

        // Collect-Later Payment Plan
        lo_condition3.setSafeOnClickListener {
            Timber.d("f9: Collect-Later Payment Plan click")
            clickToPlan(PL)
        }

        // Prepaid
        rbPrepaid.setSafeOnClickListener {
            Timber.d("f9: rb Prepaid click")
            mContract.paymentTermCode = PPD
            rbPrepaid.typeface = tfBold
            rbCollect.typeface = tfRegular
        }

        // Collect
        rbCollect.setSafeOnClickListener {
            Timber.d("f9: rb Collect click")
            mContract.paymentTermCode = CCT
            rbPrepaid.typeface = tfRegular
            rbCollect.typeface = tfBold
        }
    }

    private fun clickToPlan(paymentPlan: String) {
        mContract.paymentPlanCode = paymentPlan
        setPaymentUi(paymentPlan)
        setPaymentValue(paymentPlan)
    }

    private fun showSaveDialog() {
        val dialog = NormalTwoBtnDialog(title = getString(R.string.offer_save_dialog_title),
                desc = getString(R.string.offer_save_dialog_desc),
                leftBtnText = getString(R.string.offer_save_dialog_discard),
                rightBtnText = getString(R.string.offer_save_dialog_save))
        dialog.isCancelable = false
        dialog.setOnClickListener(View.OnClickListener {
            it?.let {
                dialog.dismiss()
                if (it.id == R.id.btn_right) {
                    // TODO : Save
                    showToast("SAVE offer")
                } else {
                    finish()
                }
            }
        })
        dialog.show(this.supportFragmentManager, dialog.CLASS_NAME)
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()
}