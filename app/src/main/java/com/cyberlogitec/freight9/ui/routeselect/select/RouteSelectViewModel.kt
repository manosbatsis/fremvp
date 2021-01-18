package com.cyberlogitec.freight9.ui.routeselect.select

import android.content.Context
import android.content.Intent
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class RouteSelectViewModel(context: Context) : BaseViewModel(context), RouteSelectInputs, RouteSelectOutputs {

    val inPuts: RouteSelectInputs = this
    private val callOpenSearch = PublishSubject.create<Intent>()
    private val loadFeaturedRoute = PublishSubject.create<Parameter>()
    private val addFeaturedRoute = PublishSubject.create<FeaturedRoute>()
    private val deleteFeaturedRoute = PublishSubject.create<FeaturedRoute>()
    val outPuts: RouteSelectOutputs = this
    private val doOpenSearch = PublishSubject.create<Intent>()
    private val featuredRoutes = PublishSubject.create<List<FeaturedRoute>>()
    private val doAddFeaturedRoute = PublishSubject.create<FeaturedRoute>()
    private val doDeleteFeaturedRoute = PublishSubject.create<FeaturedRoute>()

    var currentPage = -1

    init {
        callOpenSearch.bindToLifeCycle().subscribe(doOpenSearch)

        loadFeaturedRoute.flatMapSingle { enviorment.featuredRouteRepository.loadFeaturedRoutes() }
                .bindToLifeCycle().
                doOnNext {
                    Timber.v("route:/load on select view")
                    for(route in it){
                        Timber.v("route: /idx=${route.id} from=${route.fromCode} to=${route.toCode}")
                    }
                }
                .subscribe(featuredRoutes)

        addFeaturedRoute.bindToLifeCycle()
                .doOnNext {
                    Timber.v("Insert route by favorite")
                    enviorment.featuredRouteRepository.insertFeaturedRoute(it)
                }
                .subscribe(doAddFeaturedRoute)

        deleteFeaturedRoute.bindToLifeCycle()
                .doOnNext {
                    Timber.v("delete route by favorite")
                    enviorment.featuredRouteRepository.deleteRoute(it)
                }.subscribe(doDeleteFeaturedRoute)
    }

    override fun callFeaturedRoute(parameter: Parameter) = loadFeaturedRoute.onNext(parameter)
    override fun addFeatureRoute(route: FeaturedRoute) = addFeaturedRoute.onNext(route)
    override fun deleteFeaturedRoute(route: FeaturedRoute) = deleteFeaturedRoute.onNext(route)
    override fun callOpenSearch(intent: Intent) = callOpenSearch.onNext(intent)

    override fun getFeaturedRoutes(): Observable<List<FeaturedRoute>> = featuredRoutes
    override fun doAddFeatureRoute(): Observable<FeaturedRoute> = doAddFeaturedRoute
    override fun doDeleteFeaturedRoute(): Observable<FeaturedRoute> = doDeleteFeaturedRoute
    override fun doOpenSearch(): Observable<Intent> = doOpenSearch
}

interface RouteSelectInputs {
    fun callFeaturedRoute(parameter: Parameter)
    fun addFeatureRoute(route: FeaturedRoute)
    fun deleteFeaturedRoute(route: FeaturedRoute)
    fun callOpenSearch(intent: Intent)
}

interface RouteSelectOutputs {
    fun getFeaturedRoutes(): Observable<List<FeaturedRoute>>
    fun doAddFeatureRoute(): Observable<FeaturedRoute>
    fun doDeleteFeaturedRoute(): Observable<FeaturedRoute>
    fun doOpenSearch() : Observable<Intent>
}