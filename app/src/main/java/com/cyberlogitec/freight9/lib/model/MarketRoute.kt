package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "market_route")
data class MarketRoute(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name="id")
        @SerializedName("id")
        var id: Long?,

        @ColumnInfo(name="from_code")
        var fromCode: String,

        @ColumnInfo(name="from_detail")
        var fromDetail: String?,

        @ColumnInfo(name="to_code")
        var toCode: String,

        @ColumnInfo(name="to_detail")
        var toDetail: String?
):Serializable