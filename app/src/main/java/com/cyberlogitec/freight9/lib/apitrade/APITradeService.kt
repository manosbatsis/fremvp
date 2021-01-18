package com.cyberlogitec.freight9.lib.apitrade

import com.cyberlogitec.freight9.BuildConfig
import com.cyberlogitec.freight9.lib.apistat.*
import com.cyberlogitec.freight9.lib.model.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface APITradeService {

    object EndPoint {
        const val baseUrl = BuildConfig.SERVER_TRADE_URL
        const val wsUrl = BuildConfig.WS_URL
        //fun wsUrl(topicId: Long?) = wsUrl + "ws/chat/" + topicId + "/"
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Member (MVP)

    @POST("api/v1/product/register")
    fun register(@Body body: User): Single<User>

    @POST("api/v1/product/login")
    fun login(@Body body: User): Single<User>

    @DELETE("api/v1/product/logout")
    fun logout(): Completable

    @FormUrlEncoded
    @POST("oauth/token")
    fun getToken(
            @Header("Authorization") basicAuth: String,
            @Field("grant_type") grantType: String,
            @Field("username") username: String,
            @Field("password") password: String
    ): Observable<TokenResponse>

    @FormUrlEncoded
    @POST("oauth/token")
    fun getTokenRefresh(
            @Header("Authorization") basicAuth: String,
            @Field("grant_type") grantType: String,
            @Field("refresh_token") username: String
    ): Observable<TokenResponse>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Route Select (MVP)

    @GET("api/v1/common/open/port/all")
    fun getPortData(): Single<List<Port>>

    @GET("api/v1/common/open/port/paging")
    fun getPortDataByPage(@Query("size") size: Int, @Query("page") page: Int): Single<PortPageInfo>

    @POST("api/v1/product/routeselect/list/serviceroute")
    fun postServiceRoute(@Body body: PostServiceRouteRequest): Single<List<PostServiceRouteResponse>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Schedule MDM (MVP)

    @GET("api/v1/common/open/mdm/schedule/all")
    fun getScheduleData(): Single<List<Schedule>>

    @GET("api/v1/common/open/mdm/schedule/raw/paging")
    fun getScheduleDataByPage(@Query("size") size: Int, @Query("page") page: Int): Single<SchedulePageInfo>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Offer (MVP)

    @POST("api/v1/product/inventory/list")
    fun postInventories(@Body body: PostInventoryListRequest) : Single<List<Inventory>>

    @GET("api/v1/contract/{masterContractNumber}")
    fun getContract(@Path("masterContractNumber") masterContractNumber: String) : Single<Contract>

    @POST("api/v1/product/offer/new")
    fun postOffer(@Body body: Offer) : Completable

    @POST("api/v1/product/offer/cancel")
    fun deleteOffer(@Body body: Offer) : Single<Response<Unit>>

    @GET("/api/v1/code/PaymentPlan")
    fun getPaymentPlans(): Single<List<PaymentPlan>>

    @GET("/api/v1/code/PaymentTerm")
    fun getPaymentTerm(): Single<PaymentTerm>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Buy Order (ASIS)

    @GET("api/v1/product/buyorder/order/detail/{oferGrpNr}")
    fun getBuyOrderDetails(@Path("oferGrpNr") oferGrpNr: String): Single<List<BuyOrderDetail>>

    @GET("api/v1/product/inventory/detail/{invnNr}")
    fun getInventoryDetails(@Path("invnNr") invnNr: String): Single<List<InventoryDetailDummy>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Sell Order (ASIS)

    @GET("api/v1/product/sellorder/order/list")
    fun getSellOrderList(): Single<List<BorList>>

    @GET("api/v1/product/sellorder/order/detail/{oferGrpNr}")
    fun getSellOrderDetails(@Path("oferGrpNr") oferGrpNr: String): Single<List<BuyOrderDetail>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Market (MVP)

    //@POST("api/v1/product/market/offerchart/list")
    @POST("api/v1/marketwatch/filtered/default")
    fun postMarketOfferList(@Body body: PostMarketOfferListRequest): Single<MarketChartOfferList>

    @POST("api/v1/marketwatch/filtered/cell/orders")
    fun getOrderList(@Body body: BorList): Single<List<BorList>>

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // marketwatch week bid/ask
    @POST("api/v1/marketwatch/filtered/cell/orders2")
    fun postMarketWatchBidAsk(@Body body: PostMarketWatchProductWeekDetailChartListRequest): Single<List<BorList>>

    // Etc (ASIS)

    @POST("api/v1/product/images")
    fun postImage(@Body body: PostImageBody): Single<PostImageResponse>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Trade Offer Save
    @POST("api/v1/product/offer/new")
    fun postTradeOfferNew(@Body orderTradeOfferDetail: OrderTradeOfferDetail): Single<Response<Message>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Trade Offer Detail
    // api/v1/product/offer?offerNumber=P202002201024151097400000181&offerChangeSeq=0
    @GET("api/v1/product/offer")
    fun getTradeOfferDetail(@Query("offerNumber") offerNumber: String,
                            @Query("offerChangeSeq") offerChangeSeq: Long): Single<Response<OrderTradeOfferDetail>>

    // 과거주차 정보 제거된 응답
    @GET("api/v1/product/offer/target")
    fun getProductOfferDetailTarget(@Query("offerNumber") offerNumber: String,
                            @Query("offerChangeSeq") offerChangeSeq: Long): Single<Response<OrderTradeOfferDetail>>

    @GET("api/v1/trade/offer/target")
    fun getTradeOfferDetailTarget(@Query("offerNumber") referenceOfferNumber: String,
                                  @Query("offerChangeSeq") referenceOfferChangeSeq: Long): Single<Response<OrderTradeOfferDetail>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Inventory list & Inventory detail per item (used in SellOrder)
    @POST("api/v1/product/inventory/list/ForSellOffer")
    fun postInventoryListForSellOffer(@Body body: PostInventoryListRequest): Single<Response<List<InventoryList>>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Inventory list & Inventory detail per item & PolPod Filter List
    // Inventory list
    @POST("api/v1/product/inventory/list")
    fun postInventoryList(@Body body: PostInventoryListRequest)
            : Single<Response<List<InventoryList>>>

    // For Inventory Filter
    @GET("api/v1/product/inventory/list/polpod")
    fun getInventoryListPolPod(): Single<Response<List<InventoryList>>>

    // Inventory detail 1 (Inventory)
    @GET("api/v1/product/inventory/single/{inventoryNumber}/{inventoryChangeSeq}")
    fun getInventorySingleDetail(@Path("inventoryNumber") inventoryNumber: String,
                                 @Path("inventoryChangeSeq") inventoryChangeSeq: Int)
            : Single<Response<InventoryDetails>>

    // Inventory detail 2 (Inventory + master Contract)
    //@GET("api/v1/contract/{masterContractNumber}")
    @GET("api/v1/contract/{masterContractNumber}")
    fun getMasterContractWithInventory(@Path("masterContractNumber") masterContractNumber: String)
            : Single<Response<MasterContractWithInventory>>

    @GET("/api/v1/code/PaymentPlan/{paymentPlanCode}")
    fun getPaymentPlan(@Path("paymentPlanCode") paymentPlanCode: String)
            : Single<Response<PaymentPlan>>

    @GET("/api/v1/code/PaymentPlan")
    fun getPaymentPlan()
            : Single<Response<List<PaymentPlan>>>

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Alpha - R2 : Dashboard
    // Dashboard summary : YourOffersActivity
    @POST("api/v1/dashboard2/list/summary")
    fun postDashboardSummaryList(@Body body: PostDashboardSummaryListRequest)
            : Single<Response<Dashboard>>

    // Dashboard week list : YourOffersActivity
    @POST("api/v1/dashboard2/list/summary/weeklist")
    fun postDashboardWeekList(@Body body: PostDashboardWeekListRequest)
            : Single<Response<DashboardWeekList>>

    // Dashboard route list : YourOffersActivity
    @POST("api/v1/dashboard2/list/summary/rtelist")
    fun postDashboardRouteList(@Body body: PostDashboardRouteListRequest)
            : Single<Response<DashboardRouteList>>

    /**
     * Dashboard detail : YourOffersDetailPopupActivity
     * offerNumber : refer to column offerNumber
     * offerWeek   : refer to the column baseYearWeek
     */
    @GET("api/v1/dashboard2/offer/weekdetail")
    fun getDashboardOfferWeekDetail(@Query("offernumber") offerNumber: String,
                                    @Query("baseyearweek") offerWeek: String)
            : Single<Response<DashboardOfferWeekDetail>>

    /**
     * Dashboard detail > Events : YourOffersHistoryActivity
     * offerNumber : refer to column offerNumber
     */
    @GET("api/v1/dashboard2/offer/offerevents")
    fun getDashboardOfferOfferEvents(@Query("offernumber") offerNumber: String)
            : Single<Response<DashboardOfferHistory>>

}