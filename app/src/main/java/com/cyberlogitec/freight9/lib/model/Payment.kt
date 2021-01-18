package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "payment", primaryKeys = arrayOf("type","paymenttypecode","depSeq"))
data class Payment(

        @ColumnInfo(name = "type")
        @SerializedName("type")
        var dataType: String,

        @ColumnInfo(name = "paymenttypecode")
        @SerializedName("paymenttypecode")
        var paymenttypecode: String,

        @ColumnInfo(name = "depSeq")
        @SerializedName("depSeq")
        var depSeq: String,

        @ColumnInfo(name = "iniPymtRto")
        @SerializedName("iniPymtRto")
        var iniPymtRto: String?,

        @ColumnInfo(name = "midTrmPymtRto")
        @SerializedName("midTrmPymtRto")
        var midTrmPymtRto: String?,

        @ColumnInfo(name = "balRto")
        @SerializedName("balRto")
        var balRto: String?,

        @ColumnInfo(name = "selected")
        @SerializedName("selected")
        var selected: Boolean? = false
) : Serializable
