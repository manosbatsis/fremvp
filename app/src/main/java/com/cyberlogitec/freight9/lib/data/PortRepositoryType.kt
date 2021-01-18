package com.cyberlogitec.freight9.lib.data

import com.cyberlogitec.freight9.lib.db.PortDao
import com.cyberlogitec.freight9.lib.db.PortDao.PortMinimal
import com.cyberlogitec.freight9.lib.model.Port
import io.reactivex.Observable
import io.reactivex.Single
import me.texy.treeview.TreeNode

interface PortRepositoryType {

    fun loadAllPorts() : Single<List<Port>>

    fun loadTestRoute(): Single<List<PortDao.RouteMinimal>>

    fun insertPortData(ports: List<Port>)

    fun hasPortData(): Boolean

    fun isPortValid(): Observable<Boolean>

    @Deprecated("not used")
    fun loadPortTree(): TreeNode

    fun makeBaseTree(): TreeNode

    fun initializeBaseTree()

    fun makeTree(): TreeNode

    fun updateSelectedPortDate(portCode: String)

    fun getRecentPorts(): Observable<List<PortMinimal>>

    fun searchPort(value: String): Observable<List<PortMinimal>>

    fun isInland(portCode: String): Boolean

    fun deleteAll()

    fun getPortNm(portCode: String): String
}