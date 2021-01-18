package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.PaymentPlanCode.PS
import com.cyberlogitec.freight9.config.PaymentTermCode.PPD
import com.cyberlogitec.freight9.config.RdTermCode.CY_CY
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber

class BofConditionVm(context: Context) : BaseViewModel(context), ConditionInputs, ConditionOutputs  {
    val inPuts: ConditionInputs = this
    private val clickToNext = PublishSubject.create<Offer>()
    private val clickToPlan = PublishSubject.create<Offer>()
    private val requestPaymentPlan = PublishSubject.create<Unit>()

    val outPuts: ConditionOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val onClickNext = PublishSubject.create<Offer>()
    private val onSuccessRefresh = PublishSubject.create<Offer>()
    private val onSuccessRefresh2 = PublishSubject.create<List<OfferCarrier>>()
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<List<PaymentPlan>>>()

    init {
        val parentId = BehaviorSubject.create<Offer>()

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent

        intent.map { it.getSerializableExtra(Intents.OFFER) as Offer }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( parentId )


        ////////////////////////////////////////////////////////////////////////////////////////////
        
        parentId.compose<Offer> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { it.offerPaymentPlanCode = PS; it }
                .map { it.offerPaymentTermCode = PPD; it }
                .map { it.offerRdTermCode = CY_CY; it }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRefresh.onNext(it)
                    requestPaymentPlan(Unit)
                }

        refresh.flatMap{ enviorment.carrierRepository.getCarriersFromDb() }
                .filter{ it.isNotEmpty() }
                .map {
                    val offerCarrier = mutableListOf<OfferCarrier>()
                    it.forEach {
                        offerCarrier.add( OfferCarrier(offerCarrierCode = it.carriercode, offerCarrierName = it.carriername, isChecked = true) )
                    }
                    offerCarrier
                }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh2)

        clickToPlan.bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        clickToNext.bindToLifeCycle()
                .subscribe(onClickNext)

        requestPaymentPlan
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getPaymentPlan()
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestPaymentPlan.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }


    // inPuts
    override fun clickToNext(parameter: Offer) = clickToNext.onNext(parameter)
    override fun clickToPlan(parameter: Offer) = clickToPlan.onNext(parameter)
    override fun requestPaymentPlan(unit: Unit) = requestPaymentPlan.onNext(unit)

    // outPuts
    override fun onClickNext() : Observable<Offer> = onClickNext
    override fun onSuccessRefresh(): Observable<Offer> = onSuccessRefresh
    override fun onSuccessRefresh2(): Observable<List<OfferCarrier>> = onSuccessRefresh2
    override fun onSuccessRequestPaymentPlan(): Observable<Response<List<PaymentPlan>>> = onSuccessRequestPaymentPlan
}

interface ConditionInputs {
    fun clickToNext(parameter: Offer)
    fun clickToPlan(parameter: Offer)
    fun requestPaymentPlan(unit: Unit)
}

interface ConditionOutputs {
    fun onClickNext() : Observable<Offer>
    fun onSuccessRefresh(): Observable<Offer>
    fun onSuccessRefresh2(): Observable<List<OfferCarrier>>
    fun onSuccessRequestPaymentPlan() : Observable<Response<List<PaymentPlan>>>
}