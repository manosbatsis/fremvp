package com.cyberlogitec.freight9.ui.buyoffer

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Offer
import com.cyberlogitec.freight9.lib.model.OfferLineItem
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.getThisYearNumber
import com.cyberlogitec.freight9.lib.util.getTodayWeekNumber
import com.cyberlogitec.freight9.lib.util.getYearWeeks
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*

class BofRouteVm(context: Context) : BaseViewModel(context), RouteInputs, RouteOutputs {
    val inPuts: RouteInputs = this
    private val clickToEdit = PublishSubject.create<Parameter>()
    private val clickToDone = PublishSubject.create<Parameter>()
    private val clickToNext = PublishSubject.create<Parameter>()

    val outPuts: RouteOutputs = this
    private val refresh = PublishSubject.create<Parameter>()
    private val onSuccessRefresh = PublishSubject.create<Offer>()
    private val onClickNext = PublishSubject.create<Offer>()

    private val onClickEdit = PublishSubject.create<Offer>()
    private val onClickDone = PublishSubject.create<String>()

    init {

        val parentId = BehaviorSubject.create<Offer>()

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { it.getSerializableExtra(Intents.OFFER) as Offer }
                .filter{ listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // refresh
        parentId.compose<Offer> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { offer2 ->
                    offer2.offerLineItems = mutableListOf<OfferLineItem>()
                    offer2
                }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // click
        parentId.compose<Offer> { clickToNext.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter{ listOf(it).isNotEmpty() }
                .map { offer2 ->
                    val thisYearN = getThisYearNumber()
                    val thisWeekN = getTodayWeekNumber()
                    val thisMaxWeekN = Calendar.getInstance().getYearWeeks(thisYearN)
                    val offerLineItems = mutableListOf<OfferLineItem>()

                    // this year weeks
                    for (i in thisWeekN .. thisMaxWeekN) {
                        val baseYearWeek = String.format("%4d%02d", thisYearN, i)
                        offerLineItems.add(OfferLineItem(baseYearWeek = baseYearWeek))
                    }

                    // next year weeks
                    for (j in 1 .. thisWeekN - 1) {
                        val baseYearWeek = String.format("%4d%02d", thisYearN + 1, j)
                        offerLineItems.add(OfferLineItem(baseYearWeek = baseYearWeek))
                    }

                    offer2.offerLineItems = offerLineItems
                    offer2
                }
                .bindToLifeCycle()
                .subscribe(onClickNext)

        parentId.compose<Offer> { clickToEdit.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .bindToLifeCycle()
                .subscribe(onClickEdit)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface: in
    override fun clickToEdit(parameter: Parameter) = clickToEdit.onNext(parameter)
    override fun clickToDone(parameter: Parameter) = clickToDone.onNext(parameter)
    override fun clickToNext(parameter: Parameter) = clickToNext.onNext(parameter)

    // interface: out
    override fun onSuccessRefresh(): Observable<Offer> = onSuccessRefresh
    override fun onClickNext() : Observable<Offer> = onClickNext

    override fun onClickEdit() : Observable<Offer> = onClickEdit
    override fun onClickDone() : Observable<String> = onClickDone
}

interface RouteInputs {
    fun clickToEdit(parameter: Parameter)
    fun clickToDone(parameter: Parameter)
    fun clickToNext(parameter: Parameter)
}

interface RouteOutputs {
    fun onClickEdit() : Observable<Offer>
    fun onClickDone() : Observable<String>
    fun onClickNext() : Observable<Offer>

    fun onSuccessRefresh(): Observable<Offer>
}