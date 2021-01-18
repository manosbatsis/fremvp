package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseOfferViewModel
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.makeRouteDataList
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersDetailViewModel(context: Context) : BaseOfferViewModel(context),
        YourOffersDetailInputs, YourOffersDetailOutputs  {

    val inPuts: YourOffersDetailInputs = this
    private val clickViewOfferHistory = PublishSubject.create<TradeOfferWrapper>()
    private val requestOfferInfoDetails = PublishSubject.create<Pair<String, Long>>()
    private val requestCarrierName = PublishSubject.create<String>()
    private val callRequestOfferInfoDetails = PublishSubject.create<Parameter>()

    val outPuts: YourOffersDetailOutputs = this
    private val onClickViewOfferHistory = PublishSubject.create<Intent>()

    private val onSuccessRefresh = PublishSubject.create<Dashboard.Cell>()
    private val onSuccessRequestOfferInfoDetails = PublishSubject.create<Any>()
    private val onSuccessRequestCarrierName = PublishSubject.create<String>()
    private val onSuccessRouteDataList = PublishSubject.create<RouteDataList>()

    private val parentId = BehaviorSubject.create<Intent>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<Intent> { callRequestOfferInfoDetails.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map { intent  ->
                    val cell = intent.getSerializableExtra(Intents.YOUR_OFFER_DASHBOARD_ITEM) as Dashboard.Cell
                    if (intent.hasExtra(Intents.YOUR_OFFER_TRADE_WRAPPER_INFO)) {
                        // YourOffersSwipeActivity 에서 intent 받은 경우
                        val tradeOfferDetail = intent.getSerializableExtra(Intents.YOUR_OFFER_TRADE_WRAPPER_INFO) as TradeOfferWrapper
                        onSuccessRequestOfferInfoDetails.onNext(tradeOfferDetail)
                        //--------------------------------------------------------------------------
                        // Whole Route > Grid 에 사용될 Route Data 추출
                        //--------------------------------------------------------------------------
                        onSuccessRouteDataList.onNext(makeRouteDataList(tradeOfferDetail.orderTradeOfferDetail.offerRoutes))
                    } else {
                        // YourOffersActivity 에서 intent 받은 경우 api 호출해서 tradeOfferDetail 요청
                        requestOfferInfoDetails.onNext(Pair(cell.offerNumber, cell.offerChangeSeq.toLong()))
                    }
                    cell
                }
                .map { cell ->
                    hideLoadingDialog.onNext(Throwable("OK"))
                    cell
                }
                .bindToLifeCycle()
                .subscribe(onSuccessRefresh)

        /**
         * UI Card 의 link 에서 사용 (ex. condition detail, whole route, price table...)
         */
        requestOfferInfoDetails
                .flatMapMaybe { pair ->
                    val offerNumber = pair.first
                    val offerChangeSeq = pair.second
                    enviorment.apiTradeClient.getTradeOfferDetailTarget(offerNumber, offerChangeSeq)
                            .handleToError(hideLoadingDialog)
                            .toMaybe()
                }
                .map {
                    if (it.isSuccessful) {
                        //--------------------------------------------------------------------------
                        // Whole Route > Grid 에 사용될 Route Data 추출
                        //--------------------------------------------------------------------------
                        onSuccessRouteDataList.onNext(makeRouteDataList((it.body() as OrderTradeOfferDetail).offerRoutes))
                    }
                    it
                }
                .map { hideLoadingDialog.onNext(Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe(
                        { response ->
                            onSuccessRequestOfferInfoDetails.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        requestCarrierName
                .flatMap { enviorment.carrierRepository.getCarrierName(it) }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(onSuccessRequestCarrierName)

        parentId.compose<Intent> { clickViewOfferHistory.withLatestFrom(it,
                BiFunction { tradeOfferWrapper, intent ->
                    if (intent.hasExtra(Intents.YOUR_OFFER_TRADE_WRAPPER_INFO)) {
                        // YourOffersSwipeActivity 에서 intent 받은 경우
                        intent
                    } else {
                        // YourOffersActivity 에서 intent 받은 경우
                        intent.putExtra(Intents.YOUR_OFFER_TRADE_WRAPPER_INFO, tradeOfferWrapper)
                    }
                })}
                .bindToLifeCycle()
                .subscribe(onClickViewOfferHistory)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    override fun clickViewOfferHistory(tradeOfferWrapper: TradeOfferWrapper) = clickViewOfferHistory.onNext(tradeOfferWrapper)
    override fun requestOfferInfoDetails(pair: Pair<String, Long>) = requestOfferInfoDetails.onNext(pair)
    override fun requestCarrierName(carrierCode: String) = requestCarrierName.onNext(carrierCode)
    override fun callRequestOfferInfoDetails(parameter: Parameter) = callRequestOfferInfoDetails.onNext(parameter)

    override fun onSuccessRefresh(): Observable<Dashboard.Cell> = onSuccessRefresh
    override fun onSuccessRequestOfferInfoDetails(): Observable<Any> = onSuccessRequestOfferInfoDetails
    override fun onSuccessRequestCarrierName(): Observable<String> = onSuccessRequestCarrierName
    override fun onSuccessRouteDataList(): Observable<RouteDataList> = onSuccessRouteDataList
    override fun onClickViewOfferHistory(): Observable<Intent> = onClickViewOfferHistory
}

interface YourOffersDetailInputs {
    fun clickViewOfferHistory(tradeOfferWrapper: TradeOfferWrapper)
    fun requestOfferInfoDetails(pair: Pair<String, Long>)
    fun requestCarrierName(carrierCode: String)
    fun callRequestOfferInfoDetails(parameter: Parameter)
}

interface YourOffersDetailOutputs {
    fun onSuccessRefresh(): Observable<Dashboard.Cell>
    fun onSuccessRequestOfferInfoDetails(): Observable<Any>
    fun onSuccessRequestCarrierName(): Observable<String>
    fun onSuccessRouteDataList(): Observable<RouteDataList>
    fun onClickViewOfferHistory() : Observable<Intent>
}
