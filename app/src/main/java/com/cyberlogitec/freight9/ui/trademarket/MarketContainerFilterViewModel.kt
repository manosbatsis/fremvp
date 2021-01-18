package com.cyberlogitec.freight9.ui.trademarket

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Container
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketContainerFilterViewModel(context: Context) : BaseViewModel(context), ContainerFilterInputs, ContainerFilterOutputs {

    val inPuts: ContainerFilterInputs = this
    private val refreshRdterm = PublishSubject.create<Parameter>()
    private val refreshType = PublishSubject.create<Parameter>()
    private val refreshSize = PublishSubject.create<Parameter>()
    private val clickToApply = PublishSubject.create<List<Container>>()


    val outPuts: ContainerFilterOutputs = this
    private val gotoMarket = PublishSubject.create<Parameter>()
    private val refreshContainerRdterm = PublishSubject.create<List<Container>>()
    private val refreshContainerType = PublishSubject.create<List<Container>>()
    private val refreshContainerSize = PublishSubject.create<List<Container>>()

    init {

        clickToApply.bindToLifeCycle()
                .subscribe {
                    enviorment.containerRepository.storeContainersInDb(it)
                    gotoMarket.onNext(Parameter.CLICK)
                }

        refreshRdterm.flatMap{ enviorment.containerRepository.getContainersFromDb("rdterm") }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (refreshContainerRdterm)
        refreshType.flatMap{ enviorment.containerRepository.getContainersFromDb("type") }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (refreshContainerType)
        refreshSize.flatMap{ enviorment.containerRepository.getContainersFromDb("size") }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (refreshContainerSize)



    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)
        refreshRdterm.onNext(Parameter.CLICK)
        refreshType.onNext(Parameter.CLICK)
        refreshSize.onNext(Parameter.CLICK)
    }

    override fun clickToApply(list:List<Container>) {
        Timber.v("clickCarrierFilterApply")
        return clickToApply.onNext(list)
    }

    override fun gotoMarket(): Observable<Parameter> {
        Timber.v("gotoMarket")
        return gotoMarket
    }

    override fun refreshContainerRdterm(): Observable<List<Container>> {
        Timber.v("refreshRdterm")
        return refreshContainerRdterm
    }

    override fun refreshContainerType(): Observable<List<Container>> {
        Timber.v("refreshContainerType")
        return refreshContainerType
    }
    override fun refreshContainerSize(): Observable<List<Container>> {
        Timber.v("refreshContainerSize")
        return refreshContainerSize
    }

}

interface ContainerFilterInputs {
    fun clickToApply(list: List<Container>)
}

interface ContainerFilterOutputs {
    fun gotoMarket() : Observable<Parameter>
    fun refreshContainerRdterm() : Observable<List<Container>>
    fun refreshContainerType() : Observable<List<Container>>
    fun refreshContainerSize() : Observable<List<Container>>

}
