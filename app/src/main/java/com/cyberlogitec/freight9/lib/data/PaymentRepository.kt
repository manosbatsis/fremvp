package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.PaymentDao
import com.cyberlogitec.freight9.lib.model.Payment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class PaymentRepository(val paymentDao: PaymentDao): PaymentRepositoryType {

    override fun storePaymentInDb(payment: Payment) {
        Observable.fromCallable { paymentDao.insert(payment) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${payment} carrier from API in DB...")
                }
    }

    override fun storePaymentsInDb(payments: List<Payment>) {
        Observable.fromCallable { paymentDao.insertAll(payments) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("f9: " + "Inserted ${payments.size} carriers from API in DB...")
                }
    }

    override fun getPaymentsFromDb(type: String): Observable<List<Payment>> {
        return paymentDao.getPayments(type).filter { it.isNotEmpty() }
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} payments from DB...")
                }
    }

    override fun getPaymentsFromDb(): Observable<List<Payment>> {
        return paymentDao.getPayments().filter { it.isNotEmpty() }
                .toObservable()
                .doOnNext {
                    Timber.d("f9: " + "Dispatching ${it.size} payments from DB...")
                }
    }
}
