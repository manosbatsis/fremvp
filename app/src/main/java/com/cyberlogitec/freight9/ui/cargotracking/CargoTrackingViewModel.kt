package com.cyberlogitec.freight9.ui.cargotracking

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class CargoTrackingViewModel(context: Context) : BaseViewModel(context),
        CargoTrackingInputs, CargoTrackingOutputs {

    val inputs: CargoTrackingInputs = this

    val outputs: CargoTrackingOutputs = this
    private val onSuccessRefresh = PublishSubject.create<Parameter>()

    // intents
    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        refresh.bindToLifeCycle()
                .subscribe(onSuccessRefresh)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }


    // interface : out
    override fun onSuccessRefresh(): Observable<Parameter> = onSuccessRefresh
}

interface CargoTrackingInputs {

}

interface CargoTrackingOutputs {
    fun onSuccessRefresh(): Observable<Parameter>
}