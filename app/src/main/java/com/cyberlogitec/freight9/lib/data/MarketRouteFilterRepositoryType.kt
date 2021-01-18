package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.MarketRoute
import io.reactivex.Single

interface MarketRouteFilterRepositoryType {

    fun storeMarketRouteInDb(route: MarketRoute)

    fun getMarketRouteFromDb(): Single<MarketRoute>

}