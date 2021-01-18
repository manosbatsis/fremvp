package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.CHECK_CONDITIONS
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.CHECK_PRICE
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.CHECK_VOLUME
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_bof_make_offer.*
import kotlinx.android.synthetic.main.body_pol_pod_card_simple.*
import kotlinx.android.synthetic.main.fragment_bof_make_offer.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class BofWizardMakeFragment constructor(val viewModel: BofWizardViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface
    private var isEnoughBudget: Boolean = false
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val budgetValue = 99_999_999
    private val feeRate = 0.01f
    private var mOffer: Offer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_bof_make_offer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRxOutputs()
        initData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
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
        Timber.d("f9: onResume - mOffer = $mOffer")
        super.onResume()
        // Intent 로 실행되었을 때 setRxOutputs 로 처리가 안된 경우
        if (mOffer == null) {
            Timber.d("f9: onResume - requestGoToOtherStep")
            viewModel.inPuts.requestGoToOtherStep(Pair(BofWizardActivity.STEP_MAKE, null))
        }
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
        viewModel.outPuts.onGoToMakeStep()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { offer ->
                        Timber.d("f9: Rx - onGoToMakeStep")
                        sv_bof_make_body_root.scrollTo(0, 0)
                        // save offer
                        mOffer = offer
                        activity!!.runOnUiThread {
                            setPolPodValue(offer)
                        }
                    }
                }

        /**
         * Paymentplans 정보 가져와서 설정
         */
        viewModel.outPuts.onRequestPaymentPlans()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { plans ->
                        Timber.d("f9: Rx - onRequestPaymentPlans - plans :$plans")
                        val paymentPlan = plans.find { it.paymentPlanCode == mOffer?.offerPaymentPlanCode }
                        activity!!.runOnUiThread {
                            setHoldingDeposit(mOffer, paymentPlan)
                        }
                    }
                }
    }

    /**
     * fragment data init
     */
    private fun initData() {
        currencyFormat.minimumFractionDigits = 0

        // init type face (font)
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
        // click check
        iv_period_and_volume.setSafeOnClickListener {
            viewModel.inPuts.clickToCheck(Pair(CHECK_VOLUME, mOffer))
        }

        // price check
        iv_price.setSafeOnClickListener {
            viewModel.inPuts.clickToCheck(Pair(CHECK_PRICE, mOffer))
        }

        // Condition check
        iv_pay_plan.setSafeOnClickListener {
            viewModel.inPuts.clickToCheck(Pair(CHECK_CONDITIONS, mOffer))
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click igree
        chk_terms_agree.setOnCheckedChangeListener { _, isChecked ->
            btn_make_buy_offer_next.isEnabled = isChecked && isEnoughBudget
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // whole deal option
        chk_whole_deal_apply.setOnClickListener {
            val isChecked = chk_whole_deal_apply.isChecked
            Timber.d("f9: isChecked: $isChecked")

            if (isChecked) {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_applied)
                tv_whole_deal_apply.typeface = tfBold
                mOffer?.allYn = "1"  // whole
            } else  {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_not_applied)
                tv_whole_deal_apply.typeface = tfRegular
                mOffer?.allYn = "0"  // partial
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click more
        tv_terms_more.setSafeOnClickListener {
            activity!!.showTermsMorePopup()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // MAKE A BUY OFFER
        btn_make_buy_offer_next.setSafeOnClickListener {
            Timber.d("f9: btn_make_buy_offer_next + : mOffer = %s", mOffer!!.toJson())
            viewModel.inPuts.clickToMake(mOffer!!)
        }
    }

    /**
     * 전달 받은 offer item 정보로 UI card 에 pol, pod 표시
     */
    private fun setPolPodValue(offer: Offer) {
        if (!offer.offerCarriers.isNullOrEmpty()) {
            offer.offerCarriers?.let { carriers ->
                val carrierCode = carriers.first().offerCarrierCode
                val carriersCount = carriers.size
                iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
                tv_carrier_count.text = carriersCount.getCodeCount(false)
            }
        } else {
            // 지금 Step 에서는 carrier를 알 수 없음
            offer.tradeCompanyCode.let { carrierCode ->
                iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
                tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
                tv_carrier_count.text = ""
            }
        }

        offer.offerRoutes?.let { routes ->
            // find first pol
            val firstPol = routes.sortedBy { it.offerRegSeq }.find { it.locationTypeCode == LocationTypeCode.POL }
            val polCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POL }
                    .distinctBy { it.locationName }
                    .size
            firstPol?.let {
                tv_pol_name.text = it.locationCode
                tv_pol_desc.text = it.locationName
                tv_pol_count.text = polCnt.getCodeCount(false)
            }

            // find first pod
            val firstPod = routes.sortedBy { it.offerRegSeq }.find { it.locationTypeCode == LocationTypeCode.POD }
            val podCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POD }
                    .distinctBy { it.locationName }
                    .size
            firstPod?.let {
                tv_pod_name.text = it.locationCode
                tv_pod_desc.text = it.locationName
                tv_pod_count.text = podCnt.getCodeCount(false)
            }

            tv_period.text = ""       // W01-W10
            // icon 표시하지 않음
            iv_period_whole.visibility = View.INVISIBLE
        }
    }

    /**
     * 전달 받은 offer item 정보로 UI card 에 holding deposit 표시
     */
    private fun setHoldingDeposit(offer: Offer?, paymentPlan: PaymentPlan?) {
        // Holding deposit : 사용자가 설정한 payplan의 Initial payment비율과 동일
        var holdingDeposit: Int
        offer?.let {
            it.offerLineItems?.let { lineItems ->
                val totalPrice = lineItems.filter { it.isChecked }.map { it.offerPrice * it.offerQty }.sum()
                paymentPlan?.let {
                    tv_holding_deposit_rate.text = getString(R.string.buy_offer_holding_deposit_rate, calcRatio(paymentPlan.initialPaymentRatio).toString())
                    holdingDeposit = calcValue(calcRatio(paymentPlan.initialPaymentRatio), totalPrice)
                    tv_holding_deposit_value.text = currencyFormat.format(holdingDeposit)
                    setBalanceAndAvailableBudget(holdingDeposit)
                }
            }
        }
    }

    private fun setBalanceAndAvailableBudget(holdingDeposit: Int) {
        // required balance = holding deposit + transaction fee(holding deposit 금액의 1%)
        val balance = holdingDeposit + calcValue(calcRatio(feeRate), holdingDeposit)
        tv_required_balance_value.text = currencyFormat.format(balance)
        tv_available_account_budget_value.text = currencyFormat.format(budgetValue)

        isEnoughBudget = budgetValue >= balance
        val textColorEnoughBudget = if (isEnoughBudget) R.color.greyish_brown else R.color.orangey_red
        tv_available_account_budget.setTextColor(ContextCompat.getColor(activity!!, textColorEnoughBudget))
        tv_available_account_budget_value.setTextColor(ContextCompat.getColor(activity!!, textColorEnoughBudget))
    }

    private fun calcRatio(value: Float) = (value * 100).toInt()
    private fun calcValue(ratio: Int, offerPrices: Int) = (ratio.toFloat() / 100 * offerPrices).toInt()

    companion object {
        @JvmStatic
        fun newInstance(viewModel: BofWizardViewModel) : BofWizardMakeFragment {
            return BofWizardMakeFragment(viewModel)
        }
    }
}