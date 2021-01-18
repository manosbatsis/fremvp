package com.cyberlogitec.freight9.lib.apistat

import com.cyberlogitec.freight9.lib.model.Port
import com.cyberlogitec.freight9.lib.model.Schedule
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PostImageResponse(@SerializedName("image_url") val imageUrl: String? = null)

/**
 *  {
 *      "dataOwnrPtrId": "HLC",
 *      "mstrCtrkNr": "MST-F9_HLC-201908-01",
 *      "ipolCd": null,
 *      "ipodCd": null,
 *      "rteSeq": 1,
 *      "porCd": "JPOSA",
 *      "polCd": "JPTYO",
 *      "podCd": "CAVAN",
 *      "delCd": "USCHI"
 *  },
 */
data class PostServiceRouteResponse(
        val dataOwnrPtrId : String?,
        val mstrCtrkNr : String?,
        var ipolCd : String?,
        var ipodCd : String?,
        val rteSeq : Int?,
        val porCd : String?,
        val porNm : String?,
        val polCd : String?,
        val polNm : String?,
        val podCd : String?,
        val podNm : String?,
        val delCd : String?,
        val delNm : String?
)

data class TokenResponse(
        val access_token: String?,
        val token_type: String?,
        val refresh_token: String?,
        val expires_in: Long?,
        val scope: String?
)

data class SchedulePageInfo(
        @SerializedName("number")
        val requestPage: Int,
        @SerializedName("totalPages")
        val totalPages: Int,
        @SerializedName(value = "content")
        val content: List<Schedule>
)

data class PortPageInfo(
        @SerializedName("number")
        val requestPage: Int,
        @SerializedName("totalPages")
        val totalPages: Int,
        @SerializedName(value = "content")
        val content: List<Port>
)

data class PostNewServicelaneDueDateResponse(
        @SerializedName("vslSvcelaneCd")
        val vslSvcelaneCd : String?,

        @SerializedName("locDivCd")
        val locDivCd : String?,

        @SerializedName("locCd")
        val locCd : String?,

        @SerializedName("dirCd")
        val dirCd : String?,

        @SerializedName("portRotSeq")
        val portRotSeq : String?,

        @SerializedName("dueEtdDt")
        val dueEtdDt : String?
)

data class NewServicelaneResponse(
        @SerializedName("cryrCd")
        val cryrCd: String?,

        @SerializedName("svceLaneCd")
        val svceLaneCd: String?
)

data class NewServicelanePolsPodsResponse(
        @SerializedName("cryrCd")
        val cryrCd: String?,

        @SerializedName("svceLaneCd")
        val svceLaneCd: String?,

        @SerializedName("locDivCd")
        val locDivCd: String?,

        @SerializedName("locCd")
        val locCd: String?,

        @SerializedName("locNm")
        val locNm: String?,

        @SerializedName("dirCd")
        val dirCd: String?,

        @SerializedName("portRotSeq")
        val portRotSeq: String?
)


