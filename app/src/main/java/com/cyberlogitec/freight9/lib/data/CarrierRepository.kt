package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.CarrierDao
import com.cyberlogitec.freight9.lib.model.Carrier
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class CarrierRepository(val carrierDao: CarrierDao): CarrierRepositoryType {

    override fun storeCarrierInDb(carrier: Carrier) {
        Observable.fromCallable { carrierDao.insert(carrier) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${carrier} carrier from API in DB...")
                }
    }

    override fun storeCarriersInDb(carriers: List<Carrier>) {
        Observable.fromCallable { carrierDao.insertAll(carriers) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${carriers.size} carriers from API in DB...")
                }
    }

    override fun getCarriersFromDb(): Observable<List<Carrier>> {
        return carrierDao.getCarriers().filter { it.isNotEmpty() }
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} carriers from DB...")
                }
    }

    override fun getCarrierName(carrierCode: String): Observable<String> {
        return carrierDao.getCarrierName(carrierCode).filter { it.isNotEmpty() }
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it} carrier from DB...")
                }
    }

}