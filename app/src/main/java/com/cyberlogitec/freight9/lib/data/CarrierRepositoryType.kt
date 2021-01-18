package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Carrier
import io.reactivex.Observable

interface CarrierRepositoryType {

    fun storeCarrierInDb(carrier: Carrier)

    fun storeCarriersInDb(carriers: List<Carrier>)

    fun getCarriersFromDb(): Observable<List<Carrier>>

    fun getCarrierName(carrierCode: String): Observable<String>

}