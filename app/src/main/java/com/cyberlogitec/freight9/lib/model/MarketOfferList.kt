package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName

data class MarketOfferList(
        @SerializedName("baseWeek")
        var week: String,

        @SerializedName("price")
        var price: Double,

        @SerializedName("offerTypeCode")
        var offerTypeCode: String,

        @SerializedName("qty")
        var qty: Int

        )






