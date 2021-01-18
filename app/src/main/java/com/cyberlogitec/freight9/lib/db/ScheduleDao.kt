package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.Schedule
import io.reactivex.Single

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules")
    fun loadAllSchedules(): Single<List<Schedule>>

    @Query("SELECT * FROM schedules where serviceLaneCode = :serviceLaneCode and polCode = :polCode")
    fun loadSchedules(serviceLaneCode: String, polCode: String): Single<List<Schedule>>

    @Insert
    fun insertAll(schedules: List<Schedule>)

    @Query("SELECT COUNT(*) FROM schedules")
    fun getScheduleCount(): Int

    @Query("DELETE FROM schedules")
    fun deleteAll()
}