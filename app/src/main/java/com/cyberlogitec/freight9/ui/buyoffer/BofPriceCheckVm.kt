package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofPriceCheckVm(context: Context) : BaseViewModel(context), PriceCheckInputs, PriceCheckOutputs  {
    val inPuts: PriceCheckInputs = this
    private val clickToEdit = PublishSubject.create<Parameter>()

    val outPuts: PriceCheckOutputs = this

    private val onClickEdit = PublishSubject.create<Offer>()
    private val onSuccessRefresh = PublishSubject.create<Offer>()

    // intents
    private val parentId = BehaviorSubject.create<Offer>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        intent.map { it.getSerializableExtra(Intents.OFFER) as Offer }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( parentId )

        parentId.compose<Offer> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        parentId.compose<Offer> { clickToEdit.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickEdit)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // inPuts
    override fun clickToEdit(parameter: Parameter) = clickToEdit.onNext(parameter)

    // outPuts
    override fun onClickEdit() : Observable<Offer> = onClickEdit
    override fun onSuccessRefresh(): Observable<Offer> = onSuccessRefresh
}

interface PriceCheckInputs {
    fun clickToEdit(parameter: Parameter)
}

interface PriceCheckOutputs {
    fun onClickEdit() : Observable<Offer>
    fun onSuccessRefresh(): Observable<Offer>
}