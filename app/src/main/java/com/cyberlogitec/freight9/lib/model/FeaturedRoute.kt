package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "featured_route")
data class FeaturedRoute(
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
        var toDetail: String?,

        @ColumnInfo(name="priority")
        var priority: Long
)