package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.model.Container
import io.reactivex.Observable

interface ContainerRepositoryType {

    fun storeContainerInDb(container: Container)

    fun storeContainersInDb(containers: List<Container>)

    fun getContainersFromDb(type: String): Observable<List<Container>>

    fun getContainersFromDb(): Observable<List<Container>>

}