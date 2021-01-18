package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.MarketIndexListDao
import com.cyberlogitec.freight9.lib.model.MarketIndexList
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MarketIndexListRepository(val Dao: MarketIndexListDao): MarketIndexListRepositoryType {
    override fun storeMarketIndexListInDb(indexList: MarketIndexList) {
        Observable.fromCallable {
            Dao.insert(indexList) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${indexList} setting in DB...")
                }
    }

    override fun getMarketIndexListFromDb(indexCode: String): Single<MarketIndexList> {
        return Dao.getIndex(indexCode)
    }

    override fun deleteAllIndex() {
        return Dao.deleteAll()
    }
}
