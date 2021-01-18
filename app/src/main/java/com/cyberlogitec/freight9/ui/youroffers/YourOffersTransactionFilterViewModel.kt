package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersTransactionFilterViewModel(context: Context) : BaseViewModel(context),
        YourOffersTransactionFilterInputs, YourOffersTransactionFilterOutputs  {

    val inPuts: YourOffersTransactionFilterInputs = this
    private val clickToClose = PublishSubject.create<Parameter>()
    private val clickToApply = PublishSubject.create<Parameter>()

    val outPuts: YourOffersTransactionFilterOutputs = this
    private val onClickToClose = PublishSubject.create<Parameter>()
    private val onClickToApply = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<Intent>()

    // intents
    private val parentId = BehaviorSubject.create<Intent>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<Intent> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .map { Timber.d("f9: yourOfferHistoryItem is $it"); it }
                .subscribe(onSuccessRefresh)

        clickToClose.bindToLifeCycle()
                .subscribe(onClickToClose)

        clickToApply.bindToLifeCycle()
                .subscribe(onClickToApply)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToClose(parameter: Parameter) = clickToClose.onNext(parameter)
    override fun clickToApply(parameter: Parameter) = clickToApply.onNext(parameter)

    // interface : out
    override fun onClickToClose() = onClickToClose
    override fun onClickToApply() = onClickToApply
    override fun onSuccessRefresh(): Observable<Intent> = onSuccessRefresh
}

interface YourOffersTransactionFilterInputs {
    fun clickToClose(parameter: Parameter)
    fun clickToApply(parameter: Parameter)
}

interface YourOffersTransactionFilterOutputs {
    fun onClickToClose(): Observable<Parameter>
    fun onClickToApply(): Observable<Parameter>
    fun onSuccessRefresh(): Observable<Intent>
}