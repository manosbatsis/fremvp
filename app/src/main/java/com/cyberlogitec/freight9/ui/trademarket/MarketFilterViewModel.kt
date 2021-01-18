package com.cyberlogitec.freight9.ui.trademarket

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Carrier
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketFilterViewModel(context: Context) : BaseViewModel(context), FilterInputs, FilterOutputs {

    val inPuts: FilterInputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val clickToApply = PublishSubject.create<Parameter>()
    private val clickToCarrierApply = PublishSubject.create<List<Carrier>>()
    private val changeSelect = PublishSubject.create<Parameter>()



    val outPuts: FilterOutputs = this
    private val gotoMarket = PublishSubject.create<Parameter>()
    private val refreshApply = PublishSubject.create<Parameter>()
    private val refreshCarrierFilter = PublishSubject.create<List<Carrier>>()

    init {

        clickToApply.bindToLifeCycle()
                .subscribe(gotoMarket)
        changeSelect.bindToLifeCycle()
                .subscribe(refreshApply)

        refresh.flatMap{ enviorment.carrierRepository.getCarriersFromDb() }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (refreshCarrierFilter)

        clickToCarrierApply.bindToLifeCycle()
                .subscribe {
                    enviorment.carrierRepository.storeCarriersInDb(it)
                    gotoMarket.onNext(Parameter.CLICK)
                }
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)
        refresh.onNext(Parameter.EVENT)
    }

    override fun clickToApply(parameter: Parameter) {
        Timber.v("f9: clickCarrierFilterApply")
        return clickToApply.onNext(parameter)
    }
    override fun clickToCarrierApply(carrierList: List<Carrier>) {
        Timber.v("f9: clickToCarrierApply")
        return clickToCarrierApply.onNext(carrierList)
    }
    override fun changeSelect(parameter: Parameter) {
        Timber.v("f9: changeSelect")
        return changeSelect.onNext(parameter)
    }
    override fun refreshApply(): Observable<Parameter> {
        Timber.v("f9: refreshApply")
        return refreshApply
    }

    override fun gotoMarket(): Observable<Parameter> {
        Timber.v("f9: gotoMarket")
        return gotoMarket
    }

    override fun refreshCarrierFilter(): Observable<List<Carrier>> {
        Timber.v("f9: refreshCarrierFilter")
        return refreshCarrierFilter
    }

}

interface FilterInputs {
    fun clickToApply(parameter: Parameter)
    fun clickToCarrierApply(carrierList: List<Carrier>)
    fun changeSelect(parameter: Parameter)
}

interface FilterOutputs {
    fun gotoMarket() : Observable<Parameter>
    fun refreshApply() : Observable<Parameter>
    fun refreshCarrierFilter() : Observable<List<Carrier>>


}
