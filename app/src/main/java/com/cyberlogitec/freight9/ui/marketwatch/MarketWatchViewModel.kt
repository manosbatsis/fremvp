package com.cyberlogitec.freight9.ui.marketwatch

import android.content.Context
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.ConstantTradeOffer
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketWatchViewModel(context: Context) : BaseViewModel(context), MarketWatchInputs, MarketWatchOutputs  {
    val inPuts: MarketWatchInputs = this


    private val refreshFilter = PublishSubject.create<Parameter>()
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val clickToCarrierFilter = PublishSubject.create<Parameter>()
    private val clickToContainerFilter = PublishSubject.create<Parameter>()
    private val clickToPpdFilter = PublishSubject.create<Parameter>()
    private val clickBuyOfferItem = PublishSubject.create<BorList>()
    private val clickSellOfferItem = PublishSubject.create<BorList>()
    private val swipeToOfferDetail = PublishSubject.create<Long>()
    private val swipeToOfferRoute = PublishSubject.create<Long>()
    private val requestOfferDetail = PublishSubject.create<BorList>()
    private val storeRouteFilter = PublishSubject.create<MarketRoute>()
    private val loadRouteFilter = PublishSubject.create<Parameter>()
    private val requestChartList = PublishSubject.create<PostMarketWatchChartRequest>()
    private val storeChartSetting = PublishSubject.create<Chart>()
    private val loadChartSetting = PublishSubject.create<String>()
    private val changeBaseWeek = PublishSubject.create<String>()
    private val requestWeekDetailChartList = PublishSubject.create<PostMarketWatchProductWeekDetailChartListRequest>()
    private val requestWeekDealHistory = PublishSubject.create<PostMarketWatchProductWeekDetailChartListRequest>()
    private val requestBid = PublishSubject.create<PostMarketWatchProductWeekDetailChartListRequest>()
    private val requestAsk = PublishSubject.create<PostMarketWatchProductWeekDetailChartListRequest>()

    val outPuts: MarketWatchOutputs = this


    private val gotoMenu = PublishSubject.create<Parameter>()
    private val gotoCarrierFilter = PublishSubject.create<Parameter>()
    private val gotoContainerFilter = PublishSubject.create<Parameter>()
    private val gotoPpdFilter = PublishSubject.create<Parameter>()
    private val gotoSellOrder = PublishSubject.create<BorList>()
    private val gotoBuyOrder = PublishSubject.create<BorList>()
    private val viewSplitPopupDetail = PublishSubject.create<Long>()
    private val viewSplitPopupRoute = PublishSubject.create<Long>()
    private val refreshCarrierFilter = PublishSubject.create<List<Carrier>>()
    private val refreshContainerFilter = PublishSubject.create<List<Container>>()
    private val refreshPaymentFilter = PublishSubject.create<List<Payment>>()
    val refreshSplitPopupDeatil = PublishSubject.create<Bor>()
    private val onSuccessLoadRouteFilter = PublishSubject.create<MarketRoute>()
    private val onSuccessRequestOfferDetail = PublishSubject.create<Bor>()
    private val onSuccessRequestOfferRoute = PublishSubject.create<Bor>()
    private val onSuccessRequestChartList = PublishSubject.create<MarketWatchChartList>()
    private val onSuccessLoadChartSetting = PublishSubject.create<Chart>()
    private val onFailLoadChartSetting = PublishSubject.create<Throwable>()
    private val onFailRequest = PublishSubject.create<Parameter>()
    private val refreshToBaseweek = PublishSubject.create<String>()
    val onSuccessRequestWeekChartList = PublishSubject.create<MarketWatchProductWeekDetailChartList>()
    private val onSuccessRequestWeekDealHistory = PublishSubject.create<MarketWatchDealHistory>()
    private val onSuccessRequestWeekBid = PublishSubject.create<List<BorList>>()
    private val onSuccessRequestWeekAsk = PublishSubject.create<List<BorList>>()

    private var bor: Bor = Bor()


    init {
        val refreshConatinerFilter = PublishSubject.create<Parameter>()
        val refreshPaymentFilter = PublishSubject.create<Parameter>()

        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        clickToCarrierFilter.bindToLifeCycle()
                .subscribe(gotoCarrierFilter)

        clickToContainerFilter.bindToLifeCycle()
                .subscribe(gotoContainerFilter)

        clickToPpdFilter.bindToLifeCycle()
                .subscribe(gotoPpdFilter)

        clickBuyOfferItem.bindToLifeCycle()
                .subscribe(gotoSellOrder)
        clickSellOfferItem.bindToLifeCycle()
                .subscribe(gotoBuyOrder)

        swipeToOfferDetail.bindToLifeCycle()
                .subscribe(viewSplitPopupDetail)
        swipeToOfferRoute.bindToLifeCycle()
                .subscribe(viewSplitPopupRoute)

        //input을 등록한다
        /*requestCarrierFilter.bindToLifeCycle()
                .subscribe()*/

        refreshFilter.flatMap{ enviorment.carrierRepository.getCarriersFromDb() }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribeWith (refreshCarrierFilter)
                .map { Parameter.EVENT}
                .bindToLifeCycle()
                .subscribe (refreshConatinerFilter)


        refreshConatinerFilter.flatMap{ enviorment.containerRepository.getContainersFromDb() }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribeWith (refreshContainerFilter)
                .map { Parameter.EVENT }
                .bindToLifeCycle()
                .subscribe(refreshPaymentFilter)

        refreshPaymentFilter.flatMap{ enviorment.paymentRepository.getPaymentsFromDb() }
                .filter { it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe (this.refreshPaymentFilter)

        storeRouteFilter.bindToLifeCycle()
                .subscribe {
                    enviorment.marketRouteFilterRepository.storeMarketRouteInDb(it)
                }

        loadRouteFilter.flatMapMaybe { enviorment.marketRouteFilterRepository.getMarketRouteFromDb().doOnError {
            onSuccessLoadRouteFilter.onComplete() }.neverError()
        }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessLoadRouteFilter.onNext(it)

                }

        storeChartSetting.bindToLifeCycle()
                .subscribe {
                    enviorment.marketChartSettingRepository.storeMarketChartSettingInDb(it)
                    onSuccessLoadChartSetting.onNext(it)
                }

        loadChartSetting.flatMapMaybe {
            enviorment.marketChartSettingRepository.getMarketChartSettingFromDb(it).doOnError {
                onFailLoadChartSetting.onNext(Throwable("FAIL"))
            }.neverError()
        }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessLoadChartSetting.onNext(it)
                }


        //request market watch chart data list
        requestChartList.flatMapMaybe{
            //it.dataOwnrPtrId = enviorment.currentUser.getCrcyCd()
            enviorment.apiStatClient.postMarketWatchChartList(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestChartList.onNext(it)
                }
        changeBaseWeek
                .bindToLifeCycle()
                .subscribe{
                    refreshToBaseweek.onNext(it)
                }

        requestWeekDetailChartList.flatMapMaybe{
            enviorment.apiStatClient.postMarketWatchWeekDetail(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestWeekChartList.onNext(it)
                }
        requestWeekDealHistory.flatMapMaybe{
            enviorment.apiStatClient.postMarketWatchWeekDealHistory(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestWeekDealHistory.onNext(it)
                }
        requestBid.flatMapMaybe{
            it.offerTypeCode = ConstantTradeOffer.OFFER_TYPE_CODE_BUY
            enviorment.apiTradeClient.postMarketWatchBidAsk(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestWeekBid.onNext(it)
                }

        requestAsk.flatMapMaybe{
            it.offerTypeCode = ConstantTradeOffer.OFFER_TYPE_CODE_SELL
            enviorment.apiTradeClient.postMarketWatchBidAsk(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestWeekAsk.onNext(it)
                }

        requestOfferDetail.flatMapMaybe {
//            bor.detailList = OrderTradeOfferDetail()
            bor.routeList = ArrayList()
            bor.item = it
            //todo 임시로 넣어둠
            bor.item.ownerCompanyCode = ""

            refreshSplitPopupDeatil.onNext(bor)
            enviorment.apiTradeClient.getTradeOfferDetailTarget(it.referenceOfferNumber!!, it.referenceOfferChangeSeq!!).neverError() }
//                .filter {it.isNotEmpty()}
                .bindToLifeCycle()
                .subscribe {
                    if(it.isSuccessful) {
                        bor.detailList = it.body()!!
                        bor.item.cryrCd?.let { it1 ->
                            enviorment.carrierRepository.getCarrierName(it1)
                                    .neverError()
                                    .subscribe{it2 ->
                                        bor.item.cryrName =it2
                                    } }
                        bor.item.carrierCount = bor.item.carrierCount?.minus(1)
                        bor.item.locPolCnt = bor.item.locPolCnt?.minus(1)
                        bor.item.locPodCnt = bor.item.locPodCnt?.minus(1)
                        onSuccessRequestOfferDetail.onNext(bor)
                    }

                }
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    override fun refreshFilter(parameter: Parameter) = refreshFilter.onNext(parameter)
    override fun clickToMenu(parameter: Parameter) = clickToMenu.onNext(parameter)
    override fun gotoMenu() : Observable<Parameter> = gotoMenu
    override fun clickToCarrierFilter(parameter: Parameter) = clickToCarrierFilter.onNext(parameter)
    override fun clickToContainerFilter(parameter: Parameter) = clickToContainerFilter.onNext(parameter)
    override fun clickToPpdFilter(parameter: Parameter) = clickToPpdFilter.onNext(parameter)
    override fun clickBuyOfferItem(item: BorList) = clickBuyOfferItem.onNext(item)
    override fun clickSellOfferItem(item: BorList) = clickSellOfferItem.onNext(item)
    override fun swipeToOfferDetail(id: Long) = swipeToOfferDetail.onNext(id)
    override fun swipeToOfferRoute(id: Long) = swipeToOfferRoute.onNext(id)
    override fun requestOfferDetail(item: BorList) = requestOfferDetail.onNext(item)
    override fun storeRouteFilter(route: MarketRoute) = storeRouteFilter.onNext(route)
    override fun requestChartList(item: PostMarketWatchChartRequest) = requestChartList.onNext(item)
    override fun storeChartSetting(chart: Chart) = storeChartSetting.onNext(chart)
    override fun loadRouteFilter(parameter: Parameter) = loadRouteFilter.onNext(parameter)
    override fun loadChartSetting(simpleName: String) = loadChartSetting.onNext(simpleName)
    override fun changeBaseWeek(baseWeek: String) = changeBaseWeek.onNext(baseWeek)
    override fun requestWeekDetailChartList(item: PostMarketWatchProductWeekDetailChartListRequest) = requestWeekDetailChartList .onNext(item)
    override fun requestWeekDealHistory(item: PostMarketWatchProductWeekDetailChartListRequest) = requestWeekDealHistory.onNext(item)
    override fun requestBid(item: PostMarketWatchProductWeekDetailChartListRequest) = requestBid.onNext(item)
    override fun requestAsk(item: PostMarketWatchProductWeekDetailChartListRequest) = requestAsk.onNext(item)

    override fun gotoCarrierFilter(): Observable<Parameter> = gotoCarrierFilter
    override fun gotoContainerFilter(): Observable<Parameter> = gotoContainerFilter
    override fun gotoPpdFilter(): Observable<Parameter> = gotoPpdFilter
    override fun gotoSellOrder(): Observable<BorList> = gotoSellOrder
    override fun gotoBuyOrder(): Observable<BorList> = gotoBuyOrder
    override fun viewSplitPopupDetail(): Observable<Long> = viewSplitPopupDetail
    override fun viewSplitPopupRoute(): Observable<Long> = viewSplitPopupRoute
    override fun refreshCarrierFilter(): Observable<List<Carrier>> = refreshCarrierFilter
    override fun refreshContainerFilter(): Observable<List<Container>> = refreshContainerFilter
    override fun refreshPaymentFilter(): Observable<List<Payment>> = refreshPaymentFilter
    override fun refreshSplitPopupDeatil(): Observable<Bor> = refreshSplitPopupDeatil
    override fun onSuccessRequestOfferDetail(): Observable<Bor> = onSuccessRequestOfferDetail
    override fun onSuccessRequestOfferRoute(): Observable<Bor> = onSuccessRequestOfferRoute
    override fun onSuccessLoadRouteFilter(): Observable<MarketRoute> = onSuccessLoadRouteFilter
    override fun onSuccessRequestChartList(): Observable<MarketWatchChartList> = onSuccessRequestChartList
    override fun onSuccessLoadChartSetting(): Observable<Chart> = onSuccessLoadChartSetting
    override fun onFailLoadChartSetting(): Observable<Throwable> = onFailLoadChartSetting
    override fun onFailRequest(): Observable<Parameter> = onFailRequest
    override fun refreshToBaseweek(): Observable<String> = refreshToBaseweek
    override fun onSuccessRequestWeekChartList(): Observable<MarketWatchProductWeekDetailChartList> = onSuccessRequestWeekChartList
    override fun onSuccessRequestWeekDealHistory(): Observable<MarketWatchDealHistory>  = onSuccessRequestWeekDealHistory
    override fun onSuccessRequestWeekBid(): Observable<List<BorList>>  = onSuccessRequestWeekBid
    override fun onSuccessRequestWeekAsk(): Observable<List<BorList>>  = onSuccessRequestWeekAsk

}

interface MarketWatchInputs {
    fun refreshFilter(parameter: Parameter)
    fun clickToMenu(parameter: Parameter)
    fun clickToCarrierFilter(parameter: Parameter)
    fun clickToContainerFilter(parameter: Parameter)
    fun clickToPpdFilter(parameter: Parameter)
    fun clickBuyOfferItem(item: BorList)
    fun clickSellOfferItem(item: BorList)
    fun swipeToOfferDetail(id: Long)
    fun swipeToOfferRoute(id: Long)
    fun requestOfferDetail(item: BorList)
    fun loadRouteFilter(parameter: Parameter)
    fun storeRouteFilter(route: MarketRoute)
    fun requestChartList(item: PostMarketWatchChartRequest)
    fun storeChartSetting(chart: Chart)
    fun loadChartSetting(simpleName: String)
    fun changeBaseWeek(baseWeek: String)
    fun requestWeekDetailChartList(item: PostMarketWatchProductWeekDetailChartListRequest)
    fun requestWeekDealHistory(item: PostMarketWatchProductWeekDetailChartListRequest)
    fun requestBid(item: PostMarketWatchProductWeekDetailChartListRequest)
    fun requestAsk(item: PostMarketWatchProductWeekDetailChartListRequest)
}

interface MarketWatchOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun gotoCarrierFilter(): Observable<Parameter>
    fun gotoContainerFilter() : Observable<Parameter>
    fun gotoPpdFilter(): Observable<Parameter>
    fun gotoSellOrder(): Observable<BorList>
    fun gotoBuyOrder(): Observable<BorList>
    fun viewSplitPopupDetail(): Observable<Long>
    fun viewSplitPopupRoute(): Observable<Long>
    fun refreshCarrierFilter(): Observable<List<Carrier>>
    fun refreshContainerFilter(): Observable<List<Container>>
    fun refreshPaymentFilter(): Observable<List<Payment>>
    fun refreshSplitPopupDeatil(): Observable<Bor>
    fun onSuccessRequestOfferDetail(): Observable<Bor>
    fun onSuccessRequestOfferRoute(): Observable<Bor>
    fun onSuccessLoadRouteFilter(): Observable<MarketRoute>
    fun onSuccessRequestChartList(): Observable<MarketWatchChartList>
    fun onSuccessLoadChartSetting(): Observable<Chart>
    fun onFailLoadChartSetting(): Observable<Throwable>
    fun onFailRequest(): Observable<Parameter>
    fun refreshToBaseweek(): Observable<String>
    fun onSuccessRequestWeekChartList(): Observable<MarketWatchProductWeekDetailChartList>
    fun onSuccessRequestWeekDealHistory(): Observable<MarketWatchDealHistory>
    fun onSuccessRequestWeekBid(): Observable<List<BorList>>
    fun onSuccessRequestWeekAsk(): Observable<List<BorList>>
}