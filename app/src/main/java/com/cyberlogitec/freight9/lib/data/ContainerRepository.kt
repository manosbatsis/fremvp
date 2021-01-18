package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.ContainerDao
import com.cyberlogitec.freight9.lib.model.Container
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ContainerRepository(val containerDao: ContainerDao): ContainerRepositoryType {

    override fun storeContainerInDb(container: Container) {
        Observable.fromCallable { containerDao.insert(container) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${container} carrier from API in DB...")
                }
    }

    override fun storeContainersInDb(containers: List<Container>) {
        Observable.fromCallable { containerDao.insertAll(containers) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${containers.size} carriers from API in DB...")
                }
    }

    override fun getContainersFromDb(type: String): Observable<List<Container>> {
        return containerDao.getContainers(type).filter { it.isNotEmpty() }
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} carriers from DB...")
                }
    }

    override fun getContainersFromDb(): Observable<List<Container>> {
        return containerDao.getContainers().filter { it.isNotEmpty() }
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} carriers from DB...")
                }
    }
}
