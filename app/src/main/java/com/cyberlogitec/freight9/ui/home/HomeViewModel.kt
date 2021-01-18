package com.cyberlogitec.freight9.ui.home

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class HomeViewModel(context: Context) : BaseViewModel(context), HomeInputs, HomeOutputs  {

    val inPuts: HomeInputs = this
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val swipeToLeft = PublishSubject.create<Int>()
    private val swipeToRight = PublishSubject.create<Int>()

    val outPuts: HomeOutputs = this
    private val gotoMenu = PublishSubject.create<Parameter>()
    private val gotoSellOrder = PublishSubject.create<Int>()
    private val gotoBuyOrder = PublishSubject.create<Int>()


    init {
        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        swipeToLeft.bindToLifeCycle()
                .subscribe(gotoSellOrder)

        swipeToRight.bindToLifeCycle()
                .subscribe(gotoBuyOrder)

    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        //refresh.onNext(Parameter.EVENT)
    }

    // override - in
    override fun clickToMenu(parameter: Parameter) {
        Timber.v("f9: clickToMenu(${parameter})")
        return clickToMenu.onNext(parameter)
    }

    override fun swipeToLeft(id: Int) {
        Timber.v("f9: swipeToLeft(${id})")
        return swipeToLeft.onNext(id)
    }

    override fun swipeToRight(id: Int) {
        Timber.v("f9: swipeToRight(${id})")
        return swipeToRight.onNext(id)
    }

    // override -out
    override fun gotoMenu() : Observable<Parameter> {
        Timber.v("f9: clickToMenu")
        return gotoMenu
    }

    override fun gotoSellOrder() : PublishSubject<Int> =  gotoSellOrder
    override fun gotoBuyOrder() : PublishSubject<Int> = gotoBuyOrder

}

interface HomeInputs {
    fun clickToMenu(parameter: Parameter)
    fun swipeToLeft(id: Int)
    fun swipeToRight(id: Int)
}

interface HomeOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun gotoSellOrder() : Observable<Int>
    fun gotoBuyOrder() : Observable<Int>
}