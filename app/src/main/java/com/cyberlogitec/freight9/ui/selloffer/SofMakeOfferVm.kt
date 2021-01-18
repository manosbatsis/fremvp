package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ContainerSizeType.N20FT_TYPE
import com.cyberlogitec.freight9.config.ContainerType.F_TYPE
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SofMakeOfferVm(context: Context) : BaseViewModel(context), MakeOfferInputs, MakeOfferOutputs  {

    // inPuts
    val inPuts: MakeOfferInputs = this
    private val clickToNext = PublishSubject.create<Offer>()
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToConditionDetail = PublishSubject.create<Parameter>()
    private val clickToWholeRoute = PublishSubject.create<Parameter>()
    private val clickToPriceTable = PublishSubject.create<Parameter>()
    private val clickToVolumeCheck = PublishSubject.create<Parameter>()
    private val clickToPriceCheck = PublishSubject.create<Parameter>()
    private val clickToPlanCheck = PublishSubject.create<Parameter>()
    private val clickToIagree = PublishSubject.create<Boolean>()
    private val clickToDealOptionsInfo = PublishSubject.create<Parameter>()

    // outPuts
    val outPuts: MakeOfferOutputs = this
    private val onSuccessRefresh = PublishSubject.create<Contract>()
    private val onSuccessRefresh2 = PublishSubject.create<Offer>()
    private val onClickNext = PublishSubject.create<Parameter>()
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickConditionDetail = PublishSubject.create<Contract>()
    private val onClickWholeRoute = PublishSubject.create<Contract>()
    private val onClickPriceTable = PublishSubject.create<Contract>()
    private val onClickVolumeCheck = PublishSubject.create<Contract>()
    private val onClickPriceCheck = PublishSubject.create<Contract>()
    private val onClickPlanCheck = PublishSubject.create<Contract>()
    private val onClickIagree = PublishSubject.create<Boolean>()
    private val onClickDealOptionsInfo = PublishSubject.create<Parameter>()

    // intents
    private val parentId = BehaviorSubject.create<Contract>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .map { contract ->
                    // Pay Plan -> paymentPlanCode (01: plan1, 02:plan2, 03:plan3)
                    var firstPaymentRatio = 0.0f
                    var middlePaymentRatio = 0.0f
                    var balancedPaymentRatio = 0.0f

                    // Pay Plan (PaymentTerm)
                    val paymentPlanJson:String = context.assets.open("payment_plan.json").bufferedReader().use { it.readText() }
                    val paymentPlansType = object: TypeToken<MutableList<PaymentPlan>>() {}.type
                    val paymentPlans :List<PaymentPlan> = Gson().fromJson(paymentPlanJson, paymentPlansType)

                    paymentPlans.find { it.paymentPlanCode == contract.paymentPlanCode }?.let {
                        firstPaymentRatio = it.initialPaymentRatio
                        middlePaymentRatio = it.middlePaymentRatio
                        balancedPaymentRatio = it.balancePaymentRatio
                    }

                    // offerLineItems
                    val offerLineItems = mutableListOf<OfferLineItem>()
                    contract.masterContractLineItems?.forEach { lineItem ->
                        val offerLineItem = OfferLineItem(tradeContainerTypeCode = F_TYPE, tradeContainerSizeCode = N20FT_TYPE)
                        val offerPrices = mutableListOf<OfferPrice>()

                        offerLineItem.baseYearWeek = lineItem.baseYearWeek
                        offerLineItem.offerQty = lineItem.offerQty
                        offerLineItem.offerPrice = lineItem.offerPrice

                        offerLineItem.firstPaymentRatio = firstPaymentRatio
                        offerLineItem.middlePaymentRatio = middlePaymentRatio
                        offerLineItem.balancedPaymentRatio = balancedPaymentRatio

                        lineItem.masterContractPrices?.forEach {
                            offerPrices.add(OfferPrice(offerPrice = it.price, containerSizeCode = it.containerSizeCode, containerTypeCode = it.containerTypeCode))
                        }
                        offerLineItem.offerPrices = offerPrices

                        offerLineItems.add( offerLineItem )
                    }

                    // offerRoutes
                    val offerRoutes = mutableListOf<OfferRoute>()
                    contract.masterContractRoutes?.let { contractRoutes ->
                        contractRoutes.forEach {
                            val offerRoute = OfferRoute(offerRegSeq = it.regSeq, locationCode = it.locationCode, locationTypeCode = it.locationTypeCode)
                            offerRoutes.add((offerRoute))
                        }
                    }

                    // offerCarriers
                    val offerCarriers = mutableListOf<OfferCarrier>()
                    contract.masterContractCarriers?.let { carriers ->
                        carriers.filter{ it.deleteYn != "1" }.forEach {
                            val offerCarrier = OfferCarrier(offerCarrierCode = it.carrierCode)
                            offerCarriers.add(offerCarrier)
                        }
                    }

                    // make offer
                    val offer = Offer(
                            masterContractNumber = contract.masterContractNumber,
                            offerTypeCode = "S",
                            offerRdTermCode = contract.rdTermCode,
                            offerPaymentTermCode = contract.paymentTermCode,
                            offerLineItems = offerLineItems,
                            offerRoutes = offerRoutes,
                            offerCarriers = offerCarriers,
                            offerPaymentPlanCode = contract.paymentPlanCode
                    )

                    offer
                }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh2)

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToNext

        clickToNext
                .map{ showLoadingDialog.onNext(Parameter.EVENT); it }
                //
                // @POST("api/v1/product/offer/new")
                //
                .flatMapSingle { enviorment.apiTradeClient.postOffer(it).handleToError(hideLoadingDialog).neverError().toSingle{} }
                .map { hideLoadingDialog.onNext(Throwable("OK")) }
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(onClickNext)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToDone

        clickToDone.bindToLifeCycle()
                .subscribe(onClickDone)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Table

        parentId.compose<Contract> { clickToConditionDetail.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickConditionDetail)

        parentId.compose<Contract> { clickToWholeRoute.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickWholeRoute)

        parentId.compose<Contract> { clickToPriceTable.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onClickPriceTable)


        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Check

        parentId.compose<Contract> { clickToVolumeCheck.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickVolumeCheck)

        parentId.compose<Contract> { clickToPriceCheck.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickPriceCheck)

        parentId.compose<Contract> { clickToPlanCheck.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickPlanCheck)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Iagree

        clickToIagree.bindToLifeCycle()
                .subscribe(onClickIagree)

        clickToDealOptionsInfo.bindToLifeCycle()
                .subscribe(onClickDealOptionsInfo)

    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToNext(parameter: Offer) = clickToNext.onNext(parameter)
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)

    override fun clickToConditionDetail(parameter: Parameter) = clickToConditionDetail.onNext(parameter)
    override fun clickToWholeRoute(parameter: Parameter) = clickToWholeRoute.onNext(parameter)
    override fun clickToPriceTable(parameter: Parameter) = clickToPriceTable.onNext(parameter)

    override fun clickToVolumeCheck(parameter: Parameter) = clickToVolumeCheck.onNext(parameter)
    override fun clickToPriceCheck(parameter: Parameter) = clickToPriceCheck.onNext(parameter)
    override fun clickToPlanCheck(parameter: Parameter) = clickToPlanCheck.onNext(parameter)
    override fun clickToIagree(parameter: Boolean) = clickToIagree.onNext(parameter)
    override fun clickToDealOptionsInfo(parameter: Parameter) = onClickDealOptionsInfo.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
    override fun onSuccessRefresh2(): Observable<Offer> = onSuccessRefresh2
    override fun onClickNext(): Observable<Parameter> = onClickNext
    override fun onClickDone() : Observable<Parameter> = onClickDone
    override fun onClickConditionDetail(): Observable<Contract> = onClickConditionDetail
    override fun onClickWholeRoute(): Observable<Contract> = onClickWholeRoute
    override fun onClickPriceTable(): Observable<Contract> = onClickPriceTable
    override fun onClickVolumeCheck(): Observable<Contract> = onClickVolumeCheck
    override fun onClickPriceCheck(): Observable<Contract> = onClickPriceCheck
    override fun onClickPlanCheck(): Observable<Contract> = onClickPlanCheck
    override fun onClickIagree(): Observable<Boolean> = onClickIagree
    override fun onClickDealOptionsInfo(): Observable<Parameter> = onClickDealOptionsInfo
}

interface MakeOfferInputs {
    fun clickToNext(parameter: Offer)
    fun clickToDone(parameter: Parameter)
    fun clickToConditionDetail(parameter: Parameter)
    fun clickToWholeRoute(parameter: Parameter)
    fun clickToPriceTable(parameter: Parameter)
    fun clickToVolumeCheck(parameter: Parameter)
    fun clickToPriceCheck(parameter: Parameter)
    fun clickToPlanCheck(parameter: Parameter)
    fun clickToIagree(parameter: Boolean)
    fun clickToDealOptionsInfo(parameter: Parameter)
}

interface MakeOfferOutputs {
    fun onSuccessRefresh(): Observable<Contract>
    fun onSuccessRefresh2(): Observable<Offer>
    fun onClickNext(): Observable<Parameter>
    fun onClickDone(): Observable<Parameter>
    fun onClickConditionDetail(): Observable<Contract>
    fun onClickWholeRoute(): Observable<Contract>
    fun onClickPriceTable(): Observable<Contract>
    fun onClickVolumeCheck(): Observable<Contract>
    fun onClickPriceCheck(): Observable<Contract>
    fun onClickPlanCheck(): Observable<Contract>
    fun onClickIagree(): Observable<Boolean>
    fun onClickDealOptionsInfo(): Observable<Parameter>
}