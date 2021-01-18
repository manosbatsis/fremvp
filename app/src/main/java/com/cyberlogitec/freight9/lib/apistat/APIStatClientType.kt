package com.cyberlogitec.freight9.lib.apistat

import com.cyberlogitec.freight9.lib.model.*
import io.reactivex.Single

interface APIStatClientType {

    //Market Watch

    fun postMarketWatchChartList(body: PostMarketWatchChartRequest): Single<MarketWatchChartList>

    fun postMarketWatchWeekDetail(body: PostMarketWatchProductWeekDetailChartListRequest): Single<MarketWatchProductWeekDetailChartList>

    fun postMarketWatchWeekDealHistory(body: PostMarketWatchProductWeekDetailChartListRequest): Single<MarketWatchDealHistory>

    //Market Index
    fun getMarketWatchIndexList(): Single<List<MarketIndexList>>

    fun postMarketIndexChartList(body: PostMarketIndexChartRequest): Single<MarketIndexChartList>
}