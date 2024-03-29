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

class BofVolumeVm(context: Context) : BaseViewModel(context), VolumeInputs, VolumeOutputs  {

    // inputs
    val inPuts: VolumeInputs = this
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToNext = PublishSubject.create<Offer>()

    // outputs
    val outPuts: VolumeOutputs = this
    private val onClickDone = PublishSubject.create<Parameter>()
    private val onClickNext = PublishSubject.create<Offer>()
    private val onSuccessRefresh = PublishSubject.create<Offer>()

    // intents
    private val parentId = BehaviorSubject.create<Offer>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { it.getSerializableExtra(Intents.OFFER) as Offer }
                .filter{ listOf(it).isNotEmpty() }
                .map{
                    it.offerLineItems?.map{ it.isChecked = false}
                    it
                }
                .bindToLifeCycle()
                .subscribe( parentId )

        ///////////////////////////////////////////////////////////////////////////////////////////
        // refresh
        parentId.compose<Offer> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        ///////////////////////////////////////////////////////////////////////////////////////////
        // click

        clickToNext.bindToLifeCycle()
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
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun clickToNext(parameter: Offer) = clickToNext.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<Offer> = onSuccessRefresh
    override fun onClickDone() : Observable<Parameter> = onClickDone
    override fun onClickNext() : Observable<Offer> = onClickNext
}

interface VolumeInputs {
    fun clickToDone(parameter: Parameter)
    fun clickToNext(parameter: Offer)
}

interface VolumeOutputs {
    fun onClickNext(): Observable<Offer>
    fun onClickDone(): Observable<Parameter>
    fun onSuccessRefresh(): Observable<Offer>
}