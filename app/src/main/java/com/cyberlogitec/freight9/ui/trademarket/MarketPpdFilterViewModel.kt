package com.cyberlogitec.freight9.ui.trademarket

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Payment
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketPpdFilterViewModel(context: Context) : BaseViewModel(context), PpdFilterInputs, PpdFilterOutputs {

    val inPuts: PpdFilterInputs = this
    private val refreshType = PublishSubject.create<Parameter>()
    private val refreshPlan = PublishSubject.create<Parameter>()
    private val clickToApply = PublishSubject.create<List<Payment>>()


    val outPuts: PpdFilterOutputs = this
    private val gotoMarket = PublishSubject.create<Parameter>()
    private val refreshPaymentType = PublishSubject.create<List<Payment>>()
    private val refreshPaymentPlan = PublishSubject.create<List<Payment>>()

    init {

        clickToApply.bindToLifeCycle()
                .subscribe{
                    enviorment.paymentRepository.storePaymentsInDb(it)
                    gotoMarket.onNext(Parameter.CLICK)
                }

        refreshType.flatMap{ enviorment.paymentRepository.getPaymentsFromDb("type") }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (refreshPaymentType)
        refreshPlan.flatMap{ enviorment.paymentRepository.getPaymentsFromDb("plan") }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (refreshPaymentPlan)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)
        refreshType.onNext(Parameter.CLICK)
        refreshPlan.onNext(Parameter.CLICK)
    }

    override fun clickToApply(list: List<Payment>) {
        Timber.v("clickPaymentFilterApply")
        return clickToApply.onNext(list)
    }

    override fun gotoMarket(): Observable<Parameter> {
        Timber.v("gotoMarket")
        return gotoMarket
    }

    override fun refreshPaymentType(): Observable<List<Payment>> {
        Timber.v("refreshPaymentType")
        return refreshPaymentType
    }
    override fun refreshPaymentPlan(): Observable<List<Payment>> {
        Timber.v("refreshPaymnetPlan")
        return refreshPaymentPlan
    }

}

interface PpdFilterInputs {
    fun clickToApply(list: List<Payment>)
}

interface PpdFilterOutputs {
    fun gotoMarket() : Observable<Parameter>
    fun refreshPaymentType(): Observable<List<Payment>>
    fun refreshPaymentPlan(): Observable<List<Payment>>

}
