package com.cyberlogitec.freight9.ui.transaction

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class TransactionViewModel(context: Context) : BaseViewModel(context), TransactionInputs, TransactionOutputs  {
    val inPuts: TransactionInputs = this
    private val clickToMenu = PublishSubject.create<Parameter>()

    val outPuts: TransactionOutputs = this
    private val gotoMenu = PublishSubject.create<Parameter>()

    init {
        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)
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

}

interface TransactionInputs {
    fun clickToMenu(parameter: Parameter)
}

interface TransactionOutputs {
    fun gotoMenu() : Observable<Parameter>
}