package com.cyberlogitec.freight9.ui.routefilter.pol


import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Port
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber


class RouteFilterPolVm(context: Context) : BaseViewModel(context), RouteFilterPolInputs, RouteFilterPolOutputs {
    val inPuts: RouteFilterPolInputs = this
    private val clickToItemChecked = PublishSubject.create<Port>()
    private val clickToTitlebar = PublishSubject.create<Parameter>()

    val outPuts: RouteFilterPolOutputs = this
    private val onSuccessRefresh = PublishSubject.create<List<Port>>()
    private val onClickItemChecked = PublishSubject.create<Port>()
    private val onClickTitlebar = PublishSubject.create<Parameter>()

    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh
        //
        refresh.map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { enviorment.portRepository.loadAllPorts().handleToError(hideLoadingDialog).neverError() }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .filter { it.isNotEmpty() }
                .map{ listOf(Port(
                        portCode = "ALL",
                        portName = "",
                        continentName = "",
                        countryCode = "",
                        subContinentCode = "",
                        subContinentName = "",
                        regionCode = "",
                        regionName = "",
                        isInland = "",
                        countryName = "",
                        id = null,
                        lastSelected = null,
                        dataOwnrPtrId = "",
                        continentCode = "") ) + it }
                .bindToLifeCycle()
                .subscribe( onSuccessRefresh )

        ////////////////////////////////////////////////////////////////////////////////////////////
        // clickToItemChecked
        //
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
    override fun clickToItemChecked(parameter: Port) = clickToItemChecked.onNext(parameter)
    override fun clickToTitlebar(parameter: Parameter) = clickToTitlebar.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<List<Port>> = onSuccessRefresh
    override fun onClickItemChecked(): Observable<Port> = onClickItemChecked
    override fun onClickTitlebar(): Observable<Parameter> = onClickTitlebar
}

interface RouteFilterPolInputs {
    fun clickToItemChecked(parameter: Port)
    fun clickToTitlebar(parameter: Parameter)
}

interface RouteFilterPolOutputs {
    fun onSuccessRefresh(): Observable<List<Port>>
    fun onClickItemChecked(): Observable<Port>
    fun onClickTitlebar(): Observable<Parameter>
}