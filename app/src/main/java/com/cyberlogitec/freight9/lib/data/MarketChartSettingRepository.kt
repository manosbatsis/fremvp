package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.MarketChartSettingDao
import com.cyberlogitec.freight9.lib.model.Chart
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MarketChartSettingRepository(val Dao: MarketChartSettingDao): MarketChartSettingRepositoryType {
    override fun storeMarketChartSettingInDb(chart: Chart) {
        Observable.fromCallable {
            Dao.insert(chart) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${chart} setting in DB...")
                }
    }

    override fun getMarketChartSettingFromDb(id: String): Single<Chart> {
        return Dao.getMarketChart(id)
    }
}
