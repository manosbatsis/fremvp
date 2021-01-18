package com.cyberlogitec.freight9.lib.db

import androidx.room.*
import com.cyberlogitec.freight9.lib.model.Inventory
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory")
    fun getInventories(): Single<List<Inventory>>

    @Query("SELECT * FROM inventory where inventoryNumber = :inventoryNumber LIMIT 1")
    fun getInventory(inventoryNumber: String): Single<Inventory>

    @Query("SELECT * FROM inventory where masterContractNumber = :masterContractNumber LIMIT 1")
    fun getInventoryWith(masterContractNumber: String): Single<Inventory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertInventory(inventory: Inventory): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertInventories(sofInventories: List<Inventory>): Single<List<Long>>

    @Delete
    fun delete(inventory: Inventory): Completable

    @Query("DELETE FROM inventory")
    fun deleteAll(): Completable


}