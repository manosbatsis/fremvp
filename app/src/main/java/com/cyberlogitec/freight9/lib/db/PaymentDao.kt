package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.Payment
import io.reactivex.Single


@Dao
interface PaymentDao {

    @Query("SELECT * FROM payment WHERE type = :dataType ")
    fun getPayments(dataType: String): Single<List<Payment>>

    @Query("SELECT * FROM payment")
    fun getPayments(): Single<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(payment: Payment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(payments: List<Payment>)
}