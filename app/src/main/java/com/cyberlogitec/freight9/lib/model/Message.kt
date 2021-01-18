package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
        {
                "eventType":"TradeOfferDealt",
                "timestamp":"202002251420557419",
                "errorCode":"0",
                "errorMessage":"Offer is dealt successfully with DealNumber:D202002251420073808430013187\nTimestamp:2020-02-25 14:20 557419\nNumber:P202002251420062008140001182",
                "memo":null,
                "userId":"hlcuser",
                "offerNumber":"P202002251420062008140001182",
                "offerChangeSeq":0,
                "referenceOfferNumber":"F202002251420068476780074963",
                "referenceChangeSeq":0,
                "dealNumber":"D202002251420073808430013187",
                "dealChangeSeq":0,
                "messageTitle":"Trading System Message"
        }
 */

@Entity(tableName = "messages")
data class Message(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "msgSeq")
        var msgSeq: Long ?,

        @ColumnInfo(name = "eventType")
        var eventType: String ? = null,

        @ColumnInfo(name = "timestamp")
        var timestamp: String ? = null,

        @ColumnInfo(name = "errorCode")
        var errorCode: String ? = null,

        @ColumnInfo(name = "errorMessage")
        var errorMessage: String ? = null,

        @ColumnInfo(name = "memo")
        var memo: String ? = null,

        @ColumnInfo(name = "userId")
        var userId: String ? = null,

        @ColumnInfo(name = "offerNumber")
        var offerNumber: String ? = null,

        @ColumnInfo(name = "offerChangeSeq")
        var offerChangeSeq: Long = 0,

        @ColumnInfo(name = "referenceOfferNumber")
        var referenceOfferNumber: String ? = null,

        @ColumnInfo(name = "referenceChangeSeq")
        var referenceChangeSeq: Long = 0,

        @ColumnInfo(name = "dealNumber")
        var dealNumber: String ? = null,

        @ColumnInfo(name = "dealChangeSeq")
        var dealChangeSeq: Long = 0,

        @ColumnInfo(name = "messageTitle")
        var messageTitle: String ? = null,

        @ColumnInfo(name = "readYn")
        var readYn: String = "N"

) : Serializable