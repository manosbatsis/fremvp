package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.MarketIndexList
import io.reactivex.Single

interface MarketIndexListRepositoryType {

    fun storeMarketIndexListInDb(chart: MarketIndexList)

    fun getMarketIndexListFromDb(indexCode: String): Single<MarketIndexList>

    fun deleteAllIndex()

}