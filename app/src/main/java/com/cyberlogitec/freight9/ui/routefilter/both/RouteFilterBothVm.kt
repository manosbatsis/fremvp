package com.cyberlogitec.freight9.ui.routefilter.both

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.db.PortDao
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber


class RouteFilterBothVm(context: Context) : BaseViewModel(context), RouteFilterBothInputs, RouteFilterBothOutputs {
    val inPuts: RouteFilterBothInputs = this
    private val clickToItemChecked = PublishSubject.create<PortDao.RouteMinimal>()
    private val clickToTitlebar = PublishSubject.create<Parameter>()

    val outPuts: RouteFilterBothOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<List<PortDao.RouteMinimal>>()
    private val onClickItemChecked = PublishSubject.create<PortDao.RouteMinimal>()
    private val onClickTitlebar = PublishSubject.create<Parameter>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh
        //
        refresh.map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { enviorment.portRepository.loadTestRoute().handleToError(hideLoadingDialog).neverError() }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { it.isNotEmpty() }
                .map{ listOf(PortDao.RouteMinimal("ALL", "Port or City", "ALL", "Port or City")) + it }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToItemChecked
        clickToItemChecked.bindToLifeCycle()
                .subscribe(onClickItemChecked)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToItemChecked
        //
        clickToTitlebar.bindToLifeCycle()
                .subscribe(onClickTitlebar)

    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun clickToItemChecked(parameter: PortDao.RouteMinimal) = clickToItemChecked.onNext(parameter)
    override fun clickToTitlebar(parameter: Parameter) = clickToTitlebar.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<List<PortDao.RouteMinimal>> = onSuccessRefresh
    override fun onClickItemChecked(): Observable<PortDao.RouteMinimal> = onClickItemChecked
    override fun onClickTitlebar(): Observable<Parameter> = onClickTitlebar
}

interface RouteFilterBothInputs {
    fun clickToItemChecked(parameter: PortDao.RouteMinimal)
    fun clickToTitlebar(parameter: Parameter)
}

interface RouteFilterBothOutputs {
    fun onSuccessRefresh(): Observable<List<PortDao.RouteMinimal>>
    fun onClickItemChecked(): Observable<PortDao.RouteMinimal>
    fun onClickTitlebar(): Observable<Parameter>
}