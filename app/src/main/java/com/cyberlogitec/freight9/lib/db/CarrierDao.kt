package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.Carrier
import io.reactivex.Single


@Dao
interface CarrierDao {

    @Query("SELECT * FROM carriers")
    fun getCarriers(): Single<List<Carrier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(carrier: Carrier)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(carriers: List<Carrier>)

    @Query("SELECT carriername FROM carriers WHERE carriercode=:code")
    fun getCarrierName(code: String): Single<String>
}