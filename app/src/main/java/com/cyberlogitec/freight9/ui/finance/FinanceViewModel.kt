package com.cyberlogitec.freight9.ui.finance

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class FinanceViewModel(context: Context) : BaseViewModel(context),
        FinanceInputs, FinanceOutputs {

    val inputs: FinanceInputs = this
    private val injectIntent = PublishSubject.create<Intent>()

    val outputs: FinanceOutputs = this
    private val onSuccessRefresh = PublishSubject.create<String>()

    // intents
    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { it.getSerializableExtra(Intents.FINANCE_TYPE) as String }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .map { Timber.d("f9: financeType is $it"); it }
                .subscribe(onSuccessRefresh)

        // Pay/Collect Plan, Transaction Statement or Invoice
        injectIntent.bindToLifeCycle()
                .subscribe(intent)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun injectIntent(intent: Intent) = injectIntent.onNext(intent)

    // interface : out
    override fun onSuccessRefresh(): Observable<String> = onSuccessRefresh
}

interface FinanceInputs {
    fun injectIntent(intent: Intent)
}

interface FinanceOutputs {
    fun onSuccessRefresh(): Observable<String>
}