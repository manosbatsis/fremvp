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

class CargoTrackingFilterViewModel(context: Context) : BaseViewModel(context),
        CargoTrackingFilterInputs, CargoTrackingFilterOutputs  {

    val inPuts: CargoTrackingFilterInputs = this
    private val clickToClose = PublishSubject.create<Parameter>()
    private val clickToApply = PublishSubject.create<Parameter>()
    private val clickToSpinDialog = PublishSubject.create<Int>()
    private val clickToSpinCalendarDialog = PublishSubject.create<Int>()
    private val clickToDateType = PublishSubject.create<Int>()
    private val clickToDatePeriodType = PublishSubject.create<Int>()

    val outPuts: CargoTrackingFilterOutputs = this
    private val onClickToClose = PublishSubject.create<Parameter>()
    private val onClickToApply = PublishSubject.create<Parameter>()
    private val onClickToSpinDialog = PublishSubject.create<Int>()
    private val onClickToSpinCalendarDialog = PublishSubject.create<Int>()
    private val onClickToDateType = PublishSubject.create<Int>()
    private val onClickToDatePeriodType = PublishSubject.create<Int>()
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
                .subscribe(onSuccessRefresh)

        clickToClose.bindToLifeCycle()
                .subscribe(onClickToClose)

        clickToApply.bindToLifeCycle()
                .subscribe(onClickToApply)

        clickToSpinDialog.bindToLifeCycle()
                .subscribe(onClickToSpinDialog)

        clickToSpinCalendarDialog.bindToLifeCycle()
                .subscribe(onClickToSpinCalendarDialog)

        clickToDateType.bindToLifeCycle()
                .subscribe(onClickToDateType)

        clickToDatePeriodType.bindToLifeCycle()
                .subscribe(onClickToDatePeriodType)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToClose(parameter: Parameter) = clickToClose.onNext(parameter)
    override fun clickToApply(parameter: Parameter) = clickToApply.onNext(parameter)
    override fun clickToSpinDialog(spinType: Int) = clickToSpinDialog.onNext(spinType)
    override fun clickToSpinCalendarDialog(spinType: Int) = clickToSpinCalendarDialog.onNext(spinType)
    override fun clickToDateType(dateType: Int) = clickToDateType.onNext(dateType)
    override fun clickToDatePeriodType(datePeriodType: Int) = clickToDatePeriodType.onNext(datePeriodType)

    // interface : out
    override fun onClickToClose() = onClickToClose
    override fun onClickToApply() = onClickToApply
    override fun onClickToSpinDialog() = onClickToSpinDialog
    override fun onClickToSpinCalendarDialog() = onClickToSpinCalendarDialog
    override fun onClickToDateType() = onClickToDateType
    override fun onClickToDatePeriodType() = onClickToDatePeriodType
    override fun onSuccessRefresh(): Observable<Intent> = onSuccessRefresh
}

interface CargoTrackingFilterInputs {
    fun clickToClose(parameter: Parameter)
    fun clickToApply(parameter: Parameter)
    fun clickToSpinDialog(spinType: Int)
    fun clickToSpinCalendarDialog(spinType: Int)
    fun clickToDateType(dateType: Int)
    fun clickToDatePeriodType(datePeriodType: Int)
}

interface CargoTrackingFilterOutputs {
    fun onClickToClose(): Observable<Parameter>
    fun onClickToApply(): Observable<Parameter>
    fun onClickToSpinDialog(): Observable<Int>
    fun onClickToSpinCalendarDialog(): Observable<Int>
    fun onClickToDateType(): Observable<Int>
    fun onClickToDatePeriodType(): Observable<Int>
    fun onSuccessRefresh(): Observable<Intent>
}