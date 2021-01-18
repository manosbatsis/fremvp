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

class BofConditionCheckVm(context: Context) : BaseViewModel(context), ConditionCheckInputs, ConditionCheckOutputs  {
    val inPuts: ConditionCheckInputs = this
    private val clickToEdit = PublishSubject.create<Parameter>()
    private val clickToPlanDetail = PublishSubject.create<Parameter>()

    val outPuts: ConditionCheckOutputs = this
    private val onClickEdit = PublishSubject.create<Offer>()
    private val onClickPlanDetail = PublishSubject.create<Offer>()
    private val requestPaymentPlan = PublishSubject.create<String>()

    private val onSuccessRefresh = PublishSubject.create<Offer>()
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<PaymentPlan>>()

    // intents
    private val parentId = BehaviorSubject.create<Offer>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        intent.map { it.getSerializableExtra(Intents.OFFER) as Offer }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<Offer> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .subscribe(onSuccessRefresh)

        parentId.compose<Offer> { clickToPlanDetail.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickPlanDetail)

        parentId.compose<Offer> { clickToEdit.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickEdit)

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
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // inPuts
    override fun clickToEdit(parameter: Parameter) = clickToEdit.onNext(parameter)
    override fun clickToPlanDetail(parameter: Parameter) = clickToPlanDetail.onNext(parameter)
    override fun requestPaymentPlan(paymentPlanCode: String) = requestPaymentPlan.onNext(paymentPlanCode)

    // outPuts
    override fun onClickEdit() : Observable<Offer> = onClickEdit
    override fun onClickPlanDetail(): Observable<Offer> = onClickPlanDetail
    override fun onSuccessRefresh(): Observable<Offer> = onSuccessRefresh
    override fun onSuccessRequestPaymentPlan(): Observable<Response<PaymentPlan>> = onSuccessRequestPaymentPlan
}

interface ConditionCheckInputs {
    fun clickToEdit(parameter: Parameter)
    fun clickToPlanDetail(parameter: Parameter)
    fun requestPaymentPlan(paymentPlanCode: String)
}

interface ConditionCheckOutputs {
    fun onClickEdit() : Observable<Offer>
    fun onClickPlanDetail(): Observable<Offer>
    fun onSuccessRefresh(): Observable<Offer>
    fun onSuccessRequestPaymentPlan() : Observable<Response<PaymentPlan>>
}