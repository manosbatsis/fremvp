package com.cyberlogitec.freight9.ui.buyoffer

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
import com.cyberlogitec.freight9.config.RdTermCode.CY_CY
import com.cyberlogitec.freight9.config.RdTermCode.CY_DR
import com.cyberlogitec.freight9.config.RdTermCode.DR_CY
import com.cyberlogitec.freight9.config.RdTermCode.DR_DR
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.ui.dialog.NormalTwoBtnDialog
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.Intents.Companion.CARRIER_LIST
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.lib.util.showToast
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.appbar_bof_condition.*
import kotlinx.android.synthetic.main.bottom_bof_condition.*
import kotlinx.android.synthetic.main.item_bof_condition_carrier.*
import kotlinx.android.synthetic.main.item_bof_condition_plan.*
import kotlinx.android.synthetic.main.item_bof_condition_rdterm.*
import kotlinx.android.synthetic.main.toolbar_common.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


@RequiresActivityViewModel(value = BofConditionVm::class)
class BofConditionAct : BaseActivity<BofConditionVm>() {

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    companion object {
        var mOffer = Offer()
        var mCarriers = mutableListOf<OfferCarrier>()
        var mPaymentPlans: MutableList<PaymentPlan> = java.util.ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.v("f9: onCreate")

        setContentView(R.layout.act_bof_condition)
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

    private fun initView() {
        // set status bar
        window.statusBarColor = getColor(R.color.title_bar)
        currencyFormat.minimumFractionDigits = 0
        // set custom toolbar
        defaultbarInit(appbar_bof_term, menuType = MenuType.DONE, title = getString(R.string.buy_offer_wizard_condition))
        setListener()
    }

    private fun setRxOutputs() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // outPuts
        viewModel.outPuts.onSuccessRefresh()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: onSuccessRefresh +")
                    it?.let { offer ->
                        mOffer = offer

                        offer.offerPaymentPlanCode?.let { paymentPlanCode ->
                            setPaymentUi(paymentPlanCode)
                        }

                        offer.offerPaymentTermCode.let { paymentTermCode ->
                            // payment term
                            when (paymentTermCode) {
                                PPD -> {
                                    rgPlanType.check(R.id.rbPrepaid)
                                    rbPrepaid.typeface = tfBold
                                    rbCollect.typeface = tfRegular
                                }
                                CCT -> {
                                    rgPlanType.check(R.id.rbCollect)
                                    rbPrepaid.typeface = tfRegular
                                    rbCollect.typeface = tfBold
                                }
                            }
                        }

                        offer.offerRdTermCode.let { rdTermCode ->
                            setRdTerm(rdTermCode)
                        }
                    }
                    Timber.d("f9: onSuccessRefresh -")
                }

        viewModel.outPuts.onSuccessRefresh2()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        // onResume 시 all carrier 로 reset 되므로 clear 인 경우에만 대입
                        if (mCarriers.isEmpty()) {
                            mCarriers = it.toMutableList()
                        }
                    }
                }

        viewModel.outPuts.onClickNext()
                .bindToLifecycle(this)
                .subscribe {
                    Timber.d("f9: startActivity(BofMakeOfferAct)")
                    startActivity(Intent(this, BofMakeOfferAct::class.java).putExtra(Intents.OFFER, it as Offer))
                }

        viewModel.outPuts.onSuccessRequestPaymentPlan()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let {
                        if (it.isSuccessful) {
                            mPaymentPlans = it.body() as MutableList<PaymentPlan>
                            initPaymentRatio()
                            setPaymentValue(mOffer.offerPaymentPlanCode)
                        } else {
                            showToast("Fail Save Offer(Http)\n" + it.errorBody())
                        }
                    }
                }
    }

    private fun setListener() {
        // on click toolbar right button
        toolbar_left_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_left_btn click")
            onBackPressed()
            //viewModel.inPuts.clickToBack(Parameter.CLICK)
        }

        toolbar_done_btn.setSafeOnClickListener {
            Timber.d("f9: toolbar_done_btn click")
            showSaveDialog()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // conditionCode click
        lo_condition1.setSafeOnClickListener {
            Timber.d("lo_condition1 click")
            clickToPlan(PS)

            mOffer.offerPaymentPlanCode = PS
            viewModel.inPuts.clickToPlan( mOffer )
        }

        lo_condition2.setSafeOnClickListener {
            Timber.d("f9: lo_condition2 click")
            clickToPlan(PF)

            mOffer.offerPaymentPlanCode = PF
            viewModel.inPuts.clickToPlan( mOffer )
        }

        lo_condition3.setSafeOnClickListener {
            Timber.d("f9: lo_condition3 click")
            clickToPlan(PL)
            mOffer.offerPaymentPlanCode = PL
            viewModel.inPuts.clickToPlan( mOffer )
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // paymentTerm radio click
        rbPrepaid.setSafeOnClickListener {
            Timber.d("f9: rbPrepaid click")

            mOffer.offerPaymentTermCode = PPD
            rbPrepaid.typeface = tfBold
            rbCollect.typeface = tfRegular
        }

        rbCollect.setSafeOnClickListener {
            Timber.d("f9: rbCollect click")

            mOffer.offerPaymentTermCode = CCT
            rbPrepaid.typeface = tfRegular
            rbCollect.typeface = tfBold
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // next
        btn_bof_condition_next_floating.setSafeOnClickListener {
            Timber.d("f9: btn_bof_condition_next_floating click")
            /**
             * TODO : container types - Dry Container Only in MVP - Fix : F_TYPE, N20FT_TYPE
             *
             * 현재 android:enabled=false 로 설정되어 있음
             *
             * [Dry Container]
             * btn_dry_20ft.isChecked
             * btn_dry_40ft.isChecked
             * btn_dry_40hc.isChecked
             * btn_dry_45hc.isChecked
             * [Reefer Container]
             * btn_reefer_20ft.isChecked
             * btn_reefer_40ft.isChecked
             * btn_reefer_40hc.isChecked
             * btn_reefer_45hc.isChecked
             * [Empty Container]
             * btn_empty_20ft.isChecked
             * btn_empty_40ft.isChecked
             * btn_empty_40hc.isChecked
             * btn_empty_45hc.isChecked
             * [Dry Container]
             * btn_soc_20ft.isChecked
             * btn_soc_40ft.isChecked
             * btn_soc_40hc.isChecked
             * btn_soc_45hc.isChecked
             */

            // carriers
            mOffer.offerCarriers = mCarriers.filter { carrier -> carrier.isChecked }
            viewModel.inPuts.clickToNext( mOffer )
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // carriers
        tv_carrier_edit.setSafeOnClickListener {
            RxActivityResult(this)
                    .start( Intent(this, BofCarrierAct::class.java).putExtra(CARRIER_LIST, ArrayList(mCarriers) ) )
                    .subscribe(
                            {
                                if (it.isOk) {
                                    val carriers = it.data.getSerializableExtra(CARRIER_LIST) as List<OfferCarrier>
                                    mCarriers.clear()
                                    mCarriers.addAll(carriers)
                                    setCarriers(carriers)
                                } else {
                                    Timber.d("f9: NotOK --> resultCode:  ${it.resultCode}")
                                }
                            },
                            {
                                viewModel.error.onNext(it)
                            }
                    )

        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // rdterm
        lo_rdterm_cy_cy.setSafeOnClickListener {
            setRdTerm(CY_CY)
        }

        lo_rdterm_cy_door.setSafeOnClickListener {
            setRdTerm(CY_DR)
        }

        lo_rdterm_door_cy.setSafeOnClickListener {
            setRdTerm(DR_CY)
        }

        lo_rdterm_door_door.setSafeOnClickListener {
            setRdTerm(DR_DR)
        }
    }

    private fun clickToPlan(paymentPlan: String) {
        mOffer.offerPaymentPlanCode = paymentPlan
        setPaymentUi(paymentPlan)
        setPaymentValue(paymentPlan)
    }

    private fun setRdTerm(rdTermCode: String) {
        tv_rdterm_cy_cy.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_rdterm_cy_door.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_rdterm_door_cy.setTextAppearance(R.style.txt_opensans_r_15_595959)
        tv_rdterm_door_door.setTextAppearance(R.style.txt_opensans_r_15_595959)
        iv_rdterm_cy_cy.visibility = View.INVISIBLE
        iv_rdterm_cy_door.visibility = View.INVISIBLE
        iv_rdterm_door_cy.visibility = View.INVISIBLE
        iv_rdterm_door_door.visibility = View.INVISIBLE
        when(rdTermCode) {
            CY_CY -> {
                tv_rdterm_cy_cy.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_cy_cy.visibility = View.VISIBLE
            }
            CY_DR -> {
                tv_rdterm_cy_door.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_cy_door.visibility = View.VISIBLE
            }
            DR_CY -> {
                tv_rdterm_door_cy.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_door_cy.visibility = View.VISIBLE
            }
            DR_DR -> {
                tv_rdterm_door_door.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_door_door.visibility = View.VISIBLE
            }
        }
        mOffer.offerRdTermCode = rdTermCode
    }

    private fun setCarriers(carriers: List<OfferCarrier>) {
        val size = carriers.size
        val checkedSize = carriers.filter { carrier -> carrier.isChecked }.size
        val allCarriers = size == checkedSize
        val firstCarrier = carriers.first()
        iv_carrier.setImageResource(if (allCarriers) {
            getString(R.string.all_carriers).getCarrierIcon(false)
        } else {
            firstCarrier.offerCarrierCode.getCarrierIcon(false)
        })
        tv_carrier_code.text = if (allCarriers) getString(R.string.all_carriers) else firstCarrier.offerCarrierCode
        tv_carrier_count.text = if (allCarriers) "" else { if (checkedSize > 1) String.format("+%d", checkedSize - 1) else "" }
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

    private fun setPaymentValue(paymentPlanCode: String?) {
        ////////////////////////////////////////////////////////////////////////////
        // total deal amount
        // filter offerQty smaller than one (1)
        paymentPlanCode?.let {
            var offerPrices = 0
            mOffer.offerLineItems?.let { lineItems ->
                offerPrices = lineItems.sumBy { it.offerPrice * it.offerQty }
                tv_sof_conditiontotal_deal_amount.text = currencyFormat.format(offerPrices)
            }

            val paymentPlan = mPaymentPlans.find { it.paymentPlanCode == paymentPlanCode }
            when (paymentPlan!!.paymentPlanCode) {
                PS -> {
                    tv_basic_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.initialPaymentRatio), offerPrices))
                    tv_basic_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.middlePaymentRatio), offerPrices))
                    tv_basic_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.balancePaymentRatio), offerPrices))
                }
                PF -> {
                    tv_first_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.initialPaymentRatio), offerPrices))
                    tv_first_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.middlePaymentRatio), offerPrices))
                    tv_first_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.balancePaymentRatio), offerPrices))
                }
                PL -> {
                    tv_last_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.initialPaymentRatio), offerPrices))
                    tv_last_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.middlePaymentRatio), offerPrices))
                    tv_last_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(paymentPlan.balancePaymentRatio), offerPrices))
                }
            }
        }
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