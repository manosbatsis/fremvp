package com.cyberlogitec.freight9.ui.inventory

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class InventoryDetailViewModel(context: Context) : BaseViewModel(context), InventoryDetailInputs, InventoryDetailOutputs  {
    val inPuts: InventoryDetailInputs = this
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val clickItem = PublishSubject.create<InventoryActivity.InventoryChange>()

    val outPuts: InventoryDetailOutputs = this
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

    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
    override fun gotoMenu() : Observable<Parameter> = gotoMenu
}

interface InventoryDetailInputs {
    fun clickToMenu(parameter: Parameter)
}

interface InventoryDetailOutputs {
    fun gotoMenu() : Observable<Parameter>
}