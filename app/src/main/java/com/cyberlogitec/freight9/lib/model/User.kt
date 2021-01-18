package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Long? = null,

        @ColumnInfo(name = "username")
        var username: String? = null,

        @ColumnInfo(name = "email")
        var email: String? = null,

        @ColumnInfo(name = "password")
        var password: String ? = null,

        @ColumnInfo(name = "token")
        var token: String? = null,

        @ColumnInfo(name = "organization")
        var organization: String? = null,

        @ColumnInfo(name = "remember")
        var remember: Boolean,

        @ColumnInfo(name = "refresh")
        var refresh: String?,

        @ColumnInfo(name = "expiresin")
        var expiresin: Long?
)
