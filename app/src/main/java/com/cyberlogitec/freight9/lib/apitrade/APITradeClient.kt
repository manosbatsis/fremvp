package com.cyberlogitec.freight9.lib.apitrade

import com.cyberlogitec.freight9.lib.apistat.*
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.util.toBase64
import com.cyberlogitec.freight9.lib.util.toFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import retrofit2.Response


class APITradeClient(val apiTradeService: APITradeService) : APITradeClientType {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Offer (MVP)

    override fun postInventories(body: PostInventoryListRequest): Single<List<Inventory>> =
            apiTradeService.postInventories(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getContract(masterContractNumber: String): Single<Contract> =
            apiTradeService.getContract(masterContractNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postOffer(body: Offer): Completable =
            apiTradeService.postOffer(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun deleteOffer(body: Offer): Single<Response<Unit>> =
            apiTradeService.deleteOffer(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getPaymentPlans(): Single<List<PaymentPlan>> =
            apiTradeService.getPaymentPlans()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getPaymentTerm(): Single<PaymentTerm> =
            apiTradeService.getPaymentTerm()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Sell Offer (ASIS)

//    override fun getInventoryDetails(invnNr: String): Single<List<SofDraftDetail>> =
//            apiService.getInventoryDetails(invnNr)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

//    override fun deleteSofDraft(oferGrpNr: String): Completable =
//            apiService.deleteSofDraft(oferGrpNr)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

//    override fun getSofDrafts(cryrCd: String): Single<List<SofDraft>> =
//            apiService.getSofDrafts(cryrCd)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

//    override fun getSofDraftDetails(oferGrpNr: String): Single<List<SofDraftDetail>> =
//            apiService.getSofDraftDetails(oferGrpNr)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

//    override fun postSofDraftDetailsFirst(body: List<SofDraftDetail>): Single<SofDraftDetail> =
//            apiService.postSofDraftDetailsFirst(body)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

//    override fun postSofDraftDetailsFinish(body: List<SofDraftDetail>): Completable =
//            apiService.postSofDraftDetailsFinish(body)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

//    override fun getSofDeposits(oferGrpNr: String): Single<List<SofDeposit>> =
//            apiService.getSofDeposits(oferGrpNr)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Buy Offer

    /*
    override fun deleteBofDraft(oferGrpNr: String): Completable =
            apiService.deleteBofDraft(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postBofContracts(body: PostPortRouteRequest): Single<List<BofContract>> =
            apiService.postBofContracts(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getServiceLanes(cryrCd: String): Single<List<BofLane>> =
            apiService.getServiceLanes(cryrCd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getServiceLaneDetails(mstrCtrkNr: String): Single<List<BofLaneDetail>> =
            apiService.getServiceLaneDetails(mstrCtrkNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getDraftServiceLanes(oferGrpNr: String): Single<List<BofLane>> =
            apiService.getDraftServiceLanes(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getBofDrafts(cryrCd: String): Single<List<BofDraft>> =
            apiService.getBofDrafts(cryrCd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getBofFirstDraftDetails(oferGrpNr: String): Single<List<BofDraftDetail>> =
            apiService.getBofFirstDraftDetails(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getBofDraftDetails(@Path("oferGrpNr") oferGrpNr: String): Single<List<BofDraftDetail>> =
            apiService.getBofDraftDetails(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getBofSecondDraftDetails(oferGrpNr: String): Single<List<BofDraftDetail>> =
            apiService.getBofSecondDraftDetails(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postBofDraftVolume(body: List<BofDraftDetail>): Single<BofDraftDetail> =
            apiService.postBofDraftVolume(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postBofDraftPrice(body: List<BofDraftDetail>): Single<BofDraftDetail> =
            apiService.postBofDraftPrice(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getBofContractDetails(mstrCtrkNr: String): Single<List<BofContractDetail>> =
            apiService.getBofContractDetails(mstrCtrkNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postBofContractDetailsFirst(body: List<BofContractLaneDetail>): Single<BofContractLaneDetail> =
            apiService.postBofContractDetailsFirst(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postBofDraftDetailsFinish(body: List<BofDraftDetail>): Completable =
            apiService.postBofDraftDetailsFinish(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getBofDeposits(oferGrpNr: String): Single<List<BofDeposit>> =
            apiService.getBofDeposits(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getNewServicelane(cryrCd: String, pol: String, pod: String): Single<NewServicelaneResponse> =
            apiService.getNewServicelane(cryrCd, pol, pod)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getNewServicelanePolsPods(cryrCd: String, svceLaneCd: String): Single<List<NewServicelanePolsPodsResponse>> =
            apiService.getNewServicelanePolsPods(cryrCd, svceLaneCd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postNewServicelaneDueDate(body: PostNewServicelaneDueDateRequest): Single<List<PostNewServicelaneDueDateResponse>> =
            apiService.postNewServicelaneDueDate(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
     */

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Member

    override fun getToken(username: String, password: String): Observable<TokenResponse> {
        val basicAuth = Credentials.basic("OPUS365-client", "OPUS365-secret")
        return apiTradeService.getToken(basicAuth, "password", username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getTokenRefresh(refreshToken: String): Observable<TokenResponse> {
        val basicAuth = Credentials.basic("OPUS365-client", "OPUS365-secret")
        return apiTradeService.getTokenRefresh(basicAuth, "refresh_token", refreshToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun register(user: User): Single<User> =
            apiTradeService.register(user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun logIn(user: User): Single<User> =
            apiTradeService.login(user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun logOut(): Completable =
            apiTradeService.logout()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Route Select

    override fun getPortData(): Single<List<Port>> {
        return apiTradeService.getPortData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getPortDataByPage(size: Int, page: Int): Single<PortPageInfo> {
        return apiTradeService.getPortDataByPage(size, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getScheduleDataByPage(size: Int, page: Int): Single<SchedulePageInfo> {
        return apiTradeService.getScheduleDataByPage(size, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun postServiceRoute(postServiceRouteRequest: PostServiceRouteRequest): Single<List<PostServiceRouteResponse>> =
            (apiTradeService.postServiceRoute(postServiceRouteRequest))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Buy Order

    override fun getBuyOrderDetails(oferGrpNr: String): Single<List<BuyOrderDetail>> =
            apiTradeService.getBuyOrderDetails(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getInventoryDetails(invnNr: String): Single<List<InventoryDetailDummy>> =
            apiTradeService.getInventoryDetails(invnNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Sell Order

    override fun getSellOrderList(): Single<List<BorList>> =
            (apiTradeService.getSellOrderList())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getSellOrderDetails(oferGrpNr: String): Single<List<BuyOrderDetail>> =
            apiTradeService.getSellOrderDetails(oferGrpNr)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Market

    override fun postMarketOfferList(body: PostMarketOfferListRequest): Single<MarketChartOfferList> {
        return apiTradeService.postMarketOfferList(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getOrderList(body: BorList): Single<List<BorList>> =
            (apiTradeService.getOrderList(body))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun postMarketWatchBidAsk(body: PostMarketWatchProductWeekDetailChartListRequest): Single<List<BorList>> {
        return apiTradeService.postMarketWatchBidAsk(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Etc


    override fun postImage(image: String): Single<PostImageResponse> =
            apiTradeService.postImage(PostImageBody(image.toFile()?.toBase64()))

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Trade Offer Save
    override fun postTradeOfferNew(orderTradeOfferDetail: OrderTradeOfferDetail): Single<Response<Message>> =
            apiTradeService.postTradeOfferNew(orderTradeOfferDetail)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Trade Offer Detail
    override fun getTradeOfferDetail(offerNumber: String, offerChangeSeq: Long): Single<Response<OrderTradeOfferDetail>> =
            apiTradeService.getTradeOfferDetail(offerNumber, offerChangeSeq)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // 과거주차 정보 제거된 응답
    override fun getProductOfferDetailTarget(offerNumber: String, offerChangeSeq: Long): Single<Response<OrderTradeOfferDetail>> =
            apiTradeService.getProductOfferDetailTarget(offerNumber, offerChangeSeq)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // 과거주차 정보 제거된 응답
    override fun getTradeOfferDetailTarget(referenceOfferNumber: String, referenceOfferChangeSeq: Long): Single<Response<OrderTradeOfferDetail>> =
            apiTradeService.getTradeOfferDetailTarget(referenceOfferNumber, referenceOfferChangeSeq)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Inventory list in SellOrder
    override fun postInventoryListForSellOffer(body: PostInventoryListRequest): Single<Response<List<InventoryList>>> =
            apiTradeService.postInventoryListForSellOffer(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // MVP : Inventory list & Inventory detail per item & PolPod Filter List
    override fun postInventoryList(body: PostInventoryListRequest): Single<Response<List<InventoryList>>> =
            apiTradeService.postInventoryList(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // For Inventory Filter
    override fun getInventoryListPolPod(): Single<Response<List<InventoryList>>> =
            apiTradeService.getInventoryListPolPod()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // Inventory detail (Inventory)
    override fun getInventorySingleDetail(inventoryNumber: String, inventoryChangeSeq: Int)
            : Single<Response<InventoryDetails>> =
            apiTradeService.getInventorySingleDetail(inventoryNumber, inventoryChangeSeq)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // Master contract (master Contract + Inventory)
    override fun getMasterContractWithInventory(masterContractNumber: String)
            : Single<Response<MasterContractWithInventory>> =
            apiTradeService.getMasterContractWithInventory(masterContractNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getPaymentPlan(paymentPlanCode: String)
            : Single<Response<PaymentPlan>> =
            apiTradeService.getPaymentPlan(paymentPlanCode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getPaymentPlan()
            : Single<Response<List<PaymentPlan>>> =
            apiTradeService.getPaymentPlan()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Alpha - R2 : Dashboard
    // Dashboard summary : YourOffersActivity
    override fun postDashboardSummaryList(body: PostDashboardSummaryListRequest): Single<Response<Dashboard>> =
            apiTradeService.postDashboardSummaryList(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // Dashboard week list : YourOffersActivity
    override fun postDashboardWeekList(body: PostDashboardWeekListRequest)
            : Single<Response<DashboardWeekList>> =
            apiTradeService.postDashboardWeekList(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    // Dashboard Route list : YourOffersActivity
    override fun postDashboardRouteList(body: PostDashboardRouteListRequest)
            : Single<Response<DashboardRouteList>> =
            apiTradeService.postDashboardRouteList(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    /**
     * Dashboard detail : YourOffersDetailActivity
     * offerNumber : refer to column offerNumber
     * offerWeek   : refer to the column baseYearWeek
     */
    override fun getDashboardOfferWeekDetail(offerNumber: String, offerWeek: String)
            : Single<Response<DashboardOfferWeekDetail>> =
            apiTradeService.getDashboardOfferWeekDetail(offerNumber, offerWeek)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    /**
     * Dashboard detail > Events : YourOffersHistoryActivity
     * offerNumber : refer to column offerNumber
     */
    override fun getDashboardOfferOfferEvents(offerNumber: String)
            : Single<Response<DashboardOfferHistory>> =
            apiTradeService.getDashboardOfferOfferEvents(offerNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

}