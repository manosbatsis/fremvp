package com.cyberlogitec.freight9.common

import android.content.Context
import android.os.Bundle
import com.cyberlogitec.freight9.config.ConstantTradeOffer.PAYMENT_PLANCODE_PS
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.util.showToast
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit

/*
* BaseViewModel For Offer Cancel, Revise
* */
open class BaseOfferViewModel(context: Context) : BaseViewModel(context), BaseOfferInputs, BaseOfferOutputs {

    val onSuccessRequestOfferUsingSameCondition: PublishSubject<Triple<String, Boolean, Offer>>
            = PublishSubject.create<Triple<String, Boolean, Offer>>()

    val baseOfferInputs: BaseOfferInputs = this
    private val requestLongProgressBar = PublishSubject.create<Parameter>()
    private val requestDiscardOffer = PublishSubject.create<Any>()
    private val requestPaymentPlans = PublishSubject.create<Unit>()

    val baseOfferOutputs: BaseOfferOutputs = this
    private val onRequestLongProgressBar: PublishSubject<Unit> = PublishSubject.create<Unit>()
    private val onSuccessDiscardOffer = PublishSubject.create<Response<Unit>>()
    private val onSuccessRequestPaymentPlans = PublishSubject.create<Response<List<PaymentPlan>>>()

    private val paymentPlans = BehaviorSubject.create<List<PaymentPlan>>()

    init {
        requestLongProgressBar
                .bindToLifeCycle()
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .delay(10, TimeUnit.SECONDS)
                .subscribe {
                    onRequestLongProgressBar.onNext(Unit)
                }

        requestDiscardOffer
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .map { data ->
                    val offer: Offer = if (data is OrderTradeOfferDetail) {
                        makeDiscardOffer(data)
                    } else {
                        data as Offer
                    }
                    offer
                }
                .flatMapMaybe {
                    enviorment.apiTradeClient.deleteOffer(it)
                            .handleToError(hideLoadingDialog)
                            .toMaybe()
                }
                .map { hideLoadingDialog.onNext(Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe(
                        { response ->
                            onSuccessDiscardOffer.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        requestPaymentPlans
                .flatMapMaybe {
                    enviorment.apiTradeClient.getPaymentPlan()
                            .handleToError(hideLoadingDialog).neverError()
                }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            if (response.isSuccessful) {
                                paymentPlans.onNext(response.body() as List<PaymentPlan>)
                            } else {
                                context.showToast("Fail Save Offer(Http)\n" + response.errorBody())
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )
    }

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        super.onCreate(context, savedInstanceState)

        Timber.d("f9: onCreate - plans = " + paymentPlans.value)
        if (!paymentPlans.hasValue()) {
            Timber.d("f9: onCreate - request plans")
            requestPaymentPlans.onNext(Unit)
        }
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

    }

    // interface : in
    override fun requestLongProgressBar(parameter: Parameter) = requestLongProgressBar.onNext(parameter)
    override fun requestDiscardOffer(data: Any) = requestDiscardOffer.onNext(data)
    override fun requestPaymentPlans(unit: Unit) = requestPaymentPlans.onNext(unit)

    // interface : out
    override fun onRequestLongProgressBar(): Observable<Unit> = onRequestLongProgressBar
    override fun onSuccessDiscardOffer(): Observable<Response<Unit>> = onSuccessDiscardOffer
    override fun onSuccessRequestPaymentPlans(): Observable<Response<List<PaymentPlan>>> = onSuccessRequestPaymentPlans

    fun requestOfferUsingSameCondition(triple: Triple<String, Boolean, OrderTradeOfferDetail>) {
        onSuccessRequestOfferUsingSameCondition.onNext(Triple(
                triple.first,       // Offer Type
                triple.second,      // Discard 유무
                makeDiscardOffer(triple.third))
        )
    }

    fun getPaymentPlans() : List<PaymentPlan> {
        var plans: MutableList<PaymentPlan> = ArrayList()
        if (paymentPlans.hasValue()) {
            plans = paymentPlans.value as MutableList<PaymentPlan>
            Timber.d("f9: getPaymentPlans - plans = " + paymentPlans.value)
        } else {
            Timber.d("f9: getPaymentPlans - NO plans")
        }
        return plans
    }

    private fun makeDiscardOffer(orderTradeOfferDetail: OrderTradeOfferDetail) = Offer(
            allYn = orderTradeOfferDetail.allYn,
            offerNumber = orderTradeOfferDetail.referenceOfferNumberOfCurrentProduct,
            offerChangeSeq = orderTradeOfferDetail.referenceOfferChangeSeqOfCurrentProduct.toLong(),
            referenceOfferNumber = orderTradeOfferDetail.offerNumber,
            referenceOfferChangeSeq = orderTradeOfferDetail.offerChangeSeq.toLong(),
            masterContractNumber = orderTradeOfferDetail.masterContractNumber!!,
            offerTypeCode = orderTradeOfferDetail.offerTypeCode,
            offerRdTermCode = orderTradeOfferDetail.offerRdTermCode,
            offerPaymentTermCode = orderTradeOfferDetail.offerPaymentTermCode,
            offerLineItems = getOfferLineItems(orderTradeOfferDetail),
            offerRoutes = getOfferRoutes(orderTradeOfferDetail),
            offerCarriers = getOfferCarriers(orderTradeOfferDetail),
            offerPaymentPlanCode = getPaymentPlanCode(orderTradeOfferDetail)
    )

    private fun getOfferLineItems(orderTradeOfferDetail: OrderTradeOfferDetail): List<OfferLineItem> {
        val offerLineItems: MutableList<OfferLineItem> = mutableListOf()
        for (lineItem in orderTradeOfferDetail.offerLineItems) {
            val offerPrices: MutableList<OfferPrice> = mutableListOf()
            for (price in lineItem.offerPrices) {
                offerPrices.add(OfferPrice(
                        containerTypeCode = price.containerTypeCode,
                        containerSizeCode = price.containerSizeCode,
                        offerPrice = price.offerPrice
                ))
            }
            offerLineItems.add(OfferLineItem(
                    baseYearWeek = lineItem.baseYearWeek,
                    offerQty = lineItem.offerQty,
                    offerPrice = lineItem.offerPrice.toInt(),
                    tradeContainerTypeCode = lineItem.tradeContainerTypeCode,
                    tradeContainerSizeCode = lineItem.tradeContainerSizeCode,
                    firstPaymentRatio = lineItem.firstPaymentRatio,
                    middlePaymentRatio = lineItem.middlePaymentRatio,
                    balancedPaymentRatio = lineItem.balancedPaymentRatio,
                    offerPrices = offerPrices
            ))
        }
        return offerLineItems
    }

    private fun getOfferRoutes(orderTradeOfferDetail: OrderTradeOfferDetail): List<OfferRoute> {
        val offerRoutes: MutableList<OfferRoute> = mutableListOf()
        for (route in orderTradeOfferDetail.offerRoutes) {
            offerRoutes.add(OfferRoute(
                    offerRegSeq = route.offerRegSeq,
                    locationCode = route.locationCode,
                    locationName = route.locationName,
                    locationTypeCode = route.locationTypeCode
            ))
        }
        return offerRoutes
    }

    private fun getOfferCarriers(orderTradeOfferDetail: OrderTradeOfferDetail): List<OfferCarrier> {
        val offerCarriers: MutableList<OfferCarrier> = mutableListOf()
        for (carrier in orderTradeOfferDetail.offerCarriers) {
            offerCarriers.add(OfferCarrier(
                    offerCarrierCode = carrier.offerCarrierCode
            ))
        }
        return offerCarriers
    }

    private fun getPaymentPlanCode(orderTradeOfferDetail: OrderTradeOfferDetail) =
            if (paymentPlans.hasValue()) {
                Timber.d("f9: getPaymentPlanCode = " + paymentPlans.value)
                val offerLineItem = orderTradeOfferDetail.offerLineItems[0]
                val findPlan = paymentPlans.value?.let {
                    it.find { plan ->
                        plan.initialPaymentRatio == offerLineItem.firstPaymentRatio
                                && plan.middlePaymentRatio == offerLineItem.middlePaymentRatio
                                && plan.balancePaymentRatio == offerLineItem.balancedPaymentRatio
                    }
                }
                findPlan?.paymentPlanCode ?: PAYMENT_PLANCODE_PS
            } else {
                Timber.d("f9: getPaymentPlanCode = NO")
                PAYMENT_PLANCODE_PS
            }
}

interface BaseOfferInputs {
    fun requestLongProgressBar(parameter: Parameter)
    fun requestDiscardOffer(data: Any)
    fun requestPaymentPlans(unit: Unit)
}

interface BaseOfferOutputs {
    fun onRequestLongProgressBar() : Observable<Unit>
    fun onSuccessDiscardOffer() : Observable<Response<Unit>>
    fun onSuccessRequestPaymentPlans() : Observable<Response<List<PaymentPlan>>>
}