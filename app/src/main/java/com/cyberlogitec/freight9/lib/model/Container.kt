package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "container")
data class Container(

        @ColumnInfo(name = "type")
        @SerializedName("type")
        var dataType: String,

        @PrimaryKey
        @ColumnInfo(name = "code")
        @SerializedName("code")
        var code: String,

        @ColumnInfo(name = "fullname")
        @SerializedName("fullname")
        var fullname: String,

        @ColumnInfo(name = "selected")
        @SerializedName("selected")
        var selected: Boolean? = false
)
