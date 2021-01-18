package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.Container
import io.reactivex.Single


@Dao
interface ContainerDao {

    @Query("SELECT * FROM container WHERE type = :dataType ")
    fun getContainers(dataType: String): Single<List<Container>>

    @Query("SELECT * FROM container")
    fun getContainers(): Single<List<Container>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(container: Container)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(containers: List<Container>)
}