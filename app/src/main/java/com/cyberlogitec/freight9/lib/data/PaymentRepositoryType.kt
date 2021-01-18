package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Payment
import io.reactivex.Observable

interface PaymentRepositoryType {

    fun storePaymentInDb(payment: Payment)

    fun storePaymentsInDb(payments: List<Payment>)

    fun getPaymentsFromDb(type: String): Observable<List<Payment>>

    fun getPaymentsFromDb(): Observable<List<Payment>>

}