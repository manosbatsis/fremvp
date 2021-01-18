package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "inventory")
data class Inventory(

        @PrimaryKey
        @ColumnInfo(name = "inventoryNumber")
        val inventoryNumber: String,

        @ColumnInfo(name = "masterContractNumber")
        val masterContractNumber: String,

        @ColumnInfo(name = "eventType")
        val eventType: String?,

        @ColumnInfo(name = "timestamp")
        val timestamp: String?,

        @ColumnInfo(name = "errorCode")
        val errorCode: String?,

        @ColumnInfo(name = "errorMessage")
        val errorMessage: String?,

        @ColumnInfo(name = "memo")
        val memo: String?,

        @ColumnInfo(name = "userId")
        val userId: String?,

        @ColumnInfo(name = "carrierCode")
        val carrierCode: String?,

        @ColumnInfo(name = "carrierCount")
        val carrierCount: Int,

        @ColumnInfo(name = "minQty")
        val minQty: String?,

        @ColumnInfo(name = "maxQty")
        val maxQty: String?,

        @ColumnInfo(name = "minYearWeek")
        val minYearWeek: String?,

        @ColumnInfo(name = "maxYearWeek")
        val maxYearWeek: String?,

        @ColumnInfo(name = "polCount")
        val polCount: Int,

        @ColumnInfo(name = "podCount")
        val podCount: Int,

        @ColumnInfo(name = "polName")
        val polName: String?,

        @ColumnInfo(name = "podName")
        val podName: String?,

        @ColumnInfo(name = "polCode")
        val polCode: String?,

        @ColumnInfo(name = "podCode")
        val podCode: String?,

        @ColumnInfo(name = "ownerCompanyCode")
        val ownerCompanyCode: String?,

        @ColumnInfo(name = "inventoryChangeSeq")
        val inventoryChangeSeq: Int
)

