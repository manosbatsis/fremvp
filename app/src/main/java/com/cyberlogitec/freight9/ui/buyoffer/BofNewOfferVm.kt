package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BofNewOfferVm(context: Context) : BaseViewModel(context), BofNewOfferInputs, BofNewOfferOutputs  {
    val inPuts: BofNewOfferInputs = this
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val clickToCreate = PublishSubject.create<Parameter>()

    val outPuts: BofNewOfferOutputs = this
    private val gotoMenu = PublishSubject.create<Parameter>()
    private val gotoCreate = PublishSubject.create<Parameter>()

    init {
        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        clickToCreate.bindToLifeCycle()
                .subscribe(gotoCreate)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        //refresh.onNext(Parameter.EVENT)
    }

    override fun clickToMenu(parameter: Parameter) {
        Timber.v("f9: clickToMenu(${parameter})")
        return clickToMenu.onNext(parameter)
    }

    override fun gotoMenu() : Observable<Parameter> {
        Timber.v("f9: clickToMenu")
        return gotoMenu
    }

    override fun clickToCreate(parameter: Parameter) {
        Timber.v("f9: clickToMenu(${parameter})")
        return clickToCreate.onNext(parameter)
    }

    override fun gotoCreate() : Observable<Parameter> {
        Timber.v("f9: clickToMenu")
        return gotoCreate
    }

}

interface BofNewOfferInputs {
    fun clickToMenu(parameter: Parameter)
    fun clickToCreate(parameter: Parameter)
}

interface BofNewOfferOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun gotoCreate() : Observable<Parameter>
}