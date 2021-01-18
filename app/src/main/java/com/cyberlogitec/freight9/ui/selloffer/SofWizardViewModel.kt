package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseOfferViewModel
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.fromJson
import com.cyberlogitec.freight9.lib.util.getTodayYearWeekNumber
import com.cyberlogitec.freight9.lib.util.toJson
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_MAKE
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_PLAN
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_PRICE
import com.cyberlogitec.freight9.ui.selloffer.SofWizardActivity.Companion.STEP_VOLUME
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofWizardViewModel(context: Context) : BaseOfferViewModel(context),
        SofWizardInputs, SofWizardOutputs  {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // inputs

    val inPuts: SofWizardInputs = this
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToMake = PublishSubject.create<Offer>()
    private val clickToBack = PublishSubject.create<Parameter>()

    private val clickToConditionDetail = PublishSubject.create<Parameter>()
    private val clickToWholeRoute = PublishSubject.create<Parameter>()
    private val clickToPriceTable = PublishSubject.create<Parameter>()

    private val clickToCheck = PublishSubject.create<Pair<Int, Contract?>>()

    private val clickToIagree = PublishSubject.create<Boolean>()
    private val clickToDealOptionsInfo = PublishSubject.create<Parameter>()

    private val contractVolumeStep = BehaviorSubject.create<Contract>()
    private val contractPriceStep = BehaviorSubject.create<Contract>()
    private val contractPlanStep = BehaviorSubject.create<Contract>()
    private val contractMakeStep = BehaviorSubject.create<Contract>()
    private val requestPaymentPlans = PublishSubject.create<Unit>()

    private val callSetup = PublishSubject.create<Contract>()
    private val requestGoToOtherStep = PublishSubject.create<Pair<Int, Contract?>>()
    private val requestVolumeContract = PublishSubject.create<Parameter>()
    private val requestPriceContract = PublishSubject.create<Parameter>()
    private val requestPlanContract = PublishSubject.create<Parameter>()
    private val requestMakeContract = PublishSubject.create<Parameter>()
    private val requestCallRefreshAfterDiscard = PublishSubject.create<Parameter>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // outputs

    val outPuts: SofWizardOutputs = this
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickMake = PublishSubject.create<Boolean>()
    private val onClickBack = PublishSubject.create<Parameter>()
    private val onSetDoneBtn = BehaviorSubject.create<Boolean>()

    private val onSuccessRefresh = BehaviorSubject.create<Contract>()

    private val onClickConditionDetail = PublishSubject.create<Contract>()
    private val onClickWholeRoute = PublishSubject.create<Contract>()
    private val onClickPriceTable = PublishSubject.create<Contract>()

    private val onClickCheck = PublishSubject.create<Pair<Int, Contract?>>()

    private val onClickIagree = PublishSubject.create<Boolean>()
    private val onClickDealOptionsInfo = PublishSubject.create<Parameter>()

    private val onGoToOtherStep = PublishSubject.create<Pair<Int, Contract?>>()
    private val onGoToVolumeStep = PublishSubject.create<Contract>()
    private val onGoToPriceStep = PublishSubject.create<Contract>()
    private val onGoToPlanStep = PublishSubject.create<Contract>()
    private val onGoToMakeStep = PublishSubject.create<Contract>()
    private val onRequestPaymentPlans = PublishSubject.create<List<PaymentPlan>>()

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

        ///////////////////////////////////////////////////////////////////////////////////////////
        // refresh (masterContractNumber)
        // 라우트 (POL, POD) 표시 (inventory db 조회)

        parentId.compose<Intent> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { Timber.d("f9: SofWizardViewModel : parentId--> ${it}"); it }
                .map { intent ->
                    val masterContractNumber = intent.getStringExtra(Intents.MSTR_CTRK_NR)
                    masterContractNumber
                }
                .flatMapMaybe { enviorment.apiTradeClient.getContract(it).handleToError(hideLoadingDialog).neverError() }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { listOf(it).isNotEmpty() }
                .map { contract ->
                    enviorment.currentUser.getCrcyCd()?.let {
                        contract.tradeCompanyCode = it
                    }
                    contract
                }
                .map { contract ->
                    contract.tradeRoleCode = "001"
                    contract
                }
                .map { contract ->
                    val masterContractLineItems = contract.masterContractLineItems
                            ?.filter { it.baseYearWeek >= getTodayYearWeekNumber() }
                            ?.sortedBy { it.baseYearWeek }
                    contract.masterContractLineItems = masterContractLineItems
                    contract.masterContractLineItems?.filter{ it.deleteYn != "1'" }?.forEach { lineItem ->
                        lineItem.isChecked = false
                        contract.inventory?.let {
                            it.inventoryDetails = it.inventoryDetails?.sortedBy { it.baseYearWeek }
                            if ( it.deleteYn != "1") {
                                it.inventoryDetails?.filter{ it.deleteYn != "1" }?.find { it.baseYearWeek == lineItem.baseYearWeek }?.let {
                                    lineItem.remainderQty = it.remainderConfirmedQty
                                    lineItem.costPrice = 0.0f
                                    lineItem.offerPrice = 0
                                }
                            }
                        }
                    }
                    contract
                }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe { contract ->
                    callSetup.onNext(contract)
                }

        /*
        * compose<R> - R : return type
        * contract - callSetup 로 전달받은 값
        * intent - parentId 에 있는 Behavior 값
        * */
        parentId.compose<Triple<Triple<Boolean, Boolean, Boolean>, Contract, Offer>> {
            callSetup.withLatestFrom(it, BiFunction { contract, intent ->
                        var offer = Offer()
                        if (intent.hasExtra(Intents.OFFER)) {
                            offer = intent.getSerializableExtra(Intents.OFFER) as Offer
                        }
                        // 특정 조건으로 호출되었는지 여부
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

                        Triple(Triple(offerByMadeCondition, offerDiscard, offerMakeStep), contract, offer) })}

                .bindToLifeCycle()
                .subscribe { triple ->

                    val offerByMadeCondition = triple.first.first
                    val offerDiscard = triple.first.second
                    val offerMakeStep = triple.first.third
                    val contract = triple.second
                    val offer = triple.third

                    /*
                    * offer 를 전달 받은 경우에만
                    * */
                    if (offerByMadeCondition) {

                        contract.paymentPlanCode = offer.offerPaymentPlanCode
                        contract.paymentTermCode = offer.offerPaymentTermCode
                        contract.rdTermCode = offer.offerRdTermCode

                        contract.masterContractLineItems?.map { masterContractLineItem ->
                            val offerLineItem = offer.offerLineItems?.find {
                                it.baseYearWeek == masterContractLineItem.baseYearWeek
                            }

                            offerLineItem?.let { lineItem ->
                                masterContractLineItem.offerQty = lineItem.offerQty
                                masterContractLineItem.offerPrice = lineItem.offerPrice
                                masterContractLineItem.isChecked = lineItem.offerQty > 0

                                val contractPrices = mutableListOf<ContractPrice>()
                                lineItem.offerPrices?.forEach { it ->
                                    contractPrices.add(ContractPrice(0, EmptyString, EmptyString, EmptyString, EmptyString, EmptyString,
                                            0,
                                            contract.masterContractNumber,
                                            lineItem.baseYearWeek,
                                            it.containerTypeCode,
                                            it.containerSizeCode,
                                            it.offerPrice,
                                            "0"))
                                }
                                if (contractPrices.isNotEmpty()) {
                                    masterContractLineItem.masterContractPrices = contractPrices
                                }
                            }
                        }

                        val contractRoutes = mutableListOf<ContractRoute>()
                        offer.offerRoutes?.forEach { offerRoute ->
                            contractRoutes.add(ContractRoute(0, EmptyString, EmptyString, EmptyString, EmptyString, EmptyString,
                                    0,
                                    contract.masterContractNumber,
                                    offerRoute.offerRegSeq,
                                    offerRoute.locationCode,
                                    offerRoute.locationName,
                                    offerRoute.locationTypeCode,
                                    "0"))
                        }
                        if (contractRoutes.isNotEmpty()) {
                            contract.masterContractRoutes = contractRoutes
                        }

                        val contractCarriers = mutableListOf<ContractCarrier>()
                        offer.offerCarriers?.forEach { offerCarrier ->
                            contractCarriers.add(ContractCarrier(0, EmptyString, EmptyString, EmptyString, EmptyString, EmptyString,
                                    0,
                                    contract.masterContractNumber,
                                    offerCarrier.offerCarrierCode,
                                    "0"))
                        }
                        if (contractCarriers.isNotEmpty()) {
                            contract.masterContractCarriers = contractCarriers
                        }
                    }

                    // DONE Btn enable
                    onSetDoneBtn.onNext(contract.masterContractLineItems?.isNotEmpty() ?: false)

                    // Volume step 인 경우 Behavior 에 넣고 바로 요청한다(Your Offers 에서 호출된 경우 : STEP_MAKE로 이동)
                    requestGoToOtherStep.onNext(Pair(if (offerMakeStep) STEP_MAKE else STEP_VOLUME, contract))

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
                    when(it.first) {
                        STEP_VOLUME -> {
                            // contract 이 null 이 아닌경우
                            it.second?.let { contract ->
                                // Behavior 에 넣는다
                                // Deep Copy
                                val contractDeepCopy = contract.toJson().fromJson<Contract>()
                                contractDeepCopy?.let { deepCopy ->
                                    contractVolumeStep.onNext(deepCopy)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - contractVolumeStep : " + contractVolumeStep.value.toString())
                            // onGoToVolumeStep 로 받는다
                            requestVolumeContract.onNext(Parameter.EVENT)
                        }
                        STEP_PRICE -> {
                            // contract 이 null 이 아닌경우
                            it.second?.let { contract ->
                                // Behavior 에 넣는다
                                // Deep Copy
                                // Volume Fragment 에서 입력된 volume 을 유지시킨다
                                val contractDeepCopy = contract.toJson().fromJson<Contract>()
                                contractDeepCopy?.let { deepCopy ->
                                    contractVolumeStep.onNext(deepCopy)
                                    contractPriceStep.onNext(deepCopy)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - contractPriceStep : " + contractPriceStep.value)
                            // onGoToPriceStep 로 받는다
                            requestPriceContract.onNext(Parameter.EVENT)
                        }
                        STEP_PLAN -> {
                            // contract 이 null 이 아닌경우
                            it.second?.let { contract ->
                                // Behavior 에 넣는다
                                // Deep Copy
                                val contractDeepCopy = contract.toJson().fromJson<Contract>()
                                contractDeepCopy?.let { deepCopy ->
                                    contractVolumeStep.onNext(deepCopy)
                                    contractPriceStep.onNext(deepCopy)
                                    contractPlanStep.onNext(deepCopy)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - contractPlanStep : " + contractPlanStep.value)
                            // onGoToPlanStep 로 받는다
                            requestPlanContract.onNext(Parameter.EVENT)
                        }
                        STEP_MAKE -> {
                            // contract 이 null 이 아닌경우
                            it.second?.let { contract ->
                                // Behavior 에 넣는다
                                // Deep Copy
                                val contractDeepCopy = contract.toJson().fromJson<Contract>()
                                contractDeepCopy?.let { deepCopy ->
                                    contractVolumeStep.onNext(deepCopy)
                                    contractPriceStep.onNext(deepCopy)
                                    contractPlanStep.onNext(deepCopy)
                                    contractMakeStep.onNext(deepCopy)
                                }
                            }
                            Timber.d("f9: ### requestGoToOtherStep - contractMakeStep : " + contractMakeStep.value)
                            // onGoToMakeStep 로 받는다
                            requestMakeContract.onNext(Parameter.EVENT)
                        }
                    }
                }

        clickToBack.bindToLifeCycle()
                .subscribe(onClickBack)

        clickToDone.bindToLifeCycle()
                .subscribe(onClickDone)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToMake

        clickToMake
                .map{ showLoadingDialog.onNext(Parameter.EVENT); it }
                //
                // @POST("api/v1/product/offer/new")
                //
                .flatMapSingle { enviorment.apiTradeClient.postOffer(it).handleToError(hideLoadingDialog).neverError().toSingle{} }
                .map { hideLoadingDialog.onNext(Throwable("OK")) }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe {
                    onClickMake.onNext(discardIntent.value ?: false)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToConditionDetail
        // volume step 의 contract 전달
        contractVolumeStep.compose<Contract> { clickToConditionDetail.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickConditionDetail)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToWholeRoute
        // volume step 의 contract 전달
        contractVolumeStep.compose<Contract> { clickToWholeRoute.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickWholeRoute)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickToPriceTable
        // volume step 의 contract 전달
        contractVolumeStep.compose<Contract> { clickToPriceTable.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickPriceTable)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Check : Volume, Price, Plan
        clickToCheck.bindToLifeCycle()
                .subscribe(onClickCheck)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Iagree

        clickToIagree.bindToLifeCycle()
                .subscribe(onClickIagree)

        clickToDealOptionsInfo.bindToLifeCycle()
                .subscribe(onClickDealOptionsInfo)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Move step

        contractVolumeStep.compose<Contract> { requestVolumeContract.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onGoToVolumeStep)

        contractPriceStep.compose<Contract> { requestPriceContract.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onGoToPriceStep)

        contractPlanStep.compose<Contract> { requestPlanContract.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onGoToPlanStep)

        contractMakeStep.compose<Contract> { requestMakeContract.withLatestFrom(it, BiFunction { _, t2 -> t2}) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe { contract ->
                    onGoToMakeStep.onNext(contract)
                    requestPaymentPlans.onNext(Unit)
                }

        ///////////////////////////////////////////////////////////////////////////////////////////

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

        // onResume 시 volumeStep 에 contract 이 없는 경우에만 요청한다
        if (!contractVolumeStep.hasValue()) {
            refresh.onNext(Parameter.EVENT)
        }
    }

    // interface : in
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun clickToBack(parameter: Parameter) = clickToBack.onNext(parameter)
    override fun clickToMake(offer: Offer) = clickToMake.onNext(offer)

    override fun clickToConditionDetail(parameter: Parameter) = clickToConditionDetail.onNext(parameter)
    override fun clickToWholeRoute(parameter: Parameter) = clickToWholeRoute.onNext(parameter)
    override fun clickToPriceTable(parameter: Parameter) = clickToPriceTable.onNext(parameter)

    override fun clickToCheck(pair: Pair<Int, Contract?>) = clickToCheck.onNext(pair)

    override fun clickToIagree(parameter: Boolean) = clickToIagree.onNext(parameter)
    override fun clickToDealOptionsInfo(parameter: Parameter) = onClickDealOptionsInfo.onNext(parameter)

    override fun contractVolumeStep(contract: Contract) = contractVolumeStep.onNext(contract)
    override fun contractPriceStep(contract: Contract) = contractPriceStep.onNext(contract)
    override fun contractPlanStep(contract: Contract) = contractPlanStep.onNext(contract)
    override fun contractMakeStep(contract: Contract) = contractMakeStep.onNext(contract)

    override fun requestPaymentPlans(unit: Unit) = requestPaymentPlans.onNext(unit)

    override fun callSetup(contract: Contract) = callSetup.onNext(contract)
    override fun requestGoToOtherStep(pair: Pair<Int, Contract?>) = requestGoToOtherStep.onNext(pair)
    override fun requestVolumeContract(parameter: Parameter) = requestVolumeContract.onNext(parameter)
    override fun requestPriceContract(parameter: Parameter) = requestPriceContract.onNext(parameter)
    override fun requestPlanContract(parameter: Parameter) = requestPlanContract.onNext(parameter)
    override fun requestMakeContract(parameter: Parameter) = requestMakeContract.onNext(parameter)

    override fun requestCallRefreshAfterDiscard(parameter: Parameter) = requestCallRefreshAfterDiscard.onNext(parameter)

    // interface : out
    override fun onRequestPaymentPlans(): Observable<List<PaymentPlan>> = onRequestPaymentPlans
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh

    override fun onClickDone() : Observable<Parameter> = onClickDone
    override fun onClickBack() : Observable<Parameter> = onClickBack
    override fun onClickMake() : Observable<Boolean> = onClickMake
    override fun onSetDoneBtn() : Observable<Boolean> = onSetDoneBtn

    override fun onClickConditionDetail() : Observable<Contract> = onClickConditionDetail
    override fun onClickWholeRoute() : Observable<Contract> = onClickWholeRoute
    override fun onClickPriceTable() : Observable<Contract> = onClickPriceTable

    override fun onClickCheck(): Observable<Pair<Int, Contract?>> = onClickCheck

    override fun onClickIagree(): Observable<Boolean> = onClickIagree
    override fun onClickDealOptionsInfo(): Observable<Parameter> = onClickDealOptionsInfo

    override fun onGoToOtherStep() : Observable<Pair<Int, Contract?>> = onGoToOtherStep
    override fun onGoToVolumeStep(): Observable<Contract> = onGoToVolumeStep
    override fun onGoToPriceStep(): Observable<Contract> = onGoToPriceStep
    override fun onGoToPlanStep(): Observable<Contract> = onGoToPlanStep
    override fun onGoToMakeStep(): Observable<Contract> = onGoToMakeStep
}

interface SofWizardInputs {
    fun clickToDone(parameter: Parameter)
    fun clickToBack(parameter: Parameter)
    fun clickToMake(offer: Offer)

    fun clickToCheck(pair: Pair<Int, Contract?>)

    fun clickToIagree(parameter: Boolean)
    fun clickToDealOptionsInfo(parameter: Parameter)

    fun clickToConditionDetail(parameter: Parameter)
    fun clickToWholeRoute(parameter: Parameter)
    fun clickToPriceTable(parameter: Parameter)

    fun contractVolumeStep(contract: Contract)
    fun contractPriceStep(contract: Contract)
    fun contractPlanStep(contract: Contract)
    fun contractMakeStep(contract: Contract)

    fun requestPaymentPlans(unit: Unit)

    fun callSetup(contract: Contract)
    fun requestGoToOtherStep(pair: Pair<Int, Contract?>)
    fun requestVolumeContract(parameter: Parameter)
    fun requestPriceContract(parameter: Parameter)
    fun requestPlanContract(parameter: Parameter)
    fun requestMakeContract(parameter: Parameter)

    fun requestCallRefreshAfterDiscard(parameter: Parameter)
}

interface SofWizardOutputs {
    fun onClickBack(): Observable<Parameter>
    fun onClickMake(): Observable<Boolean>
    fun onClickDone(): Observable<Parameter>
    fun onSetDoneBtn(): Observable<Boolean>

    fun onSuccessRefresh(): Observable<Contract>
    fun onRequestPaymentPlans() : Observable<List<PaymentPlan>>

    fun onClickConditionDetail() : Observable<Contract>
    fun onClickWholeRoute() : Observable<Contract>
    fun onClickPriceTable() : Observable<Contract>

    fun onClickCheck(): Observable<Pair<Int, Contract?>>

    fun onClickIagree(): Observable<Boolean>
    fun onClickDealOptionsInfo(): Observable<Parameter>

    fun onGoToOtherStep(): Observable<Pair<Int, Contract?>>
    fun onGoToVolumeStep() : Observable<Contract>
    fun onGoToPriceStep() : Observable<Contract>
    fun onGoToPlanStep() : Observable<Contract>
    fun onGoToMakeStep() : Observable<Contract>
}