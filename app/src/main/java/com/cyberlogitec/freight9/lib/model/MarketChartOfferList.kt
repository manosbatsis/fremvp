package com.cyberlogitec.freight9.lib.model

import com.google.gson.annotations.SerializedName

data class MarketChartOfferList(
        @SerializedName("id")
        var id: String,

        @SerializedName("topicName")
        var topicName: String,

        @SerializedName("marketTypeCode")
        var marketTypeCode: String,

        @SerializedName("companyCodes")
        var companyCodes: ArrayList<String>,

        @SerializedName("pol")
        var pol: String,

        @SerializedName("pod")
        var pod: String,

        @SerializedName("rdtermCode")
        var rdtermCode: String,

        @SerializedName("containerTypeCode")
        var containerTypeCode: String,

        @SerializedName("paymentTermCode")
        var paymentTermCode: String,

        @SerializedName("qtyUnit")
        var qtyUnit: String = "T",

        @SerializedName("matrixWeekPrice")
        var matrixWeekPrice: ArrayList<MarketOfferList>

        )






