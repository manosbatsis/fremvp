package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.MarketRoute
import io.reactivex.Single


@Dao
interface MarketRouteDao {

    @Query("SELECT * FROM market_route")
    fun getMarketRoute(): Single<MarketRoute>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(route: MarketRoute)

    @Query("DELETE FROM market_route")
    fun deleteAll()
}