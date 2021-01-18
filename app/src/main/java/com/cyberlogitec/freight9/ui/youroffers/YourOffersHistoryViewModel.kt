package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.EventCode
import com.cyberlogitec.freight9.config.EventCode.EVENT_CODE_OFFER_CLOSED
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.DashboardOfferHistory
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.fromJson
import com.cyberlogitec.freight9.lib.util.toJson
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersHistoryViewModel(context: Context) : BaseViewModel(context),
        YourOffersHistoryInputs, YourOffersHistoryOutputs  {

    val inPuts: YourOffersHistoryInputs = this
    private val clickItem = PublishSubject.create<DashboardOfferHistory.EventCell>()
    private val requestOfferEvents = PublishSubject.create<Parameter>()

    val outPuts: YourOffersHistoryOutputs = this
    private val onSuccessRefresh = PublishSubject.create<List<DashboardOfferHistory.EventCell>>()

    // intents
    private val parentId = BehaviorSubject.create<Intent>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.bindToLifeCycle()
                .subscribe(parentId)

        /**
         * Get Offers week detail From Call Api
         */
        parentId.compose<Intent> { requestOfferEvents.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { intent ->
                    // intent : YOUR_OFFER_DASHBOARD_ITEM, YOUR_OFFER_TRADE_WRAPPER_INFO
                    val cell = intent.getSerializableExtra(Intents.YOUR_OFFER_DASHBOARD_ITEM) as Dashboard.Cell
                    enviorment.apiTradeClient.getDashboardOfferOfferEvents(cell.offerNumber)
                            .handleToError(hideLoadingDialog)
                            .neverError()
                }
                .map { hideLoadingDialog.onNext(Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            if (response.isSuccessful) {
                                val dashboardOfferHistory = response.body() as DashboardOfferHistory
                                dashboardOfferHistory.eventCell?.let { receivedEeventCells ->

                                    receivedEeventCells.map { it.allYn = dashboardOfferHistory.allYn }

                                    val eventCells = receivedEeventCells
                                            .sortedByDescending { it.lastEventTimestamp }

                                    // 가장 최근의 offerClosed event 의 lastEventTimestamp 를 찾는다
                                    val offerClosedEventTime = eventCells
                                            .find { it.eventCode == EVENT_CODE_OFFER_CLOSED }
                                            ?.lastEventTimestamp ?: EmptyString

                                    // 가장 최근의 offerClosed event 이후의 Event 들은 표시되지 않도록 한다
                                    val filteredEventCells = if (offerClosedEventTime.isNotEmpty()) {
                                        eventCells.filter { it.lastEventTimestamp <= offerClosedEventTime }
                                    } else {
                                        eventCells
                                    }

                                    // weekExpired event 인 경우에는 baseYearWeek 별로 EventCell 을 만들어서 add 한다
                                    val splitEventCells = mutableListOf<DashboardOfferHistory.EventCell>()
                                    for (eventCell in filteredEventCells) {
                                        with(eventCell) {
                                            when(eventCode) {
                                                EventCode.EVENT_CODE_WEEK_EXPIRED -> {
                                                    for (eventLog in eventLog.sortedBy { it.baseYearWeek }) {
                                                        val clonedEventCell = eventCell.toJson().fromJson<DashboardOfferHistory.EventCell>()
                                                        clonedEventCell?.let {
                                                            it.baseYearWeek = eventLog.baseYearWeek
                                                            it.eventLog.map {
                                                                it.isSelected = it.baseYearWeek == eventLog.baseYearWeek
                                                            }
                                                            splitEventCells.add(clonedEventCell)
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    splitEventCells.add(eventCell)
                                                }
                                            }
                                        }
                                    }
                                    onSuccessRefresh.onNext(splitEventCells)
                                }
                            } else {
                                error.onNext(Throwable(response.errorBody().toString()))
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        // intent : YOUR_OFFER_DASHBOARD_ITEM, YOUR_OFFER_TRADE_WRAPPER_INFO + YOUR_OFFER_HISTORY_ITEM
        parentId.compose<Intent> { clickItem.withLatestFrom(it, BiFunction { eventCell, intent ->
                    intent.putExtra(Intents.YOUR_OFFER_HISTORY_ITEM, eventCell) } )}
                .map { intent ->
                    Pair(ParameterAny.ANY_JUMP_TO_OTHERS, intent)
                }
                .bindToLifeCycle()
                .subscribe(onClickViewParameterAny)
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    // interface : in
    override fun clickItem(eventCell: DashboardOfferHistory.EventCell) = clickItem.onNext(eventCell)
    override fun requestOfferEvents(parameter: Parameter) = requestOfferEvents.onNext(parameter)

    // interface : out
    override fun onSuccessRefresh(): Observable<List<DashboardOfferHistory.EventCell>> = onSuccessRefresh
}

interface YourOffersHistoryInputs {
    fun clickItem(eventCell: DashboardOfferHistory.EventCell)
    fun requestOfferEvents(parameter: Parameter)
}

interface YourOffersHistoryOutputs {
    fun onSuccessRefresh(): Observable<List<DashboardOfferHistory.EventCell>>
}