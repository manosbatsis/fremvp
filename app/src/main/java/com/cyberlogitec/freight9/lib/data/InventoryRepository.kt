package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.InventoryDao
import com.cyberlogitec.freight9.lib.model.Inventory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class InventoryRepository(val inventoryDao: InventoryDao): InventoryRepositoryType {

    override fun upsertInventory(inventory: Inventory) =
            inventoryDao.upsertInventory(inventory)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    override fun upsertInventories(sofInventories: List<Inventory>) =
            inventoryDao.upsertInventories(sofInventories)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    override fun getInventories(): Single<List<Inventory>> =
            inventoryDao.getInventories()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun getInventory(inventoryNumber: String): Single<Inventory> =
            inventoryDao.getInventory(inventoryNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    override fun getInventoryWith(masterContractNumber: String): Single<Inventory> =
            inventoryDao.getInventoryWith(masterContractNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    override fun delete(inventory: Inventory) =
            inventoryDao.delete(inventory)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    override fun deleteAll() =
            inventoryDao.deleteAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

}