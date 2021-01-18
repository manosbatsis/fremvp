package com.cyberlogitec.freight9.ui.sellorder

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseOfferViewModel
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.apitrade.PostInventoryListRequest
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.handleToError
import com.cyberlogitec.freight9.lib.rx.neverError
import com.cyberlogitec.freight9.lib.ui.route.RouteData
import com.cyberlogitec.freight9.lib.ui.route.RouteDataList
import com.cyberlogitec.freight9.lib.util.Intents
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import timber.log.Timber

class SellOrderViewModel(context: Context) : BaseOfferViewModel(context),
        SellOrderInputs, SellOrderOutputs  {

    val inPuts: SellOrderInputs = this
    private val requestOfferInfo = PublishSubject.create<Intent>()
    private val requestOfferInfoDetails = PublishSubject.create<BorList>()
    private val requestSellOrderSave = PublishSubject.create<OrderTradeOfferDetail>()
    private val requestSellDataOwnrPtrId = PublishSubject.create<Parameter>()
    private val requestInventoryList = PublishSubject.create<PostInventoryListRequest>()
    private val requestInventoryDetail = PublishSubject.create<InventoryList>()
    private val requestPortNm = PublishSubject.create<String>()
    private val requestCarrierName = PublishSubject.create<String>()

    val outPuts: SellOrderOutputs = this
    private val onSuccessRouteDataList = PublishSubject.create<RouteDataList>()
    private val onSuccessRequestCarrierName = PublishSubject.create<String>()
    private val onSuccessRequestPortNm = PublishSubject.create<String>()
    private val onSuccessRequestOfferInfo = PublishSubject.create<BorList>()
    private val onSuccessRequestOfferInfoDetails = PublishSubject.create<Response<OrderTradeOfferDetail>>()
    private val onSuccessRequestSellOrderSave = PublishSubject.create<Response<Message>>()
    private val onSuccessRequestSellDataOwnrPtrId = PublishSubject.create<String>()
    private val onSuccessRequestInventoryList = PublishSubject.create<Response<List<InventoryList>>>()
    private val onSuccessRequestInventoryDetail = PublishSubject.create<Response<InventoryDetails>>()

    private val refresh = PublishSubject.create<Parameter>()

    init {
        /**
         * Offer Info + Offer Details
         */
        requestOfferInfo
                .map { it.getSerializableExtra(Intents.OFFER_ITEM) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .neverError()
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestOfferInfo.onNext(it as BorList)
                    requestOfferInfoDetails.onNext(it)
                }

        /**
         * it : borList, offer detail 요청, route data 추출
         */
        requestOfferInfoDetails
                .map { requestSellDataOwnrPtrId.onNext(Parameter.EVENT); it }
                .flatMapMaybe {
                    enviorment.apiTradeClient.getTradeOfferDetailTarget(it.referenceOfferNumber!!, it.referenceOfferChangeSeq!!)
                        .handleToError(hideLoadingDialog)
                        .toMaybe()
                }
                .map {
                    if (it.isSuccessful) {
                        //--------------------------------------------------------------------------
                        // Whole Route > Grid 에 사용될 Route Data 추출
                        //--------------------------------------------------------------------------
                        val routeDataList = RouteDataList()
                        val offerRoutesMap = (it.body() as OrderTradeOfferDetail).offerRoutes
                                .sortedBy { offerRoute -> offerRoute.offerRegSeq }
                                .groupBy { offerRoute -> offerRoute.offerRegSeq }
                        for (data in offerRoutesMap) {
                            // data.key : offerRegSeq
                            var porCode = ""; var polCode = ""; var podCode = ""; var delCode = ""
                            var porCodeName = ""; var polCodeName = ""; var podCodeName = ""; var delCodeName = ""
                            for (routeDatas in data.value) {
                                when (routeDatas.locationTypeCode) {
                                    ConstantTradeOffer.LOCATION_TYPE_CODE_POR -> {
                                        porCode = routeDatas.locationCode
                                        porCodeName = routeDatas.locationName
                                    }
                                    ConstantTradeOffer.LOCATION_TYPE_CODE_POL -> {
                                        polCode = routeDatas.locationCode
                                        polCodeName = routeDatas.locationName
                                    }
                                    ConstantTradeOffer.LOCATION_TYPE_CODE_POD -> {
                                        podCode = routeDatas.locationCode
                                        podCodeName = routeDatas.locationName
                                    }
                                    ConstantTradeOffer.LOCATION_TYPE_CODE_DEL -> {
                                        delCode = routeDatas.locationCode
                                        delCodeName = routeDatas.locationName
                                    }
                                }
                            }
                            routeDataList.add(RouteData(porCode, porCodeName, polCode, polCodeName,
                                    podCode, podCodeName, delCode, delCodeName))
                        }
                        onSuccessRouteDataList.onNext(routeDataList)
                    }
                    it
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe(
                        { response ->
                            onSuccessRequestOfferInfoDetails.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * 현재 response 값이 없으므로 아래처럼 처리한다
         * 아니면, https://code-examples.net/ko/q/2216c1b 참고해서 NullOnEmptyConverterFactory 추가
         */
        requestSellOrderSave
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.postTradeOfferNew(it)
                            .handleToError(hideLoadingDialog)
                            .toMaybe()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestSellOrderSave.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * environment의 carrier code 요청
         */
        requestSellDataOwnrPtrId.bindToLifeCycle()
                .map { enviorment.currentUser.getCrcyCd() }
                .subscribe { onSuccessRequestSellDataOwnrPtrId.onNext(it!!) }

        // Inventory List 요청
        requestInventoryList
                .flatMapMaybe {
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.postInventoryListForSellOffer(it)
                            .handleToError(hideLoadingDialog).neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                    { response ->
                        onSuccessRequestInventoryList.onNext(response)
                    }, { throwable ->
                        error.onNext(throwable)
                    }
                )

        // Inventory Detail 요청
        requestInventoryDetail
                .flatMapMaybe {
                    //showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.getInventorySingleDetail(it.inventoryNumber!!, it.inventoryChangeSeq!!)
                            .handleToError(hideLoadingDialog).neverError()
                }
                //.map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            onSuccessRequestInventoryDetail.onNext(response)
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * Port name 요청
         */
        requestPortNm
                .map { enviorment.portRepository.getPortNm(it) }
                .bindToLifeCycle()
                .subscribe { onSuccessRequestPortNm.onNext(it) }

        /**
         * Carrier name 요청
         */
        requestCarrierName
                .flatMap { enviorment.carrierRepository.getCarrierName(it) }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (onSuccessRequestCarrierName)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        //refresh.onNext(Parameter.EVENT)
    }

    override fun requestOfferInfo(intent: Intent)  = requestOfferInfo.onNext(intent)
    override fun requestOfferInfoDetails(borList: BorList) = requestOfferInfoDetails.onNext(borList)

    override fun onSuccessRouteDataList(): Observable<RouteDataList> = onSuccessRouteDataList

    override fun requestInventoryList(inventoryRequest: PostInventoryListRequest) = requestInventoryList.onNext(inventoryRequest)
    override fun onSuccessRequestInventoryList(): Observable<Response<List<InventoryList>>> = onSuccessRequestInventoryList

    override fun requestInventoryDetail(inventoryList: InventoryList)= requestInventoryDetail.onNext(inventoryList)
    override fun onSuccessRequestInventoryDetail(): Observable<Response<InventoryDetails>> = onSuccessRequestInventoryDetail

    override fun requestSellDataOwnrPtrId(parameter: Parameter) = requestSellDataOwnrPtrId.onNext(parameter)
    override fun onSuccessRequestSellDataOwnrPtrId(): Observable<String> = onSuccessRequestSellDataOwnrPtrId

    override fun requestSellOrderSave(orderTradeOfferDetail: OrderTradeOfferDetail) = requestSellOrderSave.onNext(orderTradeOfferDetail)
    override fun onSuccessRequestSellOrderSave(): Observable<Response<Message>> = onSuccessRequestSellOrderSave

    override fun onSuccessRequestOfferInfo(): Observable<BorList> = onSuccessRequestOfferInfo
    override fun onSuccessRequestOfferInfoDetails(): Observable<Response<OrderTradeOfferDetail>> = onSuccessRequestOfferInfoDetails

    override fun requestPortNm(portCode: String) = requestPortNm.onNext(portCode)
    override fun onSuccessRequestPortNm(): Observable<String> = onSuccessRequestPortNm

    override fun requestCarrierName(carrierCode: String) = requestCarrierName.onNext(carrierCode)
    override fun onSuccessRequestCarrierName(): Observable<String> = onSuccessRequestCarrierName
}

interface SellOrderInputs {
    fun requestOfferInfo(intent: Intent)
    fun requestOfferInfoDetails(borList: BorList)
    fun requestInventoryList(inventoryRequest: PostInventoryListRequest)
    fun requestInventoryDetail(inventoryList: InventoryList)
    fun requestSellOrderSave(orderTradeOfferDetail: OrderTradeOfferDetail)
    fun requestSellDataOwnrPtrId(parameter: Parameter)
    fun requestPortNm(portCode: String)
    fun requestCarrierName(carrierCode: String)
}

interface SellOrderOutputs {
    fun onSuccessRouteDataList(): Observable<RouteDataList>
    fun onSuccessRequestInventoryList() : Observable<Response<List<InventoryList>>>
    fun onSuccessRequestInventoryDetail() : Observable<Response<InventoryDetails>>
    fun onSuccessRequestSellOrderSave() : Observable<Response<Message>>
    fun onSuccessRequestOfferInfo() : Observable<BorList>
    fun onSuccessRequestOfferInfoDetails() : Observable<Response<OrderTradeOfferDetail>>
    fun onSuccessRequestSellDataOwnrPtrId() : Observable<String>
    fun onSuccessRequestPortNm() : Observable<String>
    fun onSuccessRequestCarrierName() : Observable<String>
}