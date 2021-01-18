package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.OfferCarrier
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofCarrierVm(context: Context) : BaseViewModel(context), CarrierInputs, CarrierOutputs  {

    val inPuts: CarrierInputs = this
    private val clickToNext = PublishSubject.create<List<OfferCarrier>>()
    private val changeSelect = PublishSubject.create<Parameter>()

    val outPuts: CarrierOutputs = this
    private val onClickNext = PublishSubject.create<List<OfferCarrier>>()
    private val onSuccessRefresh = PublishSubject.create<List<OfferCarrier>>()
    private val refreshApply = PublishSubject.create<Parameter>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        val parentId = BehaviorSubject.create<List<OfferCarrier>>()

        intent.map { it.getSerializableExtra(Intents.CARRIER_LIST) as List<OfferCarrier> }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<List<OfferCarrier>> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ it.isNotEmpty() }
                .subscribe(onSuccessRefresh)

        clickToNext.bindToLifeCycle()
                .subscribe(onClickNext)

        changeSelect.bindToLifeCycle()
                .subscribe(refreshApply)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // inPuts
    override fun clickToNext(parameter: List<OfferCarrier>) = clickToNext.onNext(parameter)
    override fun changeSelect(parameter: Parameter) = changeSelect.onNext(parameter)

    // outPuts
    override fun onSuccessRefresh() : Observable<List<OfferCarrier>> = onSuccessRefresh
    override fun onClickNext() : Observable<List<OfferCarrier>> = onClickNext
    override fun refreshApply(): Observable<Parameter> = refreshApply
}

interface CarrierInputs {
    fun clickToNext(parameter: List<OfferCarrier>)
    fun changeSelect(parameter: Parameter)
}

interface CarrierOutputs {
    fun onSuccessRefresh(): Observable<List<OfferCarrier>>
    fun onClickNext() : Observable<List<OfferCarrier>>
    fun refreshApply() : Observable<Parameter>
}