package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.OrderTradeOfferDetail
import com.cyberlogitec.freight9.lib.model.PaymentPlan
import com.cyberlogitec.freight9.lib.model.TradeOfferWrapper
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.ParameterAny
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.lib.util.Intents.Companion.YOUR_OFFER_DASHBOARD_ITEM
import com.cyberlogitec.freight9.lib.util.Intents.Companion.YOUR_OFFER_FRAG_INDEX
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber

class YourOffersSwipeViewModel(context: Context) : BaseViewModel(context), Inputs, Outputs  {

    ////////////////////////////////////////////////////////////////////////////////////////////
    // inputs

    val inPuts: Inputs = this
    private val clickToDetail = PublishSubject.create<TradeOfferWrapper>()
    private val requestCarrierName = PublishSubject.create<String>()
    private val requestPaymentPlan = PublishSubject.create<String>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // outputs

    val outPuts: Outputs = this
    private val onSuccessRefresh = PublishSubject.create<Pair<Int, Dashboard.Cell>>()
    private val onSuccessRequestOfferInfoDetails = PublishSubject.create<Response<OrderTradeOfferDetail>>()
    private val onSuccessRequestCarrierName = PublishSubject.create<String>()
    private val onSuccessRequestPaymentPlan = PublishSubject.create<Response<PaymentPlan>>()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // intents

    private val parentId = BehaviorSubject.create<Intent>()
    private val refresh = PublishSubject.create<Parameter>()

    init {

        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { Timber.d("f9: intent --> $it"); it }
                .bindToLifeCycle()
                .subscribe( parentId )

        ///////////////////////////////////////////////////////////////////////////////////////////
        // refresh (masterContractNumber)
        // Request offer detail...
        // GET http://api.freight9.com:8080/api/v1/trade/offer/target?offerNumber=F202004230900248953770009221&offerChangeSeq=0
        parentId.compose<Intent> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { intent ->

                    val fragmentIndex = intent.getIntExtra(YOUR_OFFER_FRAG_INDEX, 0)
                    val cell = intent.getSerializableExtra(YOUR_OFFER_DASHBOARD_ITEM) as Dashboard.Cell
                    onSuccessRefresh.onNext(Pair(fragmentIndex, cell))

                    enviorment.apiTradeClient.getTradeOfferDetailTarget(cell.offerNumber, cell.offerChangeSeq.toLong())
                            .handleToError(hideLoadingDialog)
                            .toMaybe()
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
                .subscribe (onSuccessRequestCarrierName)

        requestPaymentPlan
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getPaymentPlan(it)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestPaymentPlan.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        // YOUR_OFFER_DASHBOARD_ITEM as Dashboard, YOUR_OFFER_TRADE_WRAPPER_INFO as tradeOfferWrapper
        parentId.compose<Intent> { clickToDetail.withLatestFrom(it,
                BiFunction { tradeOfferWrapper, intent ->
                    intent.putExtra(Intents.YOUR_OFFER_TRADE_WRAPPER_INFO, tradeOfferWrapper) }) }
                .map { intent ->
                    Pair(ParameterAny.ANY_ITEM_OBJECT, intent)
                }
                .bindToLifeCycle()
                .subscribe(onClickViewParameterAny)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }


    // interface : in
    override fun clickToDetail(tradeOfferWrapper: TradeOfferWrapper) = clickToDetail.onNext(tradeOfferWrapper)
    override fun requestCarrierName(carrierCode: String) = requestCarrierName.onNext(carrierCode)
    override fun requestPaymentPlan(paymentPlanCode: String) = requestPaymentPlan.onNext(paymentPlanCode)

    // interface : out
    override fun onSuccessRefresh(): Observable<Pair<Int, Dashboard.Cell>> = onSuccessRefresh
    override fun onSuccessRequestOfferInfoDetails(): Observable<Response<OrderTradeOfferDetail>> = onSuccessRequestOfferInfoDetails
    override fun onSuccessRequestCarrierName(): Observable<String> = onSuccessRequestCarrierName
    override fun onSuccessRequestPaymentPlan(): Observable<Response<PaymentPlan>> = onSuccessRequestPaymentPlan
}

interface Inputs {
    fun clickToDetail(tradeOfferWrapper: TradeOfferWrapper)
    fun requestCarrierName(carrierCode: String)
    fun requestPaymentPlan(paymentPlanCode: String)
}

interface Outputs {
    fun onSuccessRefresh(): Observable<Pair<Int, Dashboard.Cell>>
    fun onSuccessRequestOfferInfoDetails(): Observable<Response<OrderTradeOfferDetail>>
    fun onSuccessRequestCarrierName() : Observable<String>
    fun onSuccessRequestPaymentPlan() : Observable<Response<PaymentPlan>>
}