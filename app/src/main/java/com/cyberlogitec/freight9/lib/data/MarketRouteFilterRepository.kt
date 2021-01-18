package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.MarketRouteDao
import com.cyberlogitec.freight9.lib.model.MarketRoute
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MarketRouteFilterRepository(val Dao: MarketRouteDao): MarketRouteFilterRepositoryType {
    override fun storeMarketRouteInDb(route: MarketRoute) {
        Observable.fromCallable {
            Dao.deleteAll()
            Dao.insert(route) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${route} carrier from API in DB...")
                }
    }

    override fun getMarketRouteFromDb(): Single<MarketRoute> {
        return Dao.getMarketRoute()
    }
}
