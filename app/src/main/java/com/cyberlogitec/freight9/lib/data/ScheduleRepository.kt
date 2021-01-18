package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.ScheduleDao
import com.cyberlogitec.freight9.lib.model.Schedule
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ScheduleRepository(val scheduleDao: ScheduleDao) : ScheduleRepositoryType {
    override fun loadAllSchedules(): Single<List<Schedule>> {
        return scheduleDao.loadAllSchedules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun loadSchedules(serviceLaneCode: String, polCode: String): Single<List<Schedule>> {
        return scheduleDao.loadSchedules(serviceLaneCode, polCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun hasScheduleData(): Boolean {
        return (scheduleDao.getScheduleCount() > 0)
    }

    override fun insertScheduleData(schedules: List<Schedule>) {
        Observable.fromCallable { scheduleDao.insertAll(schedules) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${schedules.size} schedules from API in DB...")
                }
    }

    override fun deleteAll() {
        Observable.fromCallable { scheduleDao.deleteAll() }
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Timber.d("divier:/ delete all schedule")
                }
    }
}