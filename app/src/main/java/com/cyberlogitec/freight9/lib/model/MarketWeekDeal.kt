package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class MarketWeekDeal(
        @SerializedName("week")
        var week: String,

        @SerializedName("dealDate")
        var dealDate: Date,

        @SerializedName("lastprice")
        var lastprice: Double,

        @SerializedName("openprice")
        var openprice: Double,

        @SerializedName("highprice")
        var highprice: Double,

        @SerializedName("lowprice")
        var lowprice: Double,

        @SerializedName("volume")
        var volume: Int

        )

