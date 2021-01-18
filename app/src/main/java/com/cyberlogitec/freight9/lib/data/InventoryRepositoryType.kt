package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Inventory
import io.reactivex.Completable
import io.reactivex.Single

interface InventoryRepositoryType {

    fun upsertInventory(inventory: Inventory): Single<Long>

    fun upsertInventories(sofInventories: List<Inventory>): Single<List<Long>>

    fun getInventories(): Single<List<Inventory>>

    fun getInventory(inventoryNumber: String): Single<Inventory>

    fun getInventoryWith(masterContractNumber: String): Single<Inventory>

    fun delete(inventory: Inventory): Completable

    fun deleteAll(): Completable

}