package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_SELL
import com.cyberlogitec.freight9.config.ContainerSizeType
import com.cyberlogitec.freight9.config.ContainerType
import com.cyberlogitec.freight9.config.LocationTypeCode
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.ui.fragmentstepper.OnFragmentInteractionListener
import com.cyberlogitec.freight9.lib.util.*
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.CHECK_PLAN
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.CHECK_PRICE
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.CHECK_VOLUME
import com.trello.rxlifecycle3.components.support.RxFragment
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.body_offer_make_offer.*
import kotlinx.android.synthetic.main.body_pol_pod_card.*
import kotlinx.android.synthetic.main.body_sof_make_offer.*
import kotlinx.android.synthetic.main.fragment_sof_make.*
import timber.log.Timber


class SofWizardMakeFragment constructor(val viewModel: SofWizardViewModel): RxFragment() {

    private var listener: OnFragmentInteractionListener? = null

    lateinit var tfRegular: Typeface
    lateinit var tfBold: Typeface

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_sof_make, container, false)

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
        mContract = null
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
        viewModel.outPuts.onGoToMakeStep()
                .bindToLifecycle(this)
                .subscribe {
                    it?.let { contract ->
                        Timber.d("f9: Rx - onGoToMakeStep")
                        sv_sof_make_body_root.scrollTo(0, 0)
                        // save contract
                        mContract = contract
                        activity!!.runOnUiThread {
                            setPolPodValue(contract)
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
                        mPaymentPlan = plans.find { it.paymentPlanCode == mContract?.paymentPlanCode }
                        setOffer()
                    }
                }

        viewModel.outPuts.onClickDealOptionsInfo()
                .bindToLifecycle(this)
                .subscribe {
                    // TODO : Deal options info
                }
    }

    /**
     * fragment data init
     */
    private fun initData() {
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
        // MAKE A SELL OFFER
        btn_make_sell_offer_next.setSafeOnClickListener {
            Timber.d("f9: btn_make_sell_offer_next +")
            if (isAvailableQty(mContract)) {
                viewModel.inPuts.clickToMake(mOffer!!)
            } else {
                activity!!.showToast("offerQty > remainderQty")
            }
        }

        tv_link_condition_detail.setSafeOnClickListener {
            viewModel.inPuts.clickToConditionDetail(Parameter.CLICK)
        }

        tv_link_whole_route.setSafeOnClickListener {
            viewModel.inPuts.clickToWholeRoute(Parameter.CLICK)
        }

        tv_price_table.setSafeOnClickListener {
            viewModel.inPuts.clickToPriceTable(Parameter.CLICK)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click check
        iv_period_and_volume.setSafeOnClickListener {
            viewModel.inPuts.clickToCheck(Pair(CHECK_VOLUME, mContract))
        }

        // price check
        iv_price.setSafeOnClickListener {
            viewModel.inPuts.clickToCheck(Pair(CHECK_PRICE, mContract))
        }

        // plan check
        iv_pay_plan.setSafeOnClickListener {
            viewModel.inPuts.clickToCheck(Pair(CHECK_PLAN, mContract))
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click igree
        chk_terms_agree.setOnCheckedChangeListener { _, isChecked ->
            btn_make_sell_offer_next.isEnabled = isChecked
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // whole deal option
        chk_whole_deal_apply.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("f9: isChecked: ${isChecked}")
            if (isChecked) {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_applied)
                tv_whole_deal_apply.setTypeface( tfBold )
                mOffer?.let {
                    it.allYn = "1"  // whole
                }
            } else {
                tv_whole_deal_apply.text = getString(R.string.offer_whole_deal_not_applied)
                tv_whole_deal_apply.setTypeface( tfRegular )
                mOffer?.let {
                    it.allYn = "0"  // partial
                }
            }
        }

        iv_check_mark.setSafeOnClickListener {
            viewModel.inPuts.clickToDealOptionsInfo(Parameter.CLICK)
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click more
        tv_terms_more.setSafeOnClickListener {
            activity!!.showTermsMorePopup()
        }
    }

    /**
     * 전달 받은 inventory 정보로 UI card 에 pol, pod 표시
     */
    private fun setPolPodValue(contract: Contract?) {
        contract?.masterContractCarriers?.let { carriers ->
            val carrierCode = carriers.first().carrierCode
            val carriersCount = carriers.size
            iv_carrier_logo.setImageResource(carrierCode.getCarrierIcon(false))
            tv_carrier_name.text = if (carrierCode.trim().isNotEmpty()) carrierCode else getString(R.string.all_carriers)
            tv_carrier_count.text = carriersCount.getCodeCount(false)
        }

        contract?.masterContractRoutes?.let { routes ->
            // find first pol
            val firstPol = routes.sortedBy { it.regSeq }.find { it.locationTypeCode == LocationTypeCode.POL }
            val polCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POL }
                    .distinctBy { it.locationCode }.count()
            firstPol?.let {
                tv_pol_name.text = it.locationCode
                tv_pol_desc.text = it.locationName
                tv_pol_count.text = polCnt.getCodeCount(false)
            }

            // find first pod
            val firstPod = routes.sortedBy { it.regSeq }.find { it.locationTypeCode == LocationTypeCode.POD }
            val podCnt = routes
                    .filter { it.locationTypeCode == LocationTypeCode.POD }
                    .distinctBy { it.locationCode }.count()
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
     * inventory 정보를 offer data class 로 변환
     */
    private fun setOffer() {
        mContract?.let {
            // offerLineItems
            val offerLineItems = mutableListOf<OfferLineItem>()
            it.masterContractLineItems?.forEach { lineItem ->
                if (lineItem.isChecked && lineItem.offerPrice > 0) {
                    val offerLineItem = OfferLineItem(tradeContainerTypeCode = ContainerType.F_TYPE, tradeContainerSizeCode = ContainerSizeType.N20FT_TYPE)
                    val offerPrices = mutableListOf<OfferPrice>()

                    offerLineItem.baseYearWeek = lineItem.baseYearWeek
                    offerLineItem.offerQty = lineItem.offerQty
                    offerLineItem.offerPrice = lineItem.offerPrice

                    offerLineItem.firstPaymentRatio = mPaymentPlan!!.initialPaymentRatio
                    offerLineItem.middlePaymentRatio = mPaymentPlan!!.middlePaymentRatio
                    offerLineItem.balancedPaymentRatio = mPaymentPlan!!.balancePaymentRatio

                    lineItem.masterContractPrices?.forEach {
                        offerPrices.add(OfferPrice(offerPrice = it.price, containerSizeCode = it.containerSizeCode, containerTypeCode = it.containerTypeCode))
                    }
                    offerLineItem.offerPrices = offerPrices
                    offerLineItems.add(offerLineItem)
                }
            }

            // offerRoutes
            val offerRoutes = mutableListOf<OfferRoute>()
            it.masterContractRoutes?.let { contractRoutes ->
                contractRoutes.forEach {
                    val offerRoute = OfferRoute(offerRegSeq = it.regSeq, locationCode = it.locationCode, locationTypeCode = it.locationTypeCode)
                    offerRoutes.add((offerRoute))
                }
            }

            // offerCarriers
            val offerCarriers = mutableListOf<OfferCarrier>()
            it.masterContractCarriers?.let { carriers ->
                carriers.filter { it.deleteYn != "1" }.forEach {
                    val offerCarrier = OfferCarrier(offerCarrierCode = it.carrierCode)
                    offerCarriers.add(offerCarrier)
                }
            }

            // make offer
            mOffer = Offer(
                    masterContractNumber = it.masterContractNumber,
                    offerTypeCode = OFFER_TYPE_CODE_SELL,
                    offerRdTermCode = it.rdTermCode,
                    offerPaymentTermCode = it.paymentTermCode,
                    offerLineItems = offerLineItems,
                    offerRoutes = offerRoutes,
                    offerCarriers = offerCarriers,
                    offerPaymentPlanCode = it.paymentPlanCode
            )
            mOffer!!.allYn = "0"
        }
    }

    /**
     * input volume value 가 유효한지 체크
     */
    private fun isAvailableQty(contract: Contract?): Boolean {
        var isButtonEnable = true
        contract?.let { contract_ ->
            contract_.masterContractLineItems?.forEach { lineItem ->
                if (lineItem.isChecked && lineItem.offerPrice > 0) {
                    if (lineItem.offerQty > lineItem.remainderQty) {
                        isButtonEnable = false
                        return@let
                    }
                }
            }
        }
        Timber.d("f9: isAvailableQty is $isButtonEnable")
        return isButtonEnable
    }

    companion object {
        var mContract: Contract? = null
        var mOffer: Offer? = null
        var mPaymentPlan: PaymentPlan? = null

        @JvmStatic
        fun newInstance(viewModel: SofWizardViewModel) : SofWizardMakeFragment {
            return SofWizardMakeFragment(viewModel)
        }
    }
}
