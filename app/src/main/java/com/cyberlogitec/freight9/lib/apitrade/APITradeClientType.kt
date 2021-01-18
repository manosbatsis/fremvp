package com.cyberlogitec.freight9.lib.apitrade

import com.cyberlogitec.freight9.lib.apistat.*
import com.cyberlogitec.freight9.lib.model.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body

interface APITradeClientType {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Offer (MVP)

    fun postInventories(body: PostInventoryListRequest) : Single<List<Inventory>>
    fun getContract(masterContractNumber: String) : Single<Contract>
    fun postOffer(@Body body: Offer) : Completable
    fun deleteOffer(@Body body: Offer) : Single<Response<Unit>>

    fun getPaymentPlans(): Single<List<PaymentPlan>>
    fun getPaymentTerm(): Single<PaymentTerm>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Buy Order

    fun getBuyOrderDetails(oferGrpNr: String): Single<List<BuyOrderDetail>>

    fun getInventoryDetails(invnNr: String): Single<List<InventoryDetailDummy>>
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Sell Order

    fun getSellOrderList(): Single<List<BorList>>

    fun getSellOrderDetails(oferGrpNr: String): Single<List<BuyOrderDetail>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Member
    fun getToken(username: String, password: String) : Observable<TokenResponse>

    fun getTokenRefresh(refreshToken: String): Observable<TokenResponse>

    fun register(user: User): Single<User>

    fun logIn(user: User): Single<User>

    fun logOut(): Completable

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Market
    fun postMarketOfferList(body: PostMarketOfferListRequest): Single<MarketChartOfferList>

    fun getOrderList(body: BorList): Single<List<BorList>>

    fun postMarketWatchBidAsk(body: PostMarketWatchProductWeekDetailChartListRequest): Single<List<BorList>>

    // Etc

    fun postImage(image: String): Single<PostImageResponse>

    fun getPortData(): Single<List<Port>>

    fun getPortDataByPage(size: Int, page: Int): Single<PortPageInfo>

    fun postServiceRoute(postServiceRouteRequest: PostServiceRouteRequest): Single<List<PostServiceRouteResponse>>

    fun getScheduleDataByPage(size: Int, page: Int): Single<SchedulePageInfo>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Trade Offer Save
    fun postTradeOfferNew(orderTradeOfferDetail: OrderTradeOfferDetail): Single<Response<Message>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Trade Offer Detail
    fun getTradeOfferDetail(offerNumber: String, offerChangeSeq: Long): Single<Response<OrderTradeOfferDetail>>

    // 과거주차 정보 제거된 응답
    fun getProductOfferDetailTarget(offerNumber: String, offerChangeSeq: Long): Single<Response<OrderTradeOfferDetail>>

    // 과거주차 정보 제거된 응답
    fun getTradeOfferDetailTarget(referenceOfferNumber: String, referenceOfferChangeSeq: Long): Single<Response<OrderTradeOfferDetail>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Inventory list in SellOrder
    fun postInventoryListForSellOffer(body: PostInventoryListRequest): Single<Response<List<InventoryList>>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Inventory list & Inventory detail per item & PolPod Filter List
    // Inventory list
    fun postInventoryList(body: PostInventoryListRequest): Single<Response<List<InventoryList>>>

    // For Inventory Filter
    fun getInventoryListPolPod(): Single<Response<List<InventoryList>>>

    // Inventory detail 1 (Inventory)
    fun getInventorySingleDetail(inventoryNumber: String, inventoryChangeSeq: Int): Single<Response<InventoryDetails>>

    // Inventory detail 2 (Inventory + master Contract)
    fun getMasterContractWithInventory(masterContractNumber: String): Single<Response<MasterContractWithInventory>>

    // Payment plan
    fun getPaymentPlan(paymentPlanCode: String): Single<Response<PaymentPlan>>

    // Payment plan
    fun getPaymentPlan(): Single<Response<List<PaymentPlan>>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Alpha - R2 : Dashboard
    // Dashboard summary
    fun postDashboardSummaryList(body: PostDashboardSummaryListRequest): Single<Response<Dashboard>>

    // Dashboard week list
    fun postDashboardWeekList(body: PostDashboardWeekListRequest): Single<Response<DashboardWeekList>>

    // Dashboard Route list
    fun postDashboardRouteList(body: PostDashboardRouteListRequest): Single<Response<DashboardRouteList>>

    // Dashboard week list > week detail
    fun getDashboardOfferWeekDetail(offerNumber: String, offerWeek: String): Single<Response<DashboardOfferWeekDetail>>

    // Dashboard offer week detail > History
    fun getDashboardOfferOfferEvents(offerNumber: String): Single<Response<DashboardOfferHistory>>

}