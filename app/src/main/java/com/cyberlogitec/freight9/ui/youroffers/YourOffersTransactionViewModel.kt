package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersTransactionViewModel(context: Context) : BaseViewModel(context),
        YourOffersTransactionInputs, YourOffersTransactionOutputs  {

    val inPuts: YourOffersTransactionInputs = this
    private val clickToClose = PublishSubject.create<Parameter>()

    val outPuts: YourOffersTransactionOutputs = this
    private val onClickToClose = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<String>()

    // intents
    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { it.getSerializableExtra(Intents.YOUR_OFFER_TYPE) as String }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .map { Timber.d("f9: yourOfferHistoryItem is $it"); it }
                .subscribe(onSuccessRefresh)

        clickToClose.bindToLifeCycle()
                .subscribe(onClickToClose)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToClose(parameter: Parameter) = clickToClose.onNext(parameter)

    // interface : out
    override fun onClickToClose() = onClickToClose
    override fun onSuccessRefresh(): Observable<String> = onSuccessRefresh
}

interface YourOffersTransactionInputs {
    fun clickToClose(parameter: Parameter)

}

interface YourOffersTransactionOutputs {
    fun onClickToClose(): Observable<Parameter>
    fun onSuccessRefresh(): Observable<String>
}