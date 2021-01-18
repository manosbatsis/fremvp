package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.FeaturedRouteDao
import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FeaturedRouteRepository(val featuredRouteDao: FeaturedRouteDao): FeaturedRouteRepositoryType{
    override fun deleteRoute(route: FeaturedRoute) {
        Observable.fromCallable { featuredRouteDao.deleteRoute(route.fromCode, route.toCode) }
                .subscribe{
                    featuredRouteDao.getFeaturedRouteAll().toObservable()
                            .doOnNext {
                                var startPriority = 1
                                for (route in it){
                                    route.priority = startPriority.toLong()
                                    startPriority++
                                }
                                updateFeatureRoutes(it)
                            }
                            .subscribe {
                                Timber.d("diver:/ priority updated/")
                            }
                }
    }

    override fun deleteRouteById(target: Long) {
        Observable.fromCallable { featuredRouteDao.deleteById(target) }
                .subscribe {
                    featuredRouteDao.getFeaturedRouteAll().toObservable()
                            .doOnNext {
                                var startPriority = 1
                                for (route in it){
                                    route.priority = startPriority.toLong()
                                    startPriority++
                                }
                                updateFeatureRoutes(it)
                            }
                            .subscribe {
                                Timber.d("diver:/ priority updated/")
                            }
                }
    }

    override fun updateFeatureRoutes(routes: List<FeaturedRoute>) {
        var startPriority: Long = 1
        for (route in routes){
            route.priority = startPriority
            startPriority++
        }
        Observable.fromCallable { featuredRouteDao.deleteAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    featuredRouteDao.insertAll(routes)
                }
                .subscribe {
                    Timber.d("diver:/ " + "Inserted ${routes.size} routes in DB.../")
                }
    }

    override fun insertFeaturedRoute(route: FeaturedRoute) {
        featuredRouteDao.getFeaturedRouteAll().toObservable()
                .doOnNext {
                    route.priority = (it.size+1).toLong()
                    featuredRouteDao.insert(route)
                }
                .subscribe {
                    Timber.v("diver:/ Inserted route on priority=${route.priority}")
                }
    }

    override fun searchFeaturedRoute(keyword: String): Observable<List<FeaturedRoute>>{
        return Observable.fromCallable { featuredRouteDao.loadFeaturedRoutesAll() }
                .map {
                    it.filter {
                        keyword.isNotEmpty()
                                && (it.fromCode.startsWith(keyword, true)
                                || it.toCode.startsWith(keyword, true)
                                || it.fromDetail!!.startsWith(keyword, true)
                                || it.toDetail!!.startsWith(keyword, true))
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadFeaturedRoutes(): Single<List<FeaturedRoute>>{
        return featuredRouteDao.getFeaturedRouteAll()
    }
}