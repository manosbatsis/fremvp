package com.cyberlogitec.freight9.lib.apistat

import com.cyberlogitec.freight9.BuildConfig
import com.cyberlogitec.freight9.lib.model.*
import io.reactivex.Single
import retrofit2.http.*

interface APIStatService {

    object EndPoint {
        const val baseLocalUrl = BuildConfig.SERVER_STAT_URL
    }

    // MarketWatch
    @POST("api/v1/stats/filtered/marketwatch")
    fun postMarketWatchChartList(@Body body: PostMarketWatchChartRequest): Single<MarketWatchChartList>

    @POST("api/v1/stats/filtered/marketwatch/productweekdetail")
    fun postMarketWatchWeekDetail(@Body body: PostMarketWatchProductWeekDetailChartListRequest): Single<MarketWatchProductWeekDetailChartList>

    @POST("api/v1/stats/filtered/marketwatch/productdealhistory")
    fun postMarketWatchWeekDealHistory(@Body body: PostMarketWatchProductWeekDetailChartListRequest): Single<MarketWatchDealHistory>

    @GET("api/v1/stats/idxlist")
    fun getMarketIndexList()
            : Single<List<MarketIndexList>>

    @POST("api/v1/stats/filtered/marketindex/summary")
    fun postMarketIndexChartList(@Body body: PostMarketIndexChartRequest): Single<MarketIndexChartList>

    ///////////////////////////////////////////////////////////////////////////////////////////////

}