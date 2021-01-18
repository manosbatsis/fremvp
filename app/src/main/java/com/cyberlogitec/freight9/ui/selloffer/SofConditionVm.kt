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

class SofConditionVm(context: Context) : BaseViewModel(context), PlanInputs, PlanOutputs  {
    val inPuts: PlanInputs = this
    private val clickToPlan = PublishSubject.create<Contract>()
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToNext = PublishSubject.create<Contract>()
    private val requestPaymentPlan = PublishSubject.create<Unit>()

    val outPuts: PlanOutputs = this
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickNext = BehaviorSubject.create<Contract>()

    // selected id
    private val onSuccessRefresh = PublishSubject.create<Contract>()    // Tot Sum
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<List<PaymentPlan>>>()

    // intents
    private val parentId = BehaviorSubject.create<Contract>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh

        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRefresh.onNext(it)
                    requestPaymentPlan(Unit)
                }

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

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click

        clickToPlan.bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        clickToNext.bindToLifeCycle()
                .bindToLifeCycle()
                .subscribe(onClickNext)

        clickToDone.bindToLifeCycle()
                .subscribe(onClickDone)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToPlan(parameter: Contract) = clickToPlan.onNext(parameter)
    override fun clickToNext(parameter: Contract) = clickToNext.onNext(parameter)
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun requestPaymentPlan(unit: Unit) = requestPaymentPlan.onNext(unit)

    // interface : out
    override fun onClickDone(): Observable<Parameter> = onClickDone
    override fun onClickNext(): Observable<Contract> = onClickNext
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
    override fun onSuccessRequestPaymentPlan(): Observable<Response<List<PaymentPlan>>> = onSuccessRequestPaymentPlan
}

interface PlanInputs {
    fun clickToPlan(parameter: Contract)
    fun clickToDone(parameter: Parameter)
    fun clickToNext(parameter: Contract)
    fun requestPaymentPlan(unit: Unit)
}

interface PlanOutputs {
    fun onClickDone(): Observable<Parameter>
    fun onClickNext(): Observable<Contract>
    fun onSuccessRefresh(): Observable<Contract>
    fun onSuccessRequestPaymentPlan() : Observable<Response<List<PaymentPlan>>>
}