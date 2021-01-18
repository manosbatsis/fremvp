package com.cyberlogitec.freight9.ui.finance

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

class FinanceFilterViewModel(context: Context) : BaseViewModel(context),
        FinanceFilterInputs, FinanceFilterOutputs  {

    val inPuts: FinanceFilterInputs = this
    private val clickToClose = PublishSubject.create<Parameter>()
    private val clickToApply = PublishSubject.create<Parameter>()
    private val clickToSpinDialog = PublishSubject.create<Int>()
    private val clickToSpinCalendarDialog = PublishSubject.create<Int>()
    private val clickToDateType = PublishSubject.create<Int>()
    private val clickToDatePeriodType = PublishSubject.create<Int>()

    private val clickToSwitch = PublishSubject.create<String>()
    private val clickToStatusClearSelectAll = PublishSubject.create<Parameter>()
    private val clickToTypeClearSelectAll = PublishSubject.create<Parameter>()
    private val clickToCasesClearSelectAll = PublishSubject.create<Parameter>()
    private val clickToInvoiceTypeClearSelectAll = PublishSubject.create<Parameter>()
    private val clickToCollectPlanClearSelectAll = PublishSubject.create<Parameter>()

    val outPuts: FinanceFilterOutputs = this
    private val onClickToClose = PublishSubject.create<Parameter>()
    private val onClickToApply = PublishSubject.create<Parameter>()
    private val onClickToSpinDialog = PublishSubject.create<Int>()
    private val onClickToSpinCalendarDialog = PublishSubject.create<Int>()
    private val onClickToDateType = PublishSubject.create<Int>()
    private val onClickToDatePeriodType = PublishSubject.create<Int>()
    private val onSuccessRefresh = PublishSubject.create<Intent>()

    private val onClickToSwitch = PublishSubject.create<String>()
    private val onClickToStatusClearSelectAll = PublishSubject.create<Parameter>()
    private val onClickToTypeClearSelectAll = PublishSubject.create<Parameter>()
    private val onClickToCasesClearSelectAll = PublishSubject.create<Parameter>()
    private val onClickToInvoiceTypeClearSelectAll = PublishSubject.create<Parameter>()
    private val onClickToCollectPlanClearSelectAll = PublishSubject.create<Parameter>()

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

        clickToSwitch.bindToLifeCycle()
                .subscribe(onClickToSwitch)

        clickToStatusClearSelectAll.bindToLifeCycle()
                .subscribe(onClickToStatusClearSelectAll)

        clickToTypeClearSelectAll.bindToLifeCycle()
                .subscribe(onClickToTypeClearSelectAll)

        clickToCasesClearSelectAll.bindToLifeCycle()
                .subscribe(onClickToCasesClearSelectAll)

        clickToInvoiceTypeClearSelectAll.bindToLifeCycle()
                .subscribe(onClickToInvoiceTypeClearSelectAll)

        clickToCollectPlanClearSelectAll.bindToLifeCycle()
                .subscribe(onClickToCollectPlanClearSelectAll)
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

    override fun clickToSwitch(type: String) = clickToSwitch.onNext(type)
    override fun clickToStatusClearSelectAll(parameter: Parameter) = clickToStatusClearSelectAll.onNext(parameter)
    override fun clickToTypeClearSelectAll(parameter: Parameter) = clickToTypeClearSelectAll.onNext(parameter)
    override fun clickToCasesClearSelectAll(parameter: Parameter) = clickToCasesClearSelectAll.onNext(parameter)
    override fun clickToInvoiceTypeClearSelectAll(parameter: Parameter) = clickToInvoiceTypeClearSelectAll.onNext(parameter)
    override fun clickToCollectPlanClearSelectAll(parameter: Parameter) = clickToCollectPlanClearSelectAll.onNext(parameter)

    // interface : out
    override fun onClickToClose() = onClickToClose
    override fun onClickToApply() = onClickToApply
    override fun onClickToSpinDialog() = onClickToSpinDialog
    override fun onClickToSpinCalendarDialog() = onClickToSpinCalendarDialog
    override fun onClickToDateType() = onClickToDateType
    override fun onClickToDatePeriodType() = onClickToDatePeriodType
    override fun onSuccessRefresh(): Observable<Intent> = onSuccessRefresh

    override fun onClickToSwitch() = onClickToSwitch
    override fun onClickToStatusClearSelectAll() = onClickToStatusClearSelectAll
    override fun onClickToTypeClearSelectAll() = onClickToTypeClearSelectAll
    override fun onClickToCasesClearSelectAll() = onClickToCasesClearSelectAll
    override fun onClickToInvoiceTypeClearSelectAll() = onClickToInvoiceTypeClearSelectAll
    override fun onClickToCollectPlanClearSelectAll() = onClickToCollectPlanClearSelectAll
}

interface FinanceFilterInputs {
    fun clickToClose(parameter: Parameter)
    fun clickToApply(parameter: Parameter)
    fun clickToSpinDialog(spinType: Int)
    fun clickToSpinCalendarDialog(spinType: Int)
    fun clickToDateType(dateType: Int)
    fun clickToDatePeriodType(datePeriodType: Int)

    fun clickToSwitch(type: String)
    fun clickToStatusClearSelectAll(parameter: Parameter)
    fun clickToTypeClearSelectAll(parameter: Parameter)
    fun clickToCasesClearSelectAll(parameter: Parameter)
    fun clickToInvoiceTypeClearSelectAll(parameter: Parameter)
    fun clickToCollectPlanClearSelectAll(parameter: Parameter)
}

interface FinanceFilterOutputs {
    fun onClickToClose(): Observable<Parameter>
    fun onClickToApply(): Observable<Parameter>
    fun onClickToSpinDialog(): Observable<Int>
    fun onClickToSpinCalendarDialog(): Observable<Int>
    fun onClickToDateType(): Observable<Int>
    fun onClickToDatePeriodType(): Observable<Int>
    fun onSuccessRefresh(): Observable<Intent>

    fun onClickToSwitch(): Observable<String>
    fun onClickToStatusClearSelectAll(): Observable<Parameter>
    fun onClickToTypeClearSelectAll(): Observable<Parameter>
    fun onClickToCasesClearSelectAll(): Observable<Parameter>
    fun onClickToInvoiceTypeClearSelectAll(): Observable<Parameter>
    fun onClickToCollectPlanClearSelectAll(): Observable<Parameter>
}