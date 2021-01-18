package com.cyberlogitec.freight9.ui.routeselect.search

import android.content.Context
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.db.PortDao
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import me.texy.treeview.TreeNode
import timber.log.Timber

class RouteSearchViewModel(context: Context) : BaseViewModel(context), RouteSearchInputs, RouteSearchOutputs {
    val inPuts: RouteSearchInputs = this
    private val loadFeaturedRoute = PublishSubject.create<Parameter>()
    private val loadPortTree = PublishSubject.create<Parameter>()
    private val checkPortData = PublishSubject.create<Parameter>()
    private val initializePortList = PublishSubject.create<Parameter>()
    private val callShowSearch = PublishSubject.create<Parameter>()
    private val callHideSearch = PublishSubject.create<Parameter>()
    private val callFocusChange = PublishSubject.create<Boolean>()
    private val selectItem = PublishSubject.create<FeaturedRoute>()
    private val updateSelectedDate = PublishSubject.create<String>()
    private val callShowTab = PublishSubject.create<Parameter>()
    private val callSearchPort = PublishSubject.create<String>()
    private val callSearchRoute = PublishSubject.create<String>()
    private val clickPreferredRouteEdit = PublishSubject.create<Parameter>()
    val outPuts: RouteSearchOutputs = this
    private val featuredRoutes = PublishSubject.create<List<FeaturedRoute>>()
    private val portTree = PublishSubject.create<Parameter>()
    private val hasPortData = PublishSubject.create<Boolean>()
    private val doShowSearch = PublishSubject.create<Parameter>()
    private val doHideSearch = PublishSubject.create<Parameter>()
    private val doFocusChange = PublishSubject.create<Boolean>()
    private val doTabExpand = PublishSubject.create<Parameter>()
    private val setRoute = PublishSubject.create<FeaturedRoute>()
    private val doSearchPort = PublishSubject.create<Pair<String, Observable<List<PortDao.PortMinimal>>>>()
    private val doSearchRoute = PublishSubject.create<Pair<String, Observable<List<FeaturedRoute>>>>()
    private val goToPreferredRouteEdit = PublishSubject.create<Parameter>()
    var initPortTree: TreeNode

    init {
//        enviorment.portRepository.loadPortTree()
        initPortTree = enviorment.portRepository.makeTree()
        callShowSearch.bindToLifeCycle().subscribe(doShowSearch)
        callHideSearch.bindToLifeCycle().subscribe(doHideSearch)
        callFocusChange.bindToLifeCycle().subscribe(doFocusChange)
        callShowTab.bindToLifeCycle().subscribe(doTabExpand)
        selectItem.bindToLifeCycle().subscribe(setRoute)
        clickPreferredRouteEdit.bindToLifeCycle().subscribe(goToPreferredRouteEdit)

        callSearchRoute.bindToLifeCycle().flatMap {
            Observable.fromCallable { Pair(it, enviorment.featuredRouteRepository.searchFeaturedRoute(it)) }
        }.subscribe(doSearchRoute)

        callSearchPort.bindToLifeCycle().flatMap {
            Observable.fromCallable { Pair(it, enviorment.portRepository.searchPort(it)) }
        }.subscribe(doSearchPort)

        updateSelectedDate.bindToLifeCycle().doOnNext {
            enviorment.portRepository.updateSelectedPortDate(it)
        }.subscribe {
            Timber.v("route:/ port date is updated")
        }

        loadFeaturedRoute.flatMapSingle { enviorment.featuredRouteRepository.loadFeaturedRoutes() }
                .bindToLifeCycle()
                .doOnNext {
                    Timber.v("route:/load on search view")
                }
                .subscribe(featuredRoutes)

        loadPortTree.bindToLifeCycle().subscribe(portTree)
    }

    override fun callFeaturedRoute(parameter: Parameter) = loadFeaturedRoute.onNext(parameter)
    override fun callPortTree(parameter: Parameter) = loadPortTree.onNext(parameter)
    override fun callShowSearch(parameter: Parameter) = callShowSearch.onNext(parameter)
    override fun callHideSearch(parameter: Parameter) = callHideSearch.onNext(parameter)
    override fun callFocusChange(focus: Boolean) = callFocusChange.onNext(focus)
    override fun callSelectItem(route: FeaturedRoute) = selectItem.onNext(route)
    override fun callUpdatePortDate(code: String) = updateSelectedDate.onNext(code)
    override fun callSearchPort(value: String) = callSearchPort.onNext(value)
    override fun callSearchRoute(value: String) = callSearchRoute.onNext(value)
    override fun clickPreferredEdit(parameter: Parameter) = clickPreferredRouteEdit.onNext(parameter)

    override fun getFeaturedRoutes(): Observable<List<FeaturedRoute>> = featuredRoutes
    override fun getPortTree(): Observable<Parameter> = portTree
    override fun doShowSearch(): Observable<Parameter> = doShowSearch
    override fun doHideSearch(): Observable<Parameter> = doHideSearch
    override fun doFocusChange(): Observable<Boolean> = doFocusChange
    override fun setRoute(): Observable<FeaturedRoute> = setRoute
    override fun doSearchPort(): Observable<Pair<String, Observable<List<PortDao.PortMinimal>>>> = doSearchPort
    override fun doSearchRoute(): Observable<Pair<String, Observable<List<FeaturedRoute>>>> = doSearchRoute
    override fun goToPreferredEdit(): Observable<Parameter> = goToPreferredRouteEdit

    fun checkPort(code: String): Boolean{
        val isInland = enviorment.portRepository.isInland(code);
        Timber.v("diver:/ check for $code is Port.." + ", isInland = " + isInland);
        return isInland;
    }
}

interface RouteSearchInputs {
    fun callFeaturedRoute(parameter: Parameter)
    fun callPortTree(parameter: Parameter)
    fun callShowSearch(parameter: Parameter)
    fun callHideSearch(parameter: Parameter)
    fun callFocusChange(focus: Boolean)
    fun callSelectItem(route: FeaturedRoute)
    fun callUpdatePortDate(code: String)
    fun callSearchPort(value: String)
    fun callSearchRoute(value: String)
    fun clickPreferredEdit(parameter: Parameter)
}

interface RouteSearchOutputs {
    fun getFeaturedRoutes(): Observable<List<FeaturedRoute>>
    fun getPortTree(): Observable<Parameter>
    fun doShowSearch(): Observable<Parameter>
    fun doHideSearch(): Observable<Parameter>
    fun doFocusChange(): Observable<Boolean>
    fun setRoute(): Observable<FeaturedRoute>
    fun doSearchPort(): Observable<Pair<String, Observable<List<PortDao.PortMinimal>>>>
    fun doSearchRoute(): Observable<Pair<String, Observable<List<FeaturedRoute>>>>
    fun goToPreferredEdit(): Observable<Parameter>
}