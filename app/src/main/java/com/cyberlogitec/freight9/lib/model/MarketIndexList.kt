package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

@Entity(tableName = "market_index_list")
data class MarketIndexList(
        @ColumnInfo(name = "idxSubject")
        @SerializedName("idxSubject")
        var idxSubject: String?,

        @ColumnInfo(name = "idxCategory")
        @SerializedName("idxCategory")
        var idxCategory: String?,

        @PrimaryKey
        @ColumnInfo(name = "idxCd")
        @SerializedName("idxCd")
        var idxCd: String,

        @ColumnInfo(name = "idxNm")
        @SerializedName("idxNm")
        var idxNm: String?,

        @ColumnInfo(name = "interval")
        @SerializedName("intervalItem")
        var intervalItem: ArrayList<Item>?
) : Serializable
{

data class Item(
        @SerializedName("interval")
        var interval: String,

        @SerializedName("intervalSeq")
        var intervalSeq: Int
) : Serializable
}
