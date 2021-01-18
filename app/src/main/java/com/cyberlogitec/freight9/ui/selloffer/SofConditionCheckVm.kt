package com.cyberlogitec.freight9.ui.selloffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Contract
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

class SofConditionCheckVm(context: Context) : BaseViewModel(context), PlanCheckInputs, PlanCheckOutputs  {
    val inPuts: PlanCheckInputs = this
    private val clickToEdit = PublishSubject.create<Parameter>()
    private val clickToPlanDetail = PublishSubject.create<Parameter>()
    private val requestPaymentPlan = PublishSubject.create<String>()

    val outPuts: PlanCheckOutputs = this
    private val onClickEdit = PublishSubject.create<Contract>()
    private val onClickPlanDetail = PublishSubject.create<Contract>()

    private val onSuccessRefresh = PublishSubject.create<Contract>()
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<PaymentPlan>>()

    // intents
    private val parentId = BehaviorSubject.create<Contract>()
    private val refresh = PublishSubject.create<Parameter>()


    init {

        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .subscribe {
                    onSuccessRefresh.onNext(it)
                    requestPaymentPlan(it.paymentPlanCode)
                }

        parentId.compose<Contract> { clickToPlanDetail.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickPlanDetail)

        parentId.compose<Contract> { clickToEdit.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
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
    override fun onClickEdit() : Observable<Contract> = onClickEdit
    override fun onClickPlanDetail(): Observable<Contract> = onClickPlanDetail
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
    override fun onSuccessRequestPaymentPlan(): Observable<Response<PaymentPlan>> = onSuccessRequestPaymentPlan
}

interface PlanCheckInputs {
    fun clickToEdit(parameter: Parameter)
    fun clickToPlanDetail(parameter: Parameter)
    fun requestPaymentPlan(paymentPlanCode: String)
}

interface PlanCheckOutputs {
    fun onClickEdit() : Observable<Contract>
    fun onClickPlanDetail(): Observable<Contract>
    fun onSuccessRefresh(): Observable<Contract>
    fun onSuccessRequestPaymentPlan() : Observable<Response<PaymentPlan>>
}