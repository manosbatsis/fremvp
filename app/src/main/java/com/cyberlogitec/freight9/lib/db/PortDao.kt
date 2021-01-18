package com.cyberlogitec.freight9.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cyberlogitec.freight9.lib.model.Port
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

@Dao
interface PortDao {
    data class PortMinimal(
            val portNm: String,
            val portCd: String
    )

    // test route data
    data class RouteMinimal(
            val iPolCd: String,
            val iPolNm: String,
            val iPodCd: String,
            val iPodNm: String
    )

    @Query("SELECT x.portCd AS iPolCd, x.portNm AS iPolNm, y.portCd AS iPodCd, y.portNm AS iPodNm "
            + " FROM ports x, ports y "
            + " WHERE x.id <> y.id "
            + " AND x.isInland LIKE '%4%' AND y.isInland LIKE '%4%' "
            + " AND x.regnCd IN ( 'CAE', 'CAW', 'UPN', 'UPS', 'KOR', 'JPE', 'JPW', 'CNN', 'ANE' ) "
            + " AND y.regnCd IN ( 'CAE', 'CAW', 'UPN', 'UPS', 'KOR', 'JPE', 'JPW', 'CNN', 'ANE' ) "
            + " LIMIT 100000 "
    )
    fun loadTestRoute() : Single<List<RouteMinimal>>

    @Query("SELECT * FROM ports ORDER BY cntiNm, cntNm")
    fun loadAllPorts() : Single<List<Port>>

    @Query("SELECT portNm, portCd FROM ports WHERE portNm like :key OR portCd like :key")
    fun searchPort(key: String): Observable<List<PortMinimal>>

    @Query("SELECT cntiNm FROM ports WHERE isInland LIKE '%4%' GROUP BY cntiNm")
    fun getContinents(): List<String>

    @Query("SELECT cntNm FROM ports WHERE cntiNm=:continent AND isInland LIKE '%4%' GROUP BY cntNm")
    fun getCountries(continent: String): List<String>

    @Query("SELECT portNm, portCd FROM ports WHERE cntNm=:country AND isInland LIKE '%4%'")
    fun getPorts(country: String) : List<PortMinimal>

    @Insert
    fun insertAll(ports: List<Port>)

    @Insert
    fun insert(port: Port)

    @Query("SELECT COUNT(*) FROM ports")
    fun getPortCount(): Int

    @Query("DELETE FROM ports")
    fun deleteAll()

    @Query("UPDATE ports SET lastSelected=:date WHERE portCd=:code")
    fun updatePortSelectedDate(code: String, date: Date)

    @Query("SELECT portNm, portCd FROM ports WHERE lastSelected IS NOT NULL ORDER BY lastSelected DESC LIMIT 5")
    fun getRecentPorts(): List<PortMinimal>

    @Query("SELECT count(*) FROM ports WHERE portCd=:code AND isInland LIKE '%4%'")
    fun getIsInland(code: String): Boolean

    @Query("SELECT portNm FROM ports WHERE portCd=:code")
    fun getPortNm(code: String): String
}