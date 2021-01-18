package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Offer
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

class BofMakeOfferVm(context: Context) : BaseViewModel(context), MakeOfferInputs, MakeOfferOutputs  {

    // inPuts
    val inPuts: MakeOfferInputs = this
    private val clickToNext = PublishSubject.create<Offer>()
    private val clickToDone = PublishSubject.create<Parameter>()
    
    private val clickToVolumeCheck = PublishSubject.create<Parameter>()
    private val clickToPriceCheck = PublishSubject.create<Parameter>()
    private val clickToPlanCheck = PublishSubject.create<Parameter>()
    private val clickToIagree = PublishSubject.create<Boolean>()
    private val requestPaymentPlan = PublishSubject.create<String>()

    // outPuts
    val outPuts: MakeOfferOutputs = this
    private val onSuccessRefresh = PublishSubject.create<Offer>()
    private val onClickNext = PublishSubject.create<Parameter>()
    private val onClickDone = PublishSubject.create<Parameter>()
    
    private val onClickVolumeCheck = PublishSubject.create<Offer>()
    private val onClickPriceCheck = PublishSubject.create<Offer>()
    private val onClickPlanCheck = PublishSubject.create<Offer>()
    private val onClickIagree = PublishSubject.create<Boolean>()

    // intents
    private val parentId = BehaviorSubject.create<Offer>()
    private val refresh = PublishSubject.create<Parameter>()

    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<PaymentPlan>>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { it.getSerializableExtra(Intents.OFFER) as Offer }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh
        parentId.compose<Offer> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRefresh.onNext(it)
                    it.offerPaymentPlanCode?.let { paymentPlanCode ->
                        requestPaymentPlan(paymentPlanCode)
                    }
                }

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
        // clickTo Check

        parentId.compose<Offer> { clickToVolumeCheck.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickVolumeCheck)

        parentId.compose<Offer> { clickToPriceCheck.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickPriceCheck)

        parentId.compose<Offer> { clickToPlanCheck.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickPlanCheck)

        requestPaymentPlan
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getPaymentPlan(it)
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

        ///////////////////////////////////////////////////////////////////////////////////////////
        // clickTo Iagree

        clickToIagree.bindToLifeCycle()
                .subscribe(onClickIagree)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToNext(parameter: Offer) = clickToNext.onNext(parameter)
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)

    override fun clickToVolumeCheck(parameter: Parameter) = clickToVolumeCheck.onNext(parameter)
    override fun clickToPriceCheck(parameter: Parameter) = clickToPriceCheck.onNext(parameter)
    override fun clickToPlanCheck(parameter: Parameter) = clickToPlanCheck.onNext(parameter)
    override fun clickToIagree(parameter: Boolean) = clickToIagree.onNext(parameter)
    override fun requestPaymentPlan(paymentPlanCode: String) = requestPaymentPlan.onNext(paymentPlanCode)

    // interface : out
    override fun onSuccessRefresh(): Observable<Offer> = onSuccessRefresh
    override fun onSuccessRequestPaymentPlan(): Observable<Response<PaymentPlan>> = onSuccessRequestPaymentPlan
    override fun onClickNext(): Observable<Parameter> = onClickNext
    override fun onClickDone() : Observable<Parameter> = onClickDone

    override fun onClickVolumeCheck(): Observable<Offer> = onClickVolumeCheck
    override fun onClickPriceCheck(): Observable<Offer> = onClickPriceCheck
    override fun onClickPlanCheck(): Observable<Offer> = onClickPlanCheck
    override fun onClickIagree(): Observable<Boolean> = onClickIagree
}

interface MakeOfferInputs {
    fun clickToNext(parameter: Offer)
    fun clickToDone(parameter: Parameter)

    fun clickToVolumeCheck(parameter: Parameter)
    fun clickToPriceCheck(parameter: Parameter)
    fun clickToPlanCheck(parameter: Parameter)
    fun clickToIagree(parameter: Boolean)

    fun requestPaymentPlan(paymentPlanCode: String)
}

interface MakeOfferOutputs {
    fun onSuccessRefresh(): Observable<Offer>
    fun onSuccessRequestPaymentPlan() : Observable<Response<PaymentPlan>>
    fun onClickNext(): Observable<Parameter>
    fun onClickDone(): Observable<Parameter>

    fun onClickVolumeCheck(): Observable<Offer>
    fun onClickPriceCheck(): Observable<Offer>
    fun onClickPlanCheck(): Observable<Offer>
    fun onClickIagree(): Observable<Boolean>
}