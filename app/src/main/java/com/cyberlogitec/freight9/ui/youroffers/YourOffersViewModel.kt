package com.cyberlogitec.freight9.ui.youroffers

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.R
import com.cyberlogitec.freight9.common.BaseActivity
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.config.Constant.EmptyString
import com.cyberlogitec.freight9.config.ConstantTradeOffer.OFFER_TYPE_CODE_BUY
import com.cyberlogitec.freight9.config.FilterOnMarket.FILTER_CLOSED
import com.cyberlogitec.freight9.config.FilterOnMarket.FILTER_ONMARKET
import com.cyberlogitec.freight9.lib.apistat.PostDashboardRouteListRequest
import com.cyberlogitec.freight9.lib.apistat.PostDashboardSummaryListRequest
import com.cyberlogitec.freight9.lib.apistat.PostDashboardWeekListRequest
import com.cyberlogitec.freight9.lib.model.Dashboard
import com.cyberlogitec.freight9.lib.model.DashboardRouteList
import com.cyberlogitec.freight9.lib.model.DashboardWeekList
import com.cyberlogitec.freight9.lib.rx.*
import com.cyberlogitec.freight9.lib.util.Intents
import com.cyberlogitec.freight9.ui.inventory.RouteFilterPopup
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class YourOffersViewModel(context: Context) : BaseViewModel(context),
        YourOffersInputs, YourOffersOutputs  {

    val inPuts: YourOffersInputs = this
    private val injectIntent = PublishSubject.create<Intent>()

    // Load from Api
    private val requestOffersSummaryByApi = PublishSubject.create<YourOffersActivity.FilterCondition>()
    private val requestOffersFilter = PublishSubject.create<YourOffersActivity.FilterCondition>()
    private val requestRouteData = PublishSubject.create<Triple<RouteFilterPopup.RouteFromTo, String, String>>()
    private val requestWeekListByApi = PublishSubject.create<PostDashboardWeekListRequest>()
    private val requestRouteListByApi = PublishSubject.create<PostDashboardRouteListRequest>()

    private val receivedOffersSummary = BehaviorSubject.create<Dashboard>()
    private val receivedOffersRouteList = BehaviorSubject.create<DashboardRouteList>()

    val outPuts: YourOffersOutputs = this
    private val onSuccessRefresh = PublishSubject.create<String>()
    private val onSuccessWeekList = PublishSubject.create<List<String>>()
    private val onSuccessRequestOffersSummary = BehaviorSubject.create<Pair<String, Dashboard>>()
    private val onSuccessOffersFilter
            = BehaviorSubject.create<Triple<YourOffersActivity.FilterCondition, List<Dashboard.Cell>, Pair<Int, Int>>>()
    private val onSuccessRouteData = PublishSubject.create<Pair<RouteFilterPopup.RouteFromTo, List<RouteFilterPopup.RouteAdapterData>>>()

    // intents
    private val parentId = BehaviorSubject.create<String>()
    private val refresh = PublishSubject.create<Parameter>()

    init {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // intent
        intent.map { it.getSerializableExtra(Intents.YOUR_OFFER_TYPE) as String }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .subscribe(parentId)

        parentId.compose<String> { refresh.withLatestFrom(it, BiFunction { _, t2 -> t2 }) }
                .filter { listOf(it).isNotEmpty() }
                .bindToLifeCycle()
                .map { Timber.d("f9: yourOfferType is $it"); it }
                .subscribe(onSuccessRefresh)

        // Buy Offers or Sell Offers From MENU
        injectIntent.bindToLifeCycle()
                .subscribe(intent)

        /**
         * Get Offers Summary From Call Api
         */
        parentId.compose<Pair<YourOffersActivity.FilterCondition, PostDashboardSummaryListRequest>> {
            requestOffersSummaryByApi
                .withLatestFrom(it, BiFunction { filterCondition, offerType ->
                    val postDashboardSummaryListRequest = PostDashboardSummaryListRequest(
                            enviorment.currentUser.getUsrId(),
                            offerType,
                            if (filterCondition.polCodeIsAll) EmptyString else filterCondition.polCode,
                            if (filterCondition.podCodeIsAll) EmptyString else filterCondition.podCode,
                            if (filterCondition.selectedWeek.isEmpty()) {
                                EmptyString
                            } else {
                                filterCondition.selectedWeek
                            }
                    )
                    Pair(filterCondition, postDashboardSummaryListRequest)
                }) }
                .map { showLoadingDialog.onNext(Parameter.EVENT); it }
                .flatMapMaybe { pair ->
                    val postDashboardSummaryListRequest = pair.second
                    enviorment.apiTradeClient.postDashboardSummaryList(postDashboardSummaryListRequest)
                            .handleToError(hideLoadingDialog)
                            .neverError()
                            .doOnError { throwable ->
                                error.onNext(throwable)
                            }
                            .map { response ->
                                Pair(pair, response)
                            }
                }
                .map { pair ->
                    // Call api - WeekList, RouteList : 최초 init 일 때에만
                    val filterCondition = pair.first.first
                    val postDashboardSummaryListRequest = pair.first.second

                    // Route 가 변경된 후 Summary 요청 시 WeekList 도 요청
                    if (filterCondition.isRouteChanged) {
                        // second : postDashboardSummaryListRequest
                        requestWeekListByApi(PostDashboardWeekListRequest(
                                userId = postDashboardSummaryListRequest.userId,
                                offerTypeCode = postDashboardSummaryListRequest.offerTypeCode,
                                polCode = postDashboardSummaryListRequest.polCode,
                                podCode = postDashboardSummaryListRequest.podCode
                        ))
                    }

                    // 최초 1회만 RouteList 요청
                    if (!filterCondition.isInitRequested) {
                        requestRouteListByApi(PostDashboardRouteListRequest(
                                postDashboardSummaryListRequest.userId,
                                postDashboardSummaryListRequest.offerTypeCode
                        ))
                    }
                    pair
                }
                .map { pair ->
                    hideLoadingDialog.onNext( Throwable("OK"))
                    pair
                }
                .bindToLifeCycle()
                .subscribe { pair ->
                    // Pair(Pair(filterCondition, postDashboardSummaryListRequest), response)
                    val postDashboardSummaryListRequest = pair.first.second
                    val response = pair.second
                    val offerType = postDashboardSummaryListRequest.offerTypeCode ?: OFFER_TYPE_CODE_BUY
                    if (response.isSuccessful) {
                        val dashBoard = (response.body() as Dashboard)
                        onSuccessRequestOffersSummary.onNext(Pair(offerType, dashBoard))

                        // List<Dashboard> 만 BehaviorSubject 에 할당 (Filter 에서 사용)
                        receivedOffersSummary.onNext(dashBoard)

                        // OnMarket, Closed 로 filtering
                        requestOffersFilter(pair.first.first)
                    } else {
                        error.onNext(Throwable(response.errorBody().toString()))
                    }
                }

        /**
         * Get Offers Week List From Call Api
         */
        requestWeekListByApi
                .flatMapMaybe { postDashboardWeekListRequest ->
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.postDashboardWeekList(postDashboardWeekListRequest)
                            .handleToError(hideLoadingDialog)
                            .neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            if (response.isSuccessful) {
                                val dashboardWeekList = response.body() as DashboardWeekList
                                onSuccessWeekList.onNext(dashboardWeekList.baseYearWeek.sorted())
                            } else {
                                error.onNext(Throwable(response.errorBody().toString()))
                            }
                        }, { throwable ->
                            error.onNext(throwable)
                        }
                )

        /**
         * Get Offers Route List From Call Api
         */
        requestRouteListByApi
                .flatMapMaybe { postDashboardRouteListRequest ->
                    showLoadingDialog.onNext(Parameter.EVENT)
                    enviorment.apiTradeClient.postDashboardRouteList(postDashboardRouteListRequest)
                            .handleToError(hideLoadingDialog)
                            .neverError()
                }
                .map { hideLoadingDialog.onNext( Throwable("OK")); it }
                .bindToLifeCycle()
                .subscribe (
                        { response ->
                            if (response.isSuccessful) {
                                receivedOffersRouteList.onNext(response.body() as DashboardRouteList)
                            } else {
                                error.onNext(Throwable(response.errorBody().toString()))
                                }
                            }, { throwable ->
                                error.onNext(throwable)
                            }
                )


        /**
         * Filtering only By On Market/Closed
         * receivedOfferSummary : received dashboards from Rest
         * requestOfferFilter : Filter conditions
         */
        receivedOffersSummary
                .compose<Triple<YourOffersActivity.FilterCondition, List<Dashboard.Cell>, Pair<Int, Int>>> {
                    requestOffersFilter
                        // t1 : FilterCondition, t2 : List<Dashboard>
                        .withLatestFrom(it, BiFunction { filterCondition, dashBoard ->

                            //----------------------------------------------------------------------
                            // On Market, Closed 에 표시된 count
                            val onMarketCount = dashBoard.cell.count { it.offerStatus ==  FILTER_ONMARKET }
                            val closedCount = dashBoard.cell.count { it.offerStatus ==  FILTER_CLOSED }

                            //----------------------------------------------------------------------
                            // On Market, Closed 에 대한 filter
                            val filteredCellList = dashBoard.cell.filter { cell ->
                                if (filterCondition.isOnMarketList) {
                                    // offerStatus == "1"
                                    cell.offerStatus == FILTER_ONMARKET
                                } else {
                                    // offerStatus == "0"
                                    cell.offerStatus == FILTER_CLOSED
                                }
                            }

                            //----------------------------------------------------------------------
                            Triple(filterCondition,
                                    filteredCellList.sortedByDescending { it.eventTimestamp },
                                    Pair(onMarketCount, closedCount))
                        })
                }
                .bindToLifeCycle()
                .subscribe(onSuccessOffersFilter)

        /**
         * compose<R> - R : return type
         * conditions - requestRouteData 로 전달받은 값
         * dashboardList - onSuccessOffersFilter 에 있는 Behavior 값
         */
        receivedOffersRouteList
                .compose<Pair<RouteFilterPopup.RouteFromTo, List<RouteFilterPopup.RouteAdapterData>>> {
                    requestRouteData
                        .withLatestFrom(it, BiFunction { conditions, dashBoardRouteList ->

                            var routeAdapterDatas = mutableListOf<RouteFilterPopup.RouteAdapterData>()
                            val routeFromTo = conditions.first
                            var polCode = conditions.second
                            var podCode = conditions.third
                            val routeList = dashBoardRouteList.rteList

                            // From, to 중 어떤겄이 채워져 있는지 체크되어야 함.
                            when (routeFromTo) {
                                RouteFilterPopup.RouteFromTo.FROM -> {

                                    // 첫줄에 pol : ALL 추가
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            -1,
                                            RouteFilterPopup.PortKind.ALL,
                                            EmptyString,
                                            EmptyString,
                                            -1,
                                            RouteFilterPopup.PortKind.NONE,
                                            EmptyString,
                                            EmptyString
                                    ))

                                    // "To" check
                                    if ((podCode.compareTo(context.getString(R.string.all), true) == 0)
                                            || (podCode.compareTo(context.getString(R.string.to), true) == 0)){
                                        podCode = EmptyString
                                    }

                                    // true : Pod 가 선택되어 있지 않은 경우, false : Pod 가 선택되어 있는 경우
                                    val noPodFilter = podCode.isEmpty()

                                    for ((index, route) in routeList.withIndex()) {
                                        // pod 가 선택되어 있지 않은 경우
                                        if (noPodFilter) {
                                            // Pol 전부
                                            routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                                    index,
                                                    RouteFilterPopup.PortKind.POL,
                                                    route.polCode,
                                                    route.polName,
                                                    -1,
                                                    RouteFilterPopup.PortKind.NONE,
                                                    EmptyString,
                                                    EmptyString
                                            ))
                                        } else {
                                            // pod 가 선택되어 있는 경우 Pod 가 같은 pol 항목만
                                            when (route.podCode) {
                                                podCode -> {
                                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                                            index,
                                                            RouteFilterPopup.PortKind.POL,
                                                            route.polCode,
                                                            route.polName,
                                                            -1,
                                                            RouteFilterPopup.PortKind.NONE,
                                                            EmptyString,
                                                            EmptyString
                                                    ))
                                                }
                                                else -> {}
                                            }
                                        }
                                    }

                                    routeAdapterDatas = routeAdapterDatas
                                            .distinctBy { it.polOrPorPortCode }
                                            .toMutableList()
                                }

                                RouteFilterPopup.RouteFromTo.ALL -> {

                                    // 첫줄에 pol : ALL, pod : ALL 추가
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            -1,
                                            RouteFilterPopup.PortKind.ALL,
                                            EmptyString,
                                            EmptyString,
                                            -1,
                                            RouteFilterPopup.PortKind.ALL,
                                            EmptyString,
                                            EmptyString
                                    ))

                                    // 모든 pol, pod 추가
                                    for ((index, route) in routeList.withIndex()) {
                                        routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                                index,
                                                RouteFilterPopup.PortKind.POL,
                                                route.polCode,
                                                route.polName,
                                                index,
                                                RouteFilterPopup.PortKind.POD,
                                                route.podCode,
                                                route.podName
                                        ))
                                    }

                                    routeAdapterDatas = routeAdapterDatas
                                            .distinctBy { Pair(it.polOrPorPortCode, it.podOrDelPortCode) }
                                            .toMutableList()
                                }

                                RouteFilterPopup.RouteFromTo.TO -> {

                                    // 첫줄에 pod : ALL 추가
                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                            -1,
                                            RouteFilterPopup.PortKind.NONE,
                                            EmptyString,
                                            EmptyString,
                                            -1,
                                            RouteFilterPopup.PortKind.ALL,
                                            EmptyString,
                                            EmptyString
                                    ))

                                    // "From" check
                                    if ((polCode.compareTo(context.getString(R.string.all), true) == 0)
                                            || (polCode.compareTo(context.getString(R.string.from), true) == 0)){
                                        polCode = EmptyString
                                    }

                                    // true : Pol 이 선택되어 있지 않은 경우, false : Pol 이 선택되어 있는 경우
                                    val noPolFilter = polCode.isEmpty()

                                    for ((index, route) in routeList.withIndex()) {
                                        // pol 이 선택되어 있지 않은 경우
                                        if (noPolFilter) {
                                            // Pod 전부
                                            routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                                    -1,
                                                    RouteFilterPopup.PortKind.NONE,
                                                    EmptyString,
                                                    EmptyString,
                                                    index,
                                                    RouteFilterPopup.PortKind.POD,
                                                    route.podCode,
                                                    route.podName))
                                        } else {
                                            // pol 이 선택되어 있는 경우 Pol 이 같은 pod 항목만
                                            when (route.polCode) {
                                                polCode -> {
                                                    routeAdapterDatas.add(RouteFilterPopup.RouteAdapterData(
                                                            -1,
                                                            RouteFilterPopup.PortKind.NONE,
                                                            EmptyString,
                                                            EmptyString,
                                                            index,
                                                            RouteFilterPopup.PortKind.POD,
                                                            route.podCode,
                                                            route.podName))
                                                }
                                                else -> {}
                                            }
                                        }
                                    }

                                    routeAdapterDatas = routeAdapterDatas
                                            .distinctBy { it.podOrDelPortCode }
                                            .toMutableList()
                                }
                                else -> { }
                            }
                            Pair(routeFromTo, routeAdapterDatas.toList())
                        })
                }
                .bindToLifeCycle()
                .subscribe(onSuccessRouteData)
    }

    override fun onResume(view: BaseActivity<BaseViewModel>) {
        super.onResume(view)

        Timber.v("f9: onResume")
        refresh.onNext(Parameter.EVENT)
    }

    // interface : in
    override fun injectIntent(intent: Intent) = injectIntent.onNext(intent)
    override fun requestOffersSummaryByApi(filterCondition: YourOffersActivity.FilterCondition)
            = requestOffersSummaryByApi.onNext(filterCondition)
    override fun requestOffersFilter(filterCondition: YourOffersActivity.FilterCondition)
            = requestOffersFilter.onNext(filterCondition)
    override fun requestRouteData(conditions: Triple<RouteFilterPopup.RouteFromTo, String, String>)
            = requestRouteData.onNext(conditions)
    override fun requestWeekListByApi(postDashboardWeekListRequest: PostDashboardWeekListRequest)
            = requestWeekListByApi.onNext(postDashboardWeekListRequest)
    override fun requestRouteListByApi(postDashboardRouteListRequest: PostDashboardRouteListRequest)
            = requestRouteListByApi.onNext(postDashboardRouteListRequest)

    override fun receivedOffersSummary(dashBoard: Dashboard) = receivedOffersSummary.onNext(dashBoard)
    override fun receivedOffersRouteList(dashboardRouteList: DashboardRouteList) = receivedOffersRouteList.onNext(dashboardRouteList)

    // interface : out
    override fun onSuccessRefresh(): Observable<String> = onSuccessRefresh
    override fun onSuccessWeekList(): Observable<List<String>> = onSuccessWeekList
    override fun onSuccessRequestOffersSummary() = onSuccessRequestOffersSummary
    override fun onSuccessOffersFilter(): Observable<Triple<YourOffersActivity.FilterCondition, List<Dashboard.Cell>, Pair<Int, Int>>>
            = onSuccessOffersFilter
    override fun onSuccessRouteData(): Observable<Pair<RouteFilterPopup.RouteFromTo, List<RouteFilterPopup.RouteAdapterData>>>
            = onSuccessRouteData
}

interface YourOffersInputs {
    fun injectIntent(intent: Intent)

    fun requestOffersSummaryByApi(filterCondition: YourOffersActivity.FilterCondition)
    fun requestOffersFilter(filterCondition: YourOffersActivity.FilterCondition)
    fun requestRouteData(conditions: Triple<RouteFilterPopup.RouteFromTo, String, String>)
    fun requestWeekListByApi(postDashboardWeekListRequest: PostDashboardWeekListRequest)
    fun requestRouteListByApi(postDashboardRouteListRequest: PostDashboardRouteListRequest)

    fun receivedOffersSummary(dashBoard: Dashboard)
    fun receivedOffersRouteList(dashboardRouteList: DashboardRouteList)
}

interface YourOffersOutputs {
    fun onSuccessRefresh(): Observable<String>
    fun onSuccessWeekList(): Observable<List<String>>
    fun onSuccessRequestOffersSummary(): Observable<Pair<String, Dashboard>>
    fun onSuccessOffersFilter(): Observable<Triple<YourOffersActivity.FilterCondition, List<Dashboard.Cell>, Pair<Int, Int>>>
    fun onSuccessRouteData(): Observable<Pair<RouteFilterPopup.RouteFromTo, List<RouteFilterPopup.RouteAdapterData>>>
}