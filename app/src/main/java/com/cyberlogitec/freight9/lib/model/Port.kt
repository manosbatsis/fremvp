package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "ports")
data class Port(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: Long?,

        @ColumnInfo(name = "dataOwnrPtrId")
        @SerializedName("dataOwnrPtrId")
        var dataOwnrPtrId: String,

        @ColumnInfo(name = "cntiCd")
        @SerializedName("cntiCd")
        var continentCode: String,

        @ColumnInfo(name = "cntiNm")
        @SerializedName("cntiNm")
        var continentName: String,

        @ColumnInfo(name="sbCntiCd")
        @SerializedName("sbCntiCd")
        val subContinentCode: String,

        @ColumnInfo(name="sbCntiNm")
        @SerializedName("sbCntiNm")
        var subContinentName: String,

        @ColumnInfo(name = "cntCd")
        @SerializedName("cntCd")
        val countryCode: String,

        @ColumnInfo(name = "cntNm")
        @SerializedName("cntNm")
        var countryName: String,

        @ColumnInfo(name = "regnCd")
        @SerializedName("regnCd")
        val regionCode: String,

        @ColumnInfo(name = "regnNm")
        @SerializedName("regnNm")
        val regionName: String,

        @ColumnInfo(name = "portCd")
        @SerializedName("locCd")
        val portCode: String,

        @ColumnInfo(name = "portNm")
        @SerializedName("locNm")
        var portName: String,

        @ColumnInfo(name = "isInland")
        @SerializedName("portInldYn")
        val isInland: String,

        @ColumnInfo(name = "lastSelected")
        val lastSelected: Date?
)