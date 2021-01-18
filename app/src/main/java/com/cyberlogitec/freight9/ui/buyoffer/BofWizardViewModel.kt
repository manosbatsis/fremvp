package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseOfferViewModel
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_PAYMENT_TERM_CODE_PPD
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_RD_TERM_CODE_CYCY
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PS
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.fromJson
import com.cyberlogitec.freight9.lib.util.toJson
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.STEP_CONDITIONS
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.STEP_MAKE
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.STEP_PRICE
import com.cyberlogitec.freight9.ui.buyoffer.BofWizardActivity.Companion.STEP_VOLUME
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofWizardViewModel(context: Context) : BaseOfferViewModel(context),
        BofWizardInputs, BofWizardOutputs {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // inputs

    val inPuts: BofWizardInputs = this
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToMake = PublishSubject.create<Offer>()
    private val clickToBack = PublishSubject.create<Parameter>()
    private val clickToCheck = PublishSubject.create<Pair<Int, Offer?>>()

    private val offerVolumeStep = BehaviorSubject.create<Offer>()
    private val offerPriceStep = BehaviorSubject.create<Offer>()
    private val offerConditionsStep = BehaviorSubject.create<Offer>()
    private val offerMakeStep = BehaviorSubject.create<Offer>()

    private val requestGoToOtherStep = PublishSubject.create<Pair<Int, Offer?>>()
    private val requestVolumeOffer = PublishSubject.create<Parameter>()
    private val requestPriceOffer = PublishSubject.create<Parameter>()
    private val requestConditionsOffer = PublishSubject.create<Parameter>()
    private val requestMakeOffer = PublishSubject.create<Parameter>()
    private val requestPaymentPlans = PublishSubject.create<Unit>()
    private val requestCarrier = PublishSubject.create<Unit>()

    private val requestCallRefreshAfterDiscard = PublishSubject.create<Parameter>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // outputs

    val outPuts: BofWizardOutputs = this
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickMake = PublishSubject.create<Boolean>()
    private val onClickBack = PublishSubject.create<Parameter>()
    private val onSetDoneBtn = BehaviorSubject.create<Boolean>()

    private val onClickCheck = PublishSubject.create<Pair<Int, Offer?>>()

    private val onRequestPaymentPlans = PublishSubject.create<List<PaymentPlan>>()
    private val onSuccessCarrier = PublishSubject.create<List<OfferCarrier>>()

    private val onGoToOtherStep = PublishSubject.create<Pair<Int, Offer?>>()
    private val onGoToVolumeStep = PublishSubject.create<Offer>()
    private val onGoToPriceStep = PublishSubject.create<Offer>()
    private val onGoToConditionsStep = PublishSubject.create<Offer>()
    private val onGoToMakeStep = PublishSubject.create<Offer>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val discardIntent = BehaviorSubject.create<Boolean>()
    private val parentId = BehaviorSubject.create<Intent>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.bindToLifeCycle()
                .subscribe { intent ->
                    var offerDiscard = false
                    if (intent.hasExtra(Intents.OFFER_DISCARD)) {
                        offerDiscard = intent.getBooleanExtra(Intents.OFFER_DISCARD, false)
                    }
                    discardIntent.onNext(offerDiscard)
                    parentId.onNext(intent)
                }

        // intent 포인터에 재설정 후 refresh 다시 호출
        parentId.compose<Intent> { requestCallRefreshAfterDiscard.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { Timber.d("f9: requestCallRefreshAfterDiscard !"); it }
                .map { intent ->
                    intent.putExtra(Intents.OFFER_DISCARD, false)
                }
                .subscribe { refresh.onNext(Parameter.EVENT) }

        parentId.compose<Intent> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { Timber.d("f9: BofWizardViewModel : parentId--> ${it}"); it }
                .map { intent ->
                    val offer = intent.getSerializableExtra(Intents.OFFER) as Offer
                    // 외부 모듈에서 호출되었는지 여부
                    var offerByMadeCondition = false
                    if (intent.hasExtra(Intents.OFFER_BY_MADE_CONDITION)) {
                        offerByMadeCondition = intent.getBooleanExtra(Intents.OFFER_BY_MADE_CONDITION, false)
                    }
                    // Discard 처리 할지 여부
                    var offerDiscard = false
                    if (intent.hasExtra(Intents.OFFER_DISCARD)) {
                        offerDiscard = intent.getBooleanExtra(Intents.OFFER_DISCARD, false)
                    }
                    // Make Step 이동 여부
                    var offerMakeStep = false
                    if (intent.hasExtra(Intents.OFFER_MAKE_STEP)) {
                        offerMakeStep = intent.getBooleanExtra(Intents.OFFER_MAKE_STEP, false)
                    }

                    if (offerByMadeCondition) {
                        // Your Offers 에서 Discard 요청된 경우
                        offer.offerLineItems?.map { lineItem ->
                            lineItem.isChecked = lineItem.offerQty > 0
                        }
                    } else {
                        // BofWizardRouteActivity 에서 호출된 경우
                        offer.offerLineItems?.map { lineItem ->
                            lineItem.isChecked = false
                        }
                        offer.offerPaymentPlanCode = PAYMENT_PLANCODE_PS
                        offer.offerPaymentTermCode = OFFER_PAYMENT_TERM_CODE_PPD
                        offer.offerRdTermCode = OFFER_RD_TERM_CODE_CYCY
                    }
                    Pair(Pair(offerDiscard, offerMakeStep), offer)
                }
                .bindToLifeCycle()
                .subscribe { pair ->

                    val offerDiscard = pair.first.first
                    val offerMakeStep = pair.first.second
                    val offer = pair.second

                    offer.offerLineItems?.let { lineItems ->
                        onSetDoneBtn.onNext(lineItems.isNotEmpty())
                    }

                    requestGoToOtherStep.onNext(Pair(if (offerMakeStep) { STEP_MAKE } else { STEP_VOLUME }, offer))

                    // Your Offers 에서 Discard 요청된 경우 Offer Cancel 요청
                    if (offerDiscard) {
                        baseOfferInputs.requestDiscardOffer(offer)
                    }
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToNext
        //
        requestGoToOtherStep
                .map { onGoToOtherStep.onNext(it); it }
                .bindToLifeCycle()
                .subscribe {
                    when (it.first) {
                        STEP_VOLUME -> {
                            // offer 가 null 이 아닌경우
                            it.second?.let { offer ->
                                // Deep Copy
                                val offerDeepCopy = offer.toJson().fromJson<Offer>()
                                offerDeepCopy?.let { it ->
                                    offerVolumeStep.onNext(it)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - offerVolumeStep : " + offerVolumeStep.value)
                            // onGoToVolumeStep 로 받는다
                            requestVolumeOffer.onNext(Parameter.EVENT)
                        }
                        STEP_PRICE -> {
                            // offer 가 null 이 아닌경우
                            it.second?.let { offer ->
                                // Deep Copy
                                val offerDeepCopy = offer.toJson().fromJson<Offer>()
                                offerDeepCopy?.let { it ->
                                    // offerDeepCopy의 offerQty 를 offerVolumeStep 에도 넣어준다...
                                    offerVolumeStep.onNext(it)
                                    // baseYearWeek 가 check 된 lineItem 들만 표시
                                    //it.offerLineItems = it.offerLineItems?.filter { it.isChecked }
                                    offerPriceStep.onNext(it)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - offerPriceStep : " + offerPriceStep.value)
                            // onGoToPriceStep 로 받는다
                            requestPriceOffer.onNext(Parameter.EVENT)
                        }
                        STEP_CONDITIONS -> {
                            // offer 가 null 이 아닌경우
                            it.second?.let { offer ->
                                // Deep Copy
                                val offerDeepCopy = offer.toJson().fromJson<Offer>()
                                offerDeepCopy?.let { it ->
                                    /*
                                    * offerDeepCopy 의 offerPaymentPlanCode, offerPaymentTermCode,
                                    * offerRdTermCode, offerCarriers 를 offerPriceStep 에도 넣어준다...
                                    * */
                                    offerVolumeStep.onNext(it)
                                    offerPriceStep.onNext(it)
                                    offerConditionsStep.onNext(it)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - offerPlanStep : " + offerConditionsStep.value)
                            // onGoToConditionsStep 로 받는다
                            requestConditionsOffer.onNext(Parameter.EVENT)
                        }
                        STEP_MAKE -> {
                            // offer 가 null 이 아닌 경우
                            it.second?.let { offer ->
                                // Deep Copy
                                val offerDeepCopy = offer.toJson().fromJson<Offer>()
                                offerDeepCopy?.let { it ->

                                    // offerCarriers 값들만 UI 에 표시되어야 한다...
                                    offerVolumeStep.onNext(it)
                                    offerPriceStep.onNext(it)
                                    offerConditionsStep.onNext(it)
                                    offerMakeStep.onNext(it)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - offerMakeStep : " + offerMakeStep.value)
                            // onGoToMakeStep 로 받는다
                            requestMakeOffer.onNext(Parameter.EVENT)
                        }
                    }
                }

        clickToBack.bindToLifeCycle()
                .subscribe(onClickBack)

        clickToDone.bindToLifeCycle()
                .subscribe(onClickDone)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Check : Volume, Price, Plan
        clickToCheck.bindToLifeCycle()
                .subscribe(onClickCheck)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToMake

        clickToMake
                .map{ showLoadingDialog.onNext(Parameter.EVENT); it }
                //
                // @POST("api/v1/product/offer/new")
                //
                .map {  offer ->
                    offer.offerLineItems = offer.offerLineItems?.filter { it.offerQty > 0 }; offer
                }
                .flatMapSingle { enviorment.apiTradeClient.postOffer(it).handleToError(hideLoadingDialog).neverError().toSingle{} }
                .map { hideLoadingDialog.onNext(Throwable("OK")) }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe {
                    onClickMake.onNext(discardIntent.value ?: false)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Move step

        offerVolumeStep.compose<Offer> { requestVolumeOffer.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .map { offer ->
                    offer.offerLineItems?.let { lineItems ->
                        lineItems.map { it.offerQty = if (!it.isChecked) 0 else it.offerQty}
                    }
                    offer
                }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onGoToVolumeStep)

        offerPriceStep.compose<Offer> { requestPriceOffer.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onGoToPriceStep)

        offerConditionsStep.compose<Offer> { requestConditionsOffer.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe { offer ->
                    onGoToConditionsStep.onNext(offer)
                    requestPaymentPlans.onNext(Unit)
                    requestCarrier.onNext(Unit)
                }

        offerMakeStep.compose<Offer> { requestMakeOffer.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe { offer ->
                    Timber.d("f9: requestMakeOffer")
                    onGoToMakeStep.onNext(offer)
                    requestPaymentPlans.onNext(Unit)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Conditions

        requestCarrier.flatMap{ enviorment.carrierRepository.getCarriersFromDb() }
                .filter{ it.isNotEmpty() }
                .map {
                    val offerCarrier = mutableListOf<OfferCarrier>()
                    it.forEach {
                        offerCarrier.add( OfferCarrier(offerCarrierCode = it.carriercode, offerCarrierName = it.carriername, isChecked = true) )
                    }
                    offerCarrier
                }
                .bindToLifeCycle()
                .subscribe(onSuccessCarrier)

        // BaseOfferViewModel 의 paymentPlans 에서 받아온다
        requestPaymentPlans
                .map { getPaymentPlans() }
                .bindToLifeCycle()
                .subscribe { plans ->
                    onRequestPaymentPlans.onNext(plans)
                }
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")

        // onResume 시 volumeStep 에 offer 가 없는 경우에만 요청한다
        if (!offerVolumeStep.hasValue()) {
            refresh.onNext(Parameter.EVENT)
        }
    }

    // interface : in
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun clickToBack(parameter: Parameter) = clickToBack.onNext(parameter)
    override fun clickToMake(offer: Offer) = clickToMake.onNext(offer)
    override fun clickToCheck(pair: Pair<Int, Offer?>) = clickToCheck.onNext(pair)

    override fun offerVolumeStep(offer: Offer) = offerVolumeStep.onNext(offer)
    override fun offerPriceStep(offer: Offer) = offerPriceStep.onNext(offer)
    override fun offerConditionsStep(offer: Offer) = offerConditionsStep.onNext(offer)
    override fun offerMakeStep(offer: Offer) = offerMakeStep.onNext(offer)

    override fun requestGoToOtherStep(pair: Pair<Int, Offer?>) = requestGoToOtherStep.onNext(pair)
    override fun requestVolumeOffer(parameter: Parameter) = requestVolumeOffer.onNext(parameter)
    override fun requestPriceOffer(parameter: Parameter) = requestPriceOffer.onNext(parameter)
    override fun requestConditionsOffer(parameter: Parameter) = requestConditionsOffer.onNext(parameter)
    override fun requestMakeOffer(parameter: Parameter) = requestMakeOffer.onNext(parameter)
    override fun requestPaymentPlans(unit: Unit) = requestPaymentPlans.onNext(unit)
    override fun requestCarrier(unit: Unit) = requestCarrier.onNext(unit)

    override fun requestCallRefreshAfterDiscard(parameter: Parameter) = requestCallRefreshAfterDiscard.onNext(parameter)

    // interface : out
    override fun onClickDone() : Observable<Parameter> = onClickDone
    override fun onClickBack() : Observable<Parameter> = onClickBack
    override fun onClickMake() : Observable<Boolean> = onClickMake
    override fun onSetDoneBtn() : Observable<Boolean> = onSetDoneBtn

    override fun onClickCheck(): Observable<Pair<Int, Offer?>> = onClickCheck

    override fun onGoToOtherStep() : Observable<Pair<Int, Offer?>> = onGoToOtherStep
    override fun onGoToVolumeStep(): Observable<Offer> = onGoToVolumeStep
    override fun onGoToPriceStep(): Observable<Offer> = onGoToPriceStep
    override fun onGoToConditionsStep(): Observable<Offer> = onGoToConditionsStep
    override fun onGoToMakeStep(): Observable<Offer> = onGoToMakeStep

    override fun onRequestPaymentPlans(): Observable<List<PaymentPlan>> = onRequestPaymentPlans
    override fun onSuccessCarriers(): Observable<List<OfferCarrier>> = onSuccessCarrier
}

interface BofWizardInputs {
    fun clickToDone(parameter: Parameter)
    fun clickToBack(parameter: Parameter)
    fun clickToMake(offer: Offer)
    fun clickToCheck(pair: Pair<Int, Offer?>)

    fun offerVolumeStep(offer: Offer)
    fun offerPriceStep(offer: Offer)
    fun offerConditionsStep(offer: Offer)
    fun offerMakeStep(offer: Offer)

    fun requestGoToOtherStep(pair: Pair<Int, Offer?>)
    fun requestVolumeOffer(parameter: Parameter)
    fun requestPriceOffer(parameter: Parameter)
    fun requestConditionsOffer(parameter: Parameter)
    fun requestMakeOffer(parameter: Parameter)
    fun requestPaymentPlans(unit: Unit)
    fun requestCarrier(unit: Unit)

    fun requestCallRefreshAfterDiscard(parameter: Parameter)
}

interface BofWizardOutputs {
    fun onClickBack(): Observable<Parameter>
    fun onClickMake(): Observable<Boolean>
    fun onClickDone(): Observable<Parameter>
    fun onSetDoneBtn(): Observable<Boolean>
    fun onClickCheck(): Observable<Pair<Int, Offer?>>

    fun onGoToOtherStep(): Observable<Pair<Int, Offer?>>
    fun onGoToVolumeStep() : Observable<Offer>
    fun onGoToPriceStep() : Observable<Offer>
    fun onGoToConditionsStep() : Observable<Offer>
    fun onGoToMakeStep() : Observable<Offer>

    fun onRequestPaymentPlans() : Observable<List<PaymentPlan>>
    fun onSuccessCarriers(): Observable<List<OfferCarrier>>
}