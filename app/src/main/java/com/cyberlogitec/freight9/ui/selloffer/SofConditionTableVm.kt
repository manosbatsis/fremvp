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

class SofConditionTableVm(context: Context) : BaseViewModel(context), ConditionTableInputs, ConditionTableOutputs {
    val inPuts: ConditionTableInputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val requestPaymentPlan = PublishSubject.create<String>()
    private val requestCarrierDescription = PublishSubject.create<String>()

    val outPuts: ConditionTableOutputs = this
    private val parentId = BehaviorSubject.create<Contract>()

    private val onSuccessRefresh = PublishSubject.create<Contract>()
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<PaymentPlan>>()
    private val onSuccessRequestCarrierDescription = PublishSubject.create<String>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        //
        intent.map { it.getSerializableExtra(Intents.MSTR_CTRK) as Contract }
                .filter { listOf(it).isNotEmpty() }
                .neverError()
                .bindToLifeCycle()
                .subscribe(parentId)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh
        //
        parentId.compose<Contract> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRefresh.onNext(it)
                    requestPaymentPlan(it.paymentPlanCode)
                }

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

        requestCarrierDescription
                .flatMap { enviorment.carrierRepository.getCarrierName(it) }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRequestCarrierDescription)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun refresh(parameter: Parameter) = refresh.onNext(parameter)
    override fun requestPaymentPlan(paymentPlanCode: String) = requestPaymentPlan.onNext(paymentPlanCode)
    override fun requestCarrierDescription(carrierCode: String) = requestCarrierDescription.onNext(carrierCode)

    // interface : out
    override fun onSuccessRefresh(): Observable<Contract> = onSuccessRefresh
    override fun onSuccessRequestPaymentPlan(): Observable<Response<PaymentPlan>> = onSuccessRequestPaymentPlan
    override fun onSuccessRequestCarrierDescription(): Observable<String> = onSuccessRequestCarrierDescription
}

interface ConditionTableInputs {
    fun refresh(parameter: Parameter)
    fun requestPaymentPlan(paymentPlanCode: String)
    fun requestCarrierDescription(carrierCode: String)
}

interface ConditionTableOutputs {
    fun onSuccessRefresh(): Observable<Contract>
    fun onSuccessRequestPaymentPlan() : Observable<Response<PaymentPlan>>
    fun onSuccessRequestCarrierDescription(): Observable<String>
}