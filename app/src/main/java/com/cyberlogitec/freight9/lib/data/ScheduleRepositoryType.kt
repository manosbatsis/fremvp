package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Schedule
import io.reactivex.Single

interface ScheduleRepositoryType {
    fun loadAllSchedules(): Single<List<Schedule>>

    fun loadSchedules(serviceLaneCode: String, polCode: String): Single<List<Schedule>>

    fun hasScheduleData(): Boolean

    fun insertScheduleData(schedules: List<Schedule>)

    fun deleteAll()
}