package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.MarketIndexList
import io.reactivex.Single


@Dao
interface MarketIndexListDao {

    @Query("SELECT * FROM market_index_list WHERE idxCd=:id")
    fun getIndex(id: String): Single<MarketIndexList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chart: MarketIndexList)

    @Query("DELETE FROM market_index_list")
    fun deleteAll()
}