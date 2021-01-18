package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "carriers")
data class Carrier(
        @PrimaryKey
        @ColumnInfo(name = "carriercode")
        @SerializedName("carriercode")
        var carriercode: String,

        @ColumnInfo(name = "carriername")
        @SerializedName("carriername")
        var carriername: String?,

        @ColumnInfo(name = "selected")
        @SerializedName("selected")
        var select: Boolean? = false) : Serializable
