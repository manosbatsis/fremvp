package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.Chart
import io.reactivex.Single


@Dao
interface MarketChartSettingDao {

    @Query("SELECT * FROM market_watch_chart WHERE storeId=:id")
    fun getMarketChart(id: String): Single<Chart>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chart: Chart)

    @Query("DELETE FROM market_watch_chart")
    fun deleteAll()
}