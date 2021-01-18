package com.cyberlogitec.freight9.ui.trademarket

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.apitrade.PostMarketOfferListRequest
import com.cyberlogitec.freight9.lib.model.*
import com.cyberlogitec.freight9.lib.rx.Parameter
import com.cyberlogitec.freight9.lib.rx.neverError
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MarketViewModel(context: Context) : BaseViewModel(context), MarketInputs, MarketOutputs  {
    val inPuts: MarketInputs = this


    private val refreshFilter = PublishSubject.create<Parameter>()
    private val clickToMenu = PublishSubject.create<Parameter>()
    private val clickToMenuMessageBox = PublishSubject.create<Intent>()
    private val clickToCarrierFilter = PublishSubject.create<Parameter>()
    private val clickToContainerFilter = PublishSubject.create<Parameter>()
    private val clickToPpdFilter = PublishSubject.create<Parameter>()
    private val clickBuyOfferItem = PublishSubject.create<BorList>()
    private val clickSellOfferItem = PublishSubject.create<BorList>()
    private val swipeToOfferDetail = PublishSubject.create<Long>()
    private val swipeToOfferRoute = PublishSubject.create<Long>()
    private val requestMarketOfferList = PublishSubject.create<PostMarketOfferListRequest>()
    private val requestOrderLists = PublishSubject.create<BorList>()
    private val requestOfferDetail = PublishSubject.create<BorList>()
    private val requestOfferRoute = PublishSubject.create<Bor>()
    private val storeRouteFilter = PublishSubject.create<MarketRoute>()
    private val loadRouteFilter = PublishSubject.create<Parameter>()
    val outPuts: MarketOutputs = this


    private val gotoMenu = PublishSubject.create<Parameter>()
    private val gotoMenuMessageBox = PublishSubject.create<Intent>()
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
    private val refreshSplitPopupDeatil = PublishSubject.create<Bor>()
    private val onSuccessRequestSellOfferLists = PublishSubject.create<List<BorList>>()
    private val onSuccessRequestBuyOfferLists = PublishSubject.create<List<BorList>>()
    private val onSuccessRequestOfferDetail = PublishSubject.create<Bor>()
    private val onSuccessRequestOfferRoute = PublishSubject.create<Bor>()
    private val onSuccessLoadRouteFilter = PublishSubject.create<MarketRoute>()
    private val onSuccessRequestMarketOfferList = PublishSubject.create<MarketChartOfferList>()
    private val onRefrshOfferLists = PublishSubject.create<Parameter>()
    private val onFailRequest = PublishSubject.create<Parameter>()

    private var bor: Bor = Bor()


    init {
        val refreshConatinerFilter = PublishSubject.create<Parameter>()
        val refreshPaymentFilter = PublishSubject.create<Parameter>()

        clickToMenu.bindToLifeCycle()
                .subscribe(gotoMenu)

        clickToMenuMessageBox.bindToLifeCycle()
                .subscribe(gotoMenuMessageBox)

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

        requestOrderLists.flatMapMaybe {
            onRefrshOfferLists.onNext(Parameter.EVENT)
            enviorment.apiTradeClient.getOrderList(it).neverError() }
                .filter {it.isNotEmpty() }
                .bindToLifeCycle()
                .subscribe { onSuccessRequestBuyOfferLists.onNext(it) }
        requestOfferDetail.flatMapMaybe {
//            bor.detailList = OrderTradeOfferDetail()
            bor.routeList = ArrayList()
            bor.item = it

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
                        onSuccessRequestOfferDetail.onNext(bor)
                    }

                }
        requestOfferRoute.flatMapMaybe {
            enviorment.apiTradeClient.postServiceRoute(it.getServiceRouteReques()).neverError() }
                .filter {it.isNotEmpty()}
                .bindToLifeCycle()
                .subscribe {
                    bor.routeList = it
                    onSuccessRequestOfferRoute.onNext(bor)
                }
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

        requestMarketOfferList.flatMapMaybe{
            //it.dataOwnrPtrId = enviorment.currentUser.getCrcyCd()
            enviorment.apiTradeClient.postMarketOfferList(it).neverError() }
                .bindToLifeCycle()
                .subscribe {
                    onSuccessRequestMarketOfferList.onNext(it)
                }
    }


    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
    }

    override fun refreshFilter(parameter: Parameter) {
        Timber.v("f9: refresh_filter(${parameter})")
        return refreshFilter.onNext(parameter)
    }
    override fun clickToMenu(parameter: Parameter) {
        Timber.v("f9: clickToMenu(${parameter})")
        return clickToMenu.onNext(parameter)
    }

    override fun gotoMenu() : Observable<Parameter> {
        Timber.v("f9: clickToMenu")
        return gotoMenu
    }

    override fun clickToMenuMessageBox(intent: Intent) {
        Timber.v("f9: clickToMenuMessageBox(${intent})")
        return clickToMenuMessageBox.onNext(intent)
    }

    override fun gotoMenuMessageBox() : Observable<Intent> {
        Timber.v("f9: clickToMenuMessageBox")
        return gotoMenuMessageBox
    }

    override fun clickToCarrierFilter(parameter: Parameter) {
        Timber.v("f9: clickToCarrierFilter")
        return clickToCarrierFilter.onNext(parameter)
    }

    override fun clickToContainerFilter(parameter: Parameter) {
        Timber.v("f9: clickToContainerFilter")
        return clickToContainerFilter.onNext(parameter)
    }

    override fun clickToPpdFilter(parameter: Parameter) {
        Timber.v("f9: clickToPpdFilter")
        return clickToPpdFilter.onNext(parameter)
    }
    override fun clickBuyOfferItem(item: BorList) {
        Timber.v("f9: clickBuyOfferItem")
        return clickBuyOfferItem.onNext(item)
    }

    override fun clickSellOfferItem(item: BorList) {
        Timber.v("f9: clickSellOfferItem")
        return clickSellOfferItem.onNext(item)
    }
    override fun swipeToOfferDetail(id: Long) {
        Timber.v("f9: swipeToOfferDetail")
        return swipeToOfferDetail.onNext(id)
    }

    override fun swipeToOfferRoute(id: Long) {
        Timber.v("f9: swipeToOfferRoute")
        return swipeToOfferRoute.onNext(id)
    }
    override fun requestMarketOfferList(item: PostMarketOfferListRequest) {
        Timber.v("f9: requestMarketOfferList")
        return requestMarketOfferList.onNext(item)
    }
    override fun requestOrderLists(item: BorList) {
        Timber.v("f9: requestBuyOfferInfos")
        return requestOrderLists.onNext(item)
    }
    override fun requestOfferDetail(item: BorList) {
        Timber.v("f9: requestOfferDetail")
        return requestOfferDetail.onNext(item)
    }

    override fun requestOfferRoute(bor: Bor) {
        Timber.v("f9: requestOfferRoute")
        return requestOfferRoute.onNext(bor)
    }
    override fun storeRouteFilter(route: MarketRoute) {
        Timber.v("f9: storeRouteFilter")
        return storeRouteFilter.onNext(route)
    }
    override fun loadRouteFilter(parameter: Parameter) {
        Timber.v("f9: loadRouteFilter")
        return loadRouteFilter.onNext(parameter)
    }

    override fun gotoCarrierFilter(): Observable<Parameter> {
        Timber.v("f9: gotoNext")
        return gotoCarrierFilter
    }

    override fun gotoContainerFilter(): Observable<Parameter> {
        Timber.v("f9: gotoContainerFilter")
        return gotoContainerFilter
    }
    override fun gotoPpdFilter(): Observable<Parameter> {
        Timber.v("f9: gotoPpdFilter")
        return gotoPpdFilter
    }
    override fun gotoSellOrder(): Observable<BorList> {
        Timber.v("f9: gotoSellOrder")
        return gotoSellOrder
    }
    override fun gotoBuyOrder(): Observable<BorList> {
        Timber.v("f9: gotoBuyOrder")
        return gotoBuyOrder
    }
    override fun viewSplitPopupDetail(): Observable<Long> {
        Timber.v("f9: viewSplitPopupDetail")
        return viewSplitPopupDetail
    }

    override fun viewSplitPopupRoute(): Observable<Long> {
        Timber.v("f9: viewSplitPopupRoute")
        return viewSplitPopupRoute
    }
    override fun refreshCarrierFilter(): Observable<List<Carrier>> {
        Timber.v("f9: refreshCarrierFilter")
        return refreshCarrierFilter
    }
    override fun refreshContainerFilter(): Observable<List<Container>> {
        Timber.v("f9: refreshContainerFilter")
        return refreshContainerFilter
    }
    override fun refreshPaymentFilter(): Observable<List<Payment>> {
        Timber.v("f9: refreshPaymentFilter")
        return refreshPaymentFilter
    }
    override fun refreshSplitPopupDeatil(): Observable<Bor> {
        Timber.v("f9: refreshSplitPopupDeatil")
        return refreshSplitPopupDeatil
    }

    override fun onSuccessRequestBuyOfferLists(): Observable<List<BorList>> {
        Timber.v("f9: onSuccessRequestBuyOfferLists")
        return onSuccessRequestBuyOfferLists
    }

    override fun onSuccessRequestOfferDetail(): Observable<Bor> {
        Timber.v("f9: onSuccessRequestOfferDetail")
        return onSuccessRequestOfferDetail
    }

    override fun onSuccessRequestSellOfferLists(): Observable<List<BorList>> {
        Timber.v("f9: onSuccessRequestSellOfferLists")
        return onSuccessRequestSellOfferLists
    }

    override fun onSuccessRequestOfferRoute(): Observable<Bor> {
        Timber.v("f9: onSuccessRequestOfferRoute")
        return onSuccessRequestOfferRoute
    }

    override fun onSuccessLoadRouteFilter(): Observable<MarketRoute> {
        Timber.v("f9: onSuccessLoadRouteFilter")
        return onSuccessLoadRouteFilter
    }

    override fun onSuccessRequestMarketOfferList(): Observable<MarketChartOfferList> {
        Timber.v("f9: onSuccessRequestMarketOfferList")
        return onSuccessRequestMarketOfferList
    }

    override fun onRefrshOfferLists(): Observable<Parameter> {
        Timber.v("f9: onRefrshOfferLists")
        return onRefrshOfferLists
    }
    override fun onFailRequest(): Observable<Parameter> {
        return onFailRequest
    }

}

interface MarketInputs {
    fun refreshFilter(parameter: Parameter)
    fun clickToMenu(parameter: Parameter)
    fun clickToMenuMessageBox(intent: Intent)
    fun clickToCarrierFilter(parameter: Parameter)
    fun clickToContainerFilter(parameter: Parameter)
    fun clickToPpdFilter(parameter: Parameter)
    fun clickBuyOfferItem(item: BorList)
    fun clickSellOfferItem(item: BorList)
    fun swipeToOfferDetail(id: Long)
    fun swipeToOfferRoute(id: Long)
    fun requestMarketOfferList(item: PostMarketOfferListRequest)
    fun requestOrderLists(item: BorList)
    fun requestOfferDetail(item: BorList)
    fun requestOfferRoute(bor: Bor)
    fun loadRouteFilter(parameter: Parameter)
    fun storeRouteFilter(route: MarketRoute)
}

interface MarketOutputs {
    fun gotoMenu() : Observable<Parameter>
    fun gotoMenuMessageBox() : Observable<Intent>
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
    fun onSuccessRequestSellOfferLists(): Observable<List<BorList>>
    fun onSuccessRequestBuyOfferLists(): Observable<List<BorList>>
    fun onSuccessRequestOfferDetail(): Observable<Bor>
    fun onSuccessRequestOfferRoute(): Observable<Bor>
    fun onSuccessLoadRouteFilter(): Observable<MarketRoute>
    fun onSuccessRequestMarketOfferList(): Observable<MarketChartOfferList>
    fun onRefrshOfferLists(): Observable<Parameter>
    fun onFailRequest(): Observable<Parameter>

}