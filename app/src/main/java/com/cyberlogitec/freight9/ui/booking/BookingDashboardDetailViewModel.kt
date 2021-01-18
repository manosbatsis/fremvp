package com.cyberlogitec.freight9.ui.booking

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BookingDashboardDetailViewModel(context: Context) : BaseViewModel(context),
        BookingDashboardDetailInputs, BookingDashboardDetailOutputs  {

    val inPuts: BookingDashboardDetailInputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val clickToMenu = PublishSubject.create<Parameter>()

    val outPuts: BookingDashboardDetailOutputs = this
    private val gotoMenu = PublishSubject.create<Parameter>()

    init {
        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
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

interface BookingDashboardDetailInputs {
    fun clickToMenu(parameter: Parameter)

}

interface BookingDashboardDetailOutputs {
    fun gotoMenu() : Observable<Parameter>

}