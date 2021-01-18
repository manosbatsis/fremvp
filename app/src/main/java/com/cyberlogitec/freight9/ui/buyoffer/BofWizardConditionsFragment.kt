package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.Constant
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_CCT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_CYCY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_CYDOOR
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_DOORCY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_DOORDOOR
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PF
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PL
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PS
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.getCarrierIcon
import com.cyberlogitec.freight9.lib.util.getCodeCount
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.STEP_MAKE
import com.petarmarijanovic.rxactivityresult.RxActivityResult
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_bof_condition.*
import kotlinx.android.synthetic.main.bottom_bof_condition.*
import kotlinx.android.synthetic.main.item_bof_condition_carrier.*
import kotlinx.android.synthetic.main.item_bof_condition_plan.iv_check_condition1
import kotlinx.android.synthetic.main.item_bof_condition_plan.iv_check_condition2
import kotlinx.android.synthetic.main.item_bof_condition_plan.iv_check_condition3
import kotlinx.android.synthetic.main.item_bof_condition_plan.lo_condition1
import kotlinx.android.synthetic.main.item_bof_condition_plan.lo_condition1_body
import kotlinx.android.synthetic.main.item_bof_condition_plan.lo_condition2
import kotlinx.android.synthetic.main.item_bof_condition_plan.lo_condition2_body
import kotlinx.android.synthetic.main.item_bof_condition_plan.lo_condition3
import kotlinx.android.synthetic.main.item_bof_condition_plan.lo_condition3_body
import kotlinx.android.synthetic.main.item_bof_condition_plan.rbCollect
import kotlinx.android.synthetic.main.item_bof_condition_plan.rbPrepaid
import kotlinx.android.synthetic.main.item_bof_condition_plan.rgPlanType
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_basic_rate_initial
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_basic_rate_initial_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_basic_rate_midterm
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_basic_rate_midterm_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_basic_rate_remainder
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_basic_rate_remainder_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_condition1_header
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_condition2_header
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_condition3_header
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_first_rate_initial
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_first_rate_initial_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_first_rate_midterm
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_first_rate_midterm_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_first_rate_remainder
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_first_rate_remainder_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_last_rate_initial
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_last_rate_initial_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_last_rate_midterm
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_last_rate_midterm_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_last_rate_remainder
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_last_rate_remainder_value
import kotlinx.android.synthetic.main.item_bof_condition_plan.tv_sof_conditiontotal_deal_amount
import kotlinx.android.synthetic.main.item_bof_condition_rdterm.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class BofWizardConditionsFragment constructor(val viewModel: BofWizardViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null
    private var mCarriers: MutableList<OfferCarrier>? = null

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_condition, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("f9: onViewCreated")
        setRxOutputs()
        initData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("f9: onAttach")
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Timber.d("f9: onDetach")
        mOffer = null
        listener = null
    }

    override fun onDestroyView() {
        Timber.d("f9: onDestroyView")
        super.onDestroyView()
    }

    override fun onStart() {
        Timber.d("f9: onStart")
        super.onStart()
    }

    override fun onStop() {
        Timber.d("f9: onStop")
        super.onStop()
    }

    override fun onResume() {
        Timber.d("f9: onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("f9: onPause")
    }

    /**
     * View model 의 output interface 처리
     */
    private fun setRxOutputs() {
        /**
         * offer 정보 가져와서 설정
         */
        viewModel.outPuts.onGoToConditionsStep()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offer ->
                        Timber.d("f9: Rx - onGoToConditionStep")

                        Handler().postDelayed({
                            sv_body_root.scrollTo(0,0)
                        }, 100)

                        mOffer = offer

                        with(offer) {

                            setPaymentUi(offerPaymentPlanCode)

                            // payment term
                            when (offerPaymentTermCode) {
                                OFFER_PAYMENT_TERM_CODE_PPD -> {
                                    rgPlanType.check(R.id.rbPrepaid)
                                    rbPrepaid.typeface = tfBold
                                    rbCollect.typeface = tfRegular
                                }
                                OFFER_PAYMENT_TERM_CODE_CCT -> {
                                    rgPlanType.check(R.id.rbCollect)
                                    rbPrepaid.typeface = tfRegular
                                    rbCollect.typeface = tfBold
                                }
                            }

                            // Rd Term
                            setRdTerm(offerRdTermCode)

                            // Carriers
                            setCarriers(isEdit = false)
                        }
                    }
                }

        viewModel.outPuts.onSuccessCarriers()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { carriers ->
                        if (mCarriers.isNullOrEmpty()) {
                            mCarriers = carriers.toMutableList()
                            mCarriers?.let {
                                setCarriers(isEdit = false)
                            }
                        }
                    }
                }

        viewModel.outPuts.onRequestPaymentPlans()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { plans ->
                        Timber.d("f9: Rx - onRequestPaymentPlans - plans :$plans")
                        mPaymentPlans = plans as MutableList<PaymentPlan>
                        initPaymentRatio()
                        mOffer?.let { offer ->
                            setPaymentValue(offer.offerPaymentPlanCode)
                        }
                    }
                }
    }

    /**
     * fragment data init
     */
    private fun initData() {
        currencyFormat.minimumFractionDigits = 0
        ResourcesCompat.getFont(activity!!, R.font.opensans_regular)?.let{
            tfRegular = it
        }
        ResourcesCompat.getFont(activity!!, R.font.opensans_bold)?.let {
            tfBold = it
        }

        setListener()
    }

    /**
     * Widget Click, ViewModel interface
     */
    private fun setListener() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // conditionCode click
        lo_condition1.setSafeOnClickListener {
            Timber.d("lo_condition1 click")
            clickToPlan(PAYMENT_PLANCODE_PS)
        }

        lo_condition2.setSafeOnClickListener {
            Timber.d("f9: lo_condition2 click")
            clickToPlan(PAYMENT_PLANCODE_PF)
        }

        lo_condition3.setSafeOnClickListener {
            Timber.d("f9: lo_condition3 click")
            clickToPlan(PAYMENT_PLANCODE_PL)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // paymentTerm radio click
        rbPrepaid.setSafeOnClickListener {
            Timber.d("f9: rbPrepaid click")

            mOffer?.offerPaymentTermCode = OFFER_PAYMENT_TERM_CODE_PPD
            rbPrepaid.typeface = tfBold
            rbCollect.typeface = tfRegular
        }

        rbCollect.setSafeOnClickListener {
            Timber.d("f9: rbCollect click")

            mOffer?.offerPaymentTermCode = OFFER_PAYMENT_TERM_CODE_CCT
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

            viewModel.inPuts.requestGoToOtherStep(Pair(STEP_MAKE, mOffer!!))
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // carriers
        tv_carrier_edit.setSafeOnClickListener {
            RxActivityResult(activity!!)
                    .start( Intent(activity!!, BofCarrierAct::class.java).putExtra(Intents.CARRIER_LIST, ArrayList(mCarriers) ) )
                    .subscribe(
                            {
                                if (it.isOk) {
                                    val carriers = it.data.getSerializableExtra(Intents.CARRIER_LIST) as List<OfferCarrier>
                                    mCarriers?.let {
                                        it.clear()
                                        it.addAll(carriers)
                                    }
                                    setCarriers(isEdit = true)
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
            setRdTerm(OFFER_RD_TERM_CODE_CYCY)
        }

        lo_rdterm_cy_door.setSafeOnClickListener {
            setRdTerm(OFFER_RD_TERM_CODE_CYDOOR)
        }

        lo_rdterm_door_cy.setSafeOnClickListener {
            setRdTerm(OFFER_RD_TERM_CODE_DOORCY)
        }

        lo_rdterm_door_door.setSafeOnClickListener {
            setRdTerm(OFFER_RD_TERM_CODE_DOORDOOR)
        }
    }

    /**
     * payment plan code set
     */
    private fun clickToPlan(paymentPlan: String) {
        mOffer?.let { offer ->
            offer.offerPaymentPlanCode = paymentPlan
        }
        setPaymentUi(paymentPlan)
        setPaymentValue(paymentPlan)
    }

    /**
     * Rd term code 별로 ui, 값 설정
     */
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
            OFFER_RD_TERM_CODE_CYCY -> {
                tv_rdterm_cy_cy.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_cy_cy.visibility = View.VISIBLE
            }
            OFFER_RD_TERM_CODE_CYDOOR -> {
                tv_rdterm_cy_door.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_cy_door.visibility = View.VISIBLE
            }
            OFFER_RD_TERM_CODE_DOORCY -> {
                tv_rdterm_door_cy.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_door_cy.visibility = View.VISIBLE
            }
            OFFER_RD_TERM_CODE_DOORDOOR -> {
                tv_rdterm_door_door.setTextAppearance(R.style.txt_opensans_b_15_blue_violet)
                iv_rdterm_door_door.visibility = View.VISIBLE
            }
        }
        mOffer?.apply {
            offerRdTermCode = rdTermCode
        }
    }

    /**
     * carrier data 설정
     */
    private fun setCarriers(isEdit: Boolean = false) {

        mOffer?.let { offer ->
            if (offer.offerCarriers.isNullOrEmpty()) {
                offer.offerCarriers = mCarriers
            }
        }

        mCarriers?.let { carriers ->

            if (isEdit) {
                // mCarriers 의  true 항목들로 offerCarriers 를 구성한다
                mOffer?.let { offer ->
                    offer.offerCarriers = carriers.filter { it.isChecked }
                }
            } else {
                // mOffer.offerCarriers 의 offerCarrierCode 와 같은 mCarriers.isChecked 를 설정한다
                mOffer?.offerCarriers?.let { offerCarriers ->
                    for (offerCarrier in offerCarriers) {
                        carriers.map { if (it.offerCarrierCode == offerCarrier.offerCarrierCode) it.isChecked = true }
                    }
                }
            }

            mOffer?.offerCarriers?.let { offerCarriers ->
                val checkedSize = offerCarriers.filter { carrier -> carrier.isChecked }.size
                val allCarriers = carriers.size == checkedSize
                val firstCarrier = offerCarriers.first()
                iv_carrier.setImageResource(if (allCarriers) {
                    getString(R.string.all_carriers).getCarrierIcon(false)
                } else {
                    firstCarrier.offerCarrierCode.getCarrierIcon(false)
                })
                tv_carrier_code.text = if (allCarriers) getString(R.string.all_carriers) else firstCarrier.offerCarrierCode
                tv_carrier_count.text = if (allCarriers) {
                    Constant.EmptyString
                } else {
                    checkedSize.getCodeCount(false)
                }
                mOffer?.offerCarriers = carriers.filter { carrier -> carrier.isChecked }
            }
        }
    }

    /**
     * payment plan code 별로 ui, 값 설정
     */
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

        tv_condition1_header.setTextColor(ContextCompat.getColor(activity!!, R.color.greyish_brown))
        tv_condition2_header.setTextColor(ContextCompat.getColor(activity!!, R.color.greyish_brown))
        tv_condition3_header.setTextColor(ContextCompat.getColor(activity!!, R.color.greyish_brown))

        // set conditionCode view
        when (paymentPlanCode) {
            PAYMENT_PLANCODE_PS -> {
                lo_condition1_body.visibility = View.VISIBLE
                lo_condition1.setBackgroundResource(R.drawable.bg_rectangle_blue_border)
                iv_check_condition1.setImageResource(R.drawable.btn_checkbox_solid_selected_l)
                tv_condition1_header.setTextColor(ContextCompat.getColor(activity!!, R.color.white))
            }
            PAYMENT_PLANCODE_PF -> {
                lo_condition2_body.visibility = View.VISIBLE
                lo_condition2.setBackgroundResource(R.drawable.bg_rectangle_blue_border)
                iv_check_condition2.setImageResource(R.drawable.btn_checkbox_solid_selected_l)
                tv_condition2_header.setTextColor(ContextCompat.getColor(activity!!, R.color.white))
            }
            PAYMENT_PLANCODE_PL -> {
                lo_condition3_body.visibility = View.VISIBLE
                lo_condition3.setBackgroundResource(R.drawable.bg_rectangle_blue_border)
                iv_check_condition3.setImageResource(R.drawable.btn_checkbox_solid_selected_l)
                tv_condition3_header.setTextColor(ContextCompat.getColor(activity!!, R.color.white))
            }
        }

        mOffer?.apply {
            offerPaymentPlanCode = paymentPlanCode
        }
    }

    /**
     * payment plan code 별로 payment ratio 설정
     */
    private fun initPaymentRatio() {
        for (paymentPlan in mPaymentPlans) {
            when (paymentPlan.paymentPlanCode) {
                PAYMENT_PLANCODE_PS -> {
                    tv_basic_rate_initial.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString ())
                    tv_basic_rate_midterm.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                    tv_basic_rate_remainder.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())
                }
                PAYMENT_PLANCODE_PF -> {
                    tv_first_rate_initial.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString())
                    tv_first_rate_midterm.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                    tv_first_rate_remainder.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())
                }
                PAYMENT_PLANCODE_PL -> {
                    tv_last_rate_initial.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.initialPaymentRatio).toString())
                    tv_last_rate_midterm.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.middlePaymentRatio).toString())
                    tv_last_rate_remainder.text = getString(R.string.payplan_rate_display, calcRatio(paymentPlan.balancePaymentRatio).toString())
                }
            }
        }
    }

    /**
     * payment plan code 별로 payment value 설정
     */
    private fun setPaymentValue(paymentPlanCode: String) {
        ////////////////////////////////////////////////////////////////////////////
        // total deal amount
        // filter offerQty smaller than one (1)
        val paymentPlan = mPaymentPlans.find { it.paymentPlanCode == paymentPlanCode }
        paymentPlan?.let { plan ->
            val firstPaymentRatio = plan.initialPaymentRatio
            val middlePaymentRatio = plan.middlePaymentRatio
            val balancedPaymentRatio = plan.balancePaymentRatio
            var offerPrices = 0
            mOffer?.offerLineItems?.let { lineItems ->
                offerPrices = lineItems.sumBy { it.offerPrice * it.offerQty }
                tv_sof_conditiontotal_deal_amount.text = currencyFormat.format(offerPrices)
                lineItems.map { lineItem ->
                    lineItem.firstPaymentRatio = firstPaymentRatio
                    lineItem.middlePaymentRatio = middlePaymentRatio
                    lineItem.balancedPaymentRatio = balancedPaymentRatio
                }
            }
            when (paymentPlanCode) {
                PAYMENT_PLANCODE_PS -> {
                    tv_basic_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(firstPaymentRatio), offerPrices))
                    tv_basic_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(middlePaymentRatio), offerPrices))
                    tv_basic_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(balancedPaymentRatio), offerPrices))
                }
                PAYMENT_PLANCODE_PF -> {
                    tv_first_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(firstPaymentRatio), offerPrices))
                    tv_first_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(middlePaymentRatio), offerPrices))
                    tv_first_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(balancedPaymentRatio), offerPrices))
                }
                PAYMENT_PLANCODE_PL -> {
                    tv_last_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(firstPaymentRatio), offerPrices))
                    tv_last_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(middlePaymentRatio), offerPrices))
                    tv_last_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(balancedPaymentRatio), offerPrices))
                }
            }
        }
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()

    companion object {
        var mOffer: Offer? = null
        var mPaymentPlans: MutableList<PaymentPlan> = ArrayList()

        @JvmStatic
        fun newInstance(viewModel: BofWizardViewModel) : BofWizardConditionsFragment {
            return BofWizardConditionsFragment(viewModel)
        }
    }
}