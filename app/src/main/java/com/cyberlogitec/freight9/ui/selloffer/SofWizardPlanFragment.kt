package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_CCT
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PF
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PL
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PS
import com.cyberlogitec.freight9.lib.model.Contract
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.setSafeOnClickListener
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_MAKE
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.act_sof_condition.*
import kotlinx.android.synthetic.main.body_sof_condition.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


class SofWizardPlanFragment constructor(val viewModel: SofWizardViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_sof_plan, container, false)

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
        mContract = null
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
         * inventory 정보 가져와서 설정
         */
        viewModel.outPuts.onGoToPlanStep()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { contract ->
                        Timber.d("f9: Rx - onGoToPlanStep")
                        Handler().postDelayed({
                            sv_sof_plan_body_root.scrollTo(0, 0)
                        }, 100)

                        mContract = contract
                        mContract?.let {
                            setPaymentUi(it.paymentPlanCode)
                            // payment term
                            when (it.paymentTermCode) {
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
                            viewModel.baseOfferInputs.requestPaymentPlans(Unit)
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
                        mContract?.let { contract ->
                            setPaymentValue(contract.paymentPlanCode)
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
        btn_condition_next.setSafeOnClickListener {
            Timber.d("f9: btn_condition_next")
            viewModel.inPuts.requestGoToOtherStep(Pair(STEP_MAKE, mContract!!))
        }

        // Basic Payment Plan
        lo_condition1.setSafeOnClickListener {
            Timber.d("f9: Basic Payment Plan click")
            clickToPlan(PAYMENT_PLANCODE_PS)
        }

        // Collect-First Payment Plan
        lo_condition2.setSafeOnClickListener {
            Timber.d("f9: Collect-First Payment Plan click")
            clickToPlan(PAYMENT_PLANCODE_PF)
        }

        // Collect-Later Payment Plan
        lo_condition3.setSafeOnClickListener {
            Timber.d("f9: Collect-Later Payment Plan click")
            clickToPlan(PAYMENT_PLANCODE_PL)
        }

        // Prepaid
        rbPrepaid.setSafeOnClickListener {
            Timber.d("f9: rb Prepaid click")
            mContract?.let {
                it.paymentTermCode = OFFER_PAYMENT_TERM_CODE_PPD
            }
            rbPrepaid.typeface = tfBold
            rbCollect.typeface = tfRegular
        }

        // Collect
        rbCollect.setSafeOnClickListener {
            Timber.d("f9: rb Collect click")
            mContract?.let {
                it.paymentTermCode = OFFER_PAYMENT_TERM_CODE_CCT
            }
            rbPrepaid.typeface = tfRegular
            rbCollect.typeface = tfBold
        }
    }

    /**
     * payment plan code set
     */
    private fun clickToPlan(paymentPlan: String) {
        mContract?.let {contract ->
            contract.paymentPlanCode = paymentPlan
        }
        setPaymentUi(paymentPlan)
        setPaymentValue(paymentPlan)
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
    }

    /**
     * payment plan code 별로 payment value 설정
     */
    private fun setPaymentValue(paymentPlan: String) {
        ////////////////////////////////////////////////////////////////////////////
        // total deal amount
        // filter offerQty smaller than one (1)
        var offerPrices = 0
        mContract?.masterContractLineItems?.let{ lineItems ->
            offerPrices = lineItems.sumBy{ it.offerPrice * it.offerQty }
            tv_sof_conditiontotal_deal_amount.text = currencyFormat.format(offerPrices)
        }

        val findPaymentPlan = mPaymentPlans.find { it.paymentPlanCode == paymentPlan }
        findPaymentPlan?.let { plan ->
            when(plan.paymentPlanCode) {
                PAYMENT_PLANCODE_PS -> {
                    tv_basic_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.initialPaymentRatio), offerPrices))
                    tv_basic_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.middlePaymentRatio), offerPrices))
                    tv_basic_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.balancePaymentRatio), offerPrices))
                }
                PAYMENT_PLANCODE_PF -> {
                    tv_first_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.initialPaymentRatio), offerPrices))
                    tv_first_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.middlePaymentRatio), offerPrices))
                    tv_first_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.balancePaymentRatio), offerPrices))
                }
                PAYMENT_PLANCODE_PL -> {
                    tv_last_rate_initial_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.initialPaymentRatio), offerPrices))
                    tv_last_rate_midterm_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.middlePaymentRatio), offerPrices))
                    tv_last_rate_remainder_value.text = currencyFormat.format(calcValue(calcRatio(findPaymentPlan.balancePaymentRatio), offerPrices))
                }
            }
        }
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()

    companion object {
        var mContract: Contract? = null
        var mPaymentPlans: MutableList<PaymentPlan> = ArrayList()

        @JvmStatic
        fun newInstance(viewModel: SofWizardViewModel) : SofWizardPlanFragment {
            return SofWizardPlanFragment(viewModel)
        }
    }
}
