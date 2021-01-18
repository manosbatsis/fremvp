package com.cyberlogitec.freight9.ui.routeselect.featured

import android.content.Context
import android.widget.Toast
import com.cyberlogitec.freight9.common.BaseViewModel
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import com.cyberlogitec.freight9.lib.rx.Parameter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class PreferredRouteEditViewModel(context: Context) : BaseViewModel(context), PreferredRouteEditInputs, PreferredRouteEditOutputs {
    val inPuts: PreferredRouteEditInputs = this
    private val loadFeaturedRoute = PublishSubject.create<Parameter>()
    private val addFeaturedRoute = PublishSubject.create<FeaturedRoute>()
    private val deleteFeaturedRoute = PublishSubject.create<Long>()
    private val updateFeaturedRoutes = PublishSubject.create<List<FeaturedRoute>>()
    val outPuts: PreferredRouteEditOutputs = this
    private val featuredRoutes = PublishSubject.create<List<FeaturedRoute>>()

    init {
        loadFeaturedRoute.flatMapSingle { enviorment.featuredRouteRepository.loadFeaturedRoutes() }
                .bindToLifeCycle()
                .doOnNext {
                    Timber.v("route:/load on preferred view")
                }
                .subscribe(featuredRoutes)

        addFeaturedRoute.bindToLifeCycle().
                subscribe { addRoute->
                    enviorment.featuredRouteRepository.loadFeaturedRoutes().toObservable()
                            .map {
                                it.filter { it.fromCode == addRoute.fromCode && it.toCode == addRoute.toCode }
                            }.subscribe {
                                if (it.size > 0) Toast.makeText(context, "Already contained route..", Toast.LENGTH_SHORT).show()
                                else enviorment.featuredRouteRepository.insertFeaturedRoute(addRoute)
                                Timber.v("diver:/ cotained route cnt="+it.size)
                            }
                }

        deleteFeaturedRoute.bindToLifeCycle()
                .doOnNext {
                    enviorment.featuredRouteRepository.deleteRouteById(it)
                }
                .subscribe {
                    Timber.v("delete route by favorite")
                }

        updateFeaturedRoutes.bindToLifeCycle()
                .doOnNext {
                    enviorment.featuredRouteRepository.updateFeatureRoutes(it)
                }
                .subscribe {
                    Timber.v("update route by favorite")
                }

    }
    override fun callFeaturedRoute(parameter: Parameter) = loadFeaturedRoute.onNext(parameter)
    override fun addFeatureRoute(route: FeaturedRoute) = addFeaturedRoute.onNext(route)
    override fun deleteFeaturedRoute(target_id: Long) = deleteFeaturedRoute.onNext(target_id)
    override fun updateFeaturedRtoues(routes: List<FeaturedRoute>) = updateFeaturedRoutes.onNext(routes)

    override fun getFeaturedRoutes(): Observable<List<FeaturedRoute>> = featuredRoutes
}


interface PreferredRouteEditInputs {
    fun callFeaturedRoute(parameter: Parameter)
    fun addFeatureRoute(route: FeaturedRoute)
    fun deleteFeaturedRoute(target_id: Long)
    fun updateFeaturedRtoues(routes: List<FeaturedRoute>)
}

interface PreferredRouteEditOutputs {
    fun getFeaturedRoutes(): Observable<List<FeaturedRoute>>
}