package com.cyberlogitec.freight9.ui.cargotracking

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

class CargoTrackingDetailViewModel(context: Context) : BaseViewModel(context),
        CargoTrackingDetailInputs, CargoTrackingDetailOutputs {

    val inputs: CargoTrackingDetailInputs = this

    val outputs: CargoTrackingDetailOutputs = this
    private val onSuccessRefresh = PublishSubject.create<Intent>()

    // intents
    private val parentId = BehaviorSubject.create<Intent>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        intent.filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<Intent> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .map { Timber.d("f9: cargoTracking intent is $it"); it }
                .subscribe(onSuccessRefresh)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : out
    override fun onSuccessRefresh(): Observable<Intent> = onSuccessRefresh
}

interface CargoTrackingDetailInputs {

}

interface CargoTrackingDetailOutputs {
    fun onSuccessRefresh(): Observable<Intent>
}