package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterClick
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersHistoryDetailViewModel(context: Context) : BaseViewModel(context),
        YourOffersHistoryDetailInputs, YourOffersHistoryDetailOutputs  {

    val inPuts: YourOffersHistoryDetailInputs = this
    private val requestCarrierName = PublishSubject.create<String>()

    val outPuts: YourOffersHistoryDetailOutputs = this
    private val onSuccessRefresh = PublishSubject.create<Intent>()
    private val onSuccessRequestCarrierName = PublishSubject.create<String>()

    // intents
    private val parentId = BehaviorSubject.create<Intent>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<Intent> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .map { Timber.d("f9: yourOfferHistoryItem intent is $it"); it }
                .subscribe(onSuccessRefresh)

        requestCarrierName
                .flatMap { enviorment.carrierRepository.getCarrierName(it) }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRequestCarrierName)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun requestCarrierName(carrierCode: String) = requestCarrierName.onNext(carrierCode)

    // interface : out
    override fun onSuccessRefresh(): Observable<Intent> = onSuccessRefresh
    override fun onSuccessRequestCarrierName(): Observable<String> = onSuccessRequestCarrierName
}

interface YourOffersHistoryDetailInputs {
    fun requestCarrierName(carrierCode: String)
}

interface YourOffersHistoryDetailOutputs {
    fun onSuccessRefresh(): Observable<Intent>
    fun onSuccessRequestCarrierName(): Observable<String>
}