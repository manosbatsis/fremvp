package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.FeaturedRoute
import io.reactivex.Observable
import io.reactivex.Single

interface FeaturedRouteRepositoryType{

    fun updateFeatureRoutes(routes: List<FeaturedRoute>)
    fun insertFeaturedRoute(route: FeaturedRoute)
    fun deleteRoute(route: FeaturedRoute)
    fun deleteRouteById(target: Long)
    fun loadFeaturedRoutes(): Single<List<FeaturedRoute>>
    fun searchFeaturedRoute(keyword: String): Observable<List<FeaturedRoute>>
}