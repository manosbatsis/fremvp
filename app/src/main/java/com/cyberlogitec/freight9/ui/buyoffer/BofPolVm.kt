package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofPolVm(context: Context) : BaseViewModel(context), PolInputs, PolOutputs {
    val inPuts: PolInputs = this
    private val clickToItemCheck = PublishSubject.create<Int>()
    private val clickToItemRemove = PublishSubject.create<Schedule>()
    private val clickToNext = PublishSubject.create<List<Schedule>>()
    private val clickToSearch = PublishSubject.create<Parameter>()

    val outPuts: PolOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val parentId = BehaviorSubject.create<List<Schedule>>()

    private val onSuccessRefresh = PublishSubject.create<List<Schedule>>()
    private val onClickItemCheck = PublishSubject.create<Int>()
    private val onClickItemRemove = PublishSubject.create<Schedule>()
    private val onClickNext = PublishSubject.create<List<Schedule>>()
    private val onClickSearch = PublishSubject.create<Parameter>()


    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent (parentId)

        intent.map { it.getSerializableExtra(Intents.SCHEDULE_LIST) as List<Schedule> }
                .filter{ it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh (onSuccessRefresh)

        parentId.compose<List<Schedule>> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 })  }
                .filter { it.isNotEmpty() }
                .map {
                    it.map { it.isPolChecked = false }
                    it
                }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        ////////////////////////////////////////////////////////////////////////////////////////////

        clickToItemCheck.bindToLifeCycle()
                .subscribe(onClickItemCheck)

        clickToItemRemove.bindToLifeCycle()
                .subscribe(onClickItemRemove)

        clickToNext.bindToLifeCycle()
                .subscribe(onClickNext)

        clickToSearch.bindToLifeCycle()
                .subscribe(onClickSearch)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToItemCheck(parameter: Int) = clickToItemCheck.onNext(parameter)
    override fun clickToItemRemove(parameter: Schedule) = clickToItemRemove.onNext(parameter)
    override fun clickToNext(parameter: List<Schedule>) = clickToNext.onNext(parameter)
    override fun clickToSearch(parameter: Parameter) = clickToSearch.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<List<Schedule>> = onSuccessRefresh
    override fun onClickItemCheck(): Observable<Int> = onClickItemCheck
    override fun onClickItemRemove(): Observable<Schedule> = onClickItemRemove
    override fun onClickNext(): Observable<List<Schedule>> = onClickNext
    override fun onClickSearch(): Observable<Parameter> = onClickSearch
}

interface PolInputs {
    fun clickToItemCheck(parameter: Int)
    fun clickToItemRemove(parameter: Schedule)
    fun clickToNext(parameter: List<Schedule>)
    fun clickToSearch(parameter: Parameter)
}

interface PolOutputs {
    fun onSuccessRefresh(): Observable<List<Schedule>>
    fun onClickItemCheck(): Observable<Int>
    fun onClickItemRemove(): Observable<Schedule>
    fun onClickNext(): Observable<List<Schedule>>
    fun onClickSearch(): Observable<Parameter>
}