package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "period")
data class WeekCell(
        @PrimaryKey
        @ColumnInfo(name = "id")
        @SerializedName("id")
        var id: Long = -1L,

        @ColumnInfo(name = "seqno")
        @SerializedName("seqno")
        var seqno: Int = -1,

        @ColumnInfo(name = "year")
        @SerializedName("year")
        var year: Int = -1,

        @ColumnInfo(name = "month")
        @SerializedName("month")
        var month: Int = -1,

        @ColumnInfo(name = "isBeginOfMonth")
        @SerializedName("isBeginOfMonth")
        var isBeginOfMonth: Boolean = false,

        @ColumnInfo(name = "week_number")
        @SerializedName("week_number")
        var week_number: Int = -1,

        @ColumnInfo(name = "status")
        @SerializedName("status")
        var status: Int = -1
) {
        companion object {
                val VOID = 0            // Selected (X), Contract(X)
                val VOID_SELECTED = 1  // Selected (O), Contract(X)
                val NORMAL = 2          // Selected (X), Contract(O)
                val SELECTED = 3        // Selected (O), Contract(O)
        }
}

/*
data class Fromto(
        var from: String? = null,
        var to: String? = null
)
*/