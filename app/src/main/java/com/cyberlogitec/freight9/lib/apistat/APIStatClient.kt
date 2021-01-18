package com.cyberlogitec.freight9.lib.apistat

import com.cyberlogitec.freight9.lib.model.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class APIStatClient(val apiStatService: APIStatService) : APIStatClientType {

    //- MarketWatch -------------------------
    override fun postMarketWatchChartList(body: PostMarketWatchChartRequest): Single<MarketWatchChartList> =
         apiStatService.postMarketWatchChartList(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    override fun postMarketWatchWeekDetail(body: PostMarketWatchProductWeekDetailChartListRequest): Single<MarketWatchProductWeekDetailChartList> =
            apiStatService.postMarketWatchWeekDetail(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
    override fun postMarketWatchWeekDealHistory(body: PostMarketWatchProductWeekDetailChartListRequest): Single<MarketWatchDealHistory> =
            apiStatService.postMarketWatchWeekDealHistory(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    ///////////////////////////////////////////////////////////////////////////////////////////////


    override fun getMarketWatchIndexList()
            : Single<List<MarketIndexList>> =
            apiStatService.getMarketIndexList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())


    override fun postMarketIndexChartList(body: PostMarketIndexChartRequest): Single<MarketIndexChartList> =
            apiStatService.postMarketIndexChartList(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
}