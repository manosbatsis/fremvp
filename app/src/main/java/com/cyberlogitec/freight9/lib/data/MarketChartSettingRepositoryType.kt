package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Chart
import io.reactivex.Single

interface MarketChartSettingRepositoryType {

    fun storeMarketChartSettingInDb(chart: Chart)

    fun getMarketChartSettingFromDb(id: String): Single<Chart>

}