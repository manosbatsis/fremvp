package com.cyberlogitec.freight9.ui.booking

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.booking.BookingDashboard
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class BookingDashboardViewModel(context: Context) : BaseViewModel(context),
        BookingDashboardInputs, BookingDashboardOutputs  {

    val inPuts: BookingDashboardInputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val bookingDashboardList = PublishSubject.create<Parameter>()
    private val clickToFilter = PublishSubject.create<Int>()

    val outPuts: BookingDashboardOutputs = this
    private val gotoMenu = PublishSubject.create<Parameter>()
    private val onSuccessRequestBookingDashboardList = PublishSubject.create<BookingDashboard>()
    private val gotoFilter = PublishSubject.create<Int>()

    init {
        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        bookingDashboardList.flatMapMaybe {
            enviorment.apiBookingClient.getBookingDashboardList().doOnError {
            }.neverError()
    }
    .bindToLifeCycle()
                .subscribe {
                    it.body()?.let { it1 -> onSuccessRequestBookingDashboardList.onNext(it1) }
                }

        clickToFilter.bindToLifeCycle()
                .subscribe(gotoFilter)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)
        Timber.v("f9: onResume")
    }

    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
    override fun clickToFilter(scrollType: Int) = clickToFilter.onNext(scrollType)
    override fun requestBookingDashboardList(parameter: Parameter) = bookingDashboardList.onNext(parameter)

    override fun gotoMenu() : Observable<Parameter> = gotoMenu
    override fun onSuccessRequestBookingDashboardList(): Observable<BookingDashboard> = onSuccessRequestBookingDashboardList
    override fun gotoFilter(): Observable<Int> = gotoFilter
}

interface BookingDashboardInputs {
    fun clickToMenu(parameter: Parameter)
    fun clickToFilter(scrollType: Int)
    fun requestBookingDashboardList(parameter: Parameter)

}

interface BookingDashboardOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun gotoFilter() : Observable<Int>
    fun onSuccessRequestBookingDashboardList(): Observable<BookingDashboard>

}