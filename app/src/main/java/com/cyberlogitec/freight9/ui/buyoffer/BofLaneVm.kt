package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Schedule
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofLaneVm(context: Context) : BaseViewModel(context), LaneInputs, LaneOutputs {
    val inPuts: LaneInputs = this
    private val clickToItem = PublishSubject.create<Int>()
    private val clickToItemRemove = PublishSubject.create<String>()
    private val clickToNext = PublishSubject.create<List<Schedule>>()
    private val clickToSearch = PublishSubject.create<Parameter>()

    val outPuts: LaneOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<List<Schedule>>()
    private val onClickItem = PublishSubject.create<Int>()
    private val onClickItemRemove = PublishSubject.create<String>()
    private val onClickNext = PublishSubject.create<List<Schedule>>()
    private val onClickSearch = PublishSubject.create<Parameter>()


    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh

        refresh.bindToLifeCycle()
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { enviorment.scheduleRepository.loadAllSchedules().handleToError(hideLoadingDialog).neverError() }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        ////////////////////////////////////////////////////////////////////////////////////////////

        clickToItem.bindToLifeCycle()
                .subscribe(onClickItem)

        clickToItemRemove.bindToLifeCycle()
                .subscribe(onClickItemRemove)

        clickToNext.bindToLifeCycle()
                .subscribe(onClickNext)

        clickToSearch.bindToLifeCycle()
                .subscribe(onClickSearch)

        ////////////////////////////////////////////////////////////////////////////////////////////
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToItem(parameter: Int) = clickToItem.onNext(parameter)
    override fun clickToItemRemove(parameter: String) = clickToItemRemove.onNext(parameter)
    override fun clickToNext(parameter: List<Schedule>) = clickToNext.onNext(parameter)
    override fun clickToSearch(parameter: Parameter) = clickToSearch.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<List<Schedule>> = onSuccessRefresh
    override fun onClickItem(): Observable<Int> = onClickItem
    override fun onClickItemRemove(): Observable<String> = onClickItemRemove
    override fun onClickNext(): Observable<List<Schedule>> = onClickNext
    override fun onClickSearch(): Observable<Parameter> = onClickSearch
}

interface LaneInputs {
    fun clickToItem(parameter: Int)
    fun clickToItemRemove(parameter: String)
    fun clickToNext(parameter: List<Schedule>)
    fun clickToSearch(parameter: Parameter)
}

interface LaneOutputs {
    fun onSuccessRefresh(): Observable<List<Schedule>>
    fun onClickItem(): Observable<Int>
    fun onClickItemRemove(): Observable<String>
    fun onClickNext(): Observable<List<Schedule>>
    fun onClickSearch(): Observable<Parameter>
}