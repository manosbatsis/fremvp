package com.cyberlogitec.freight9.lib.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "schedules")
data class Schedule(
        @PrimaryKey(autoGenerate = true)
        var pid: Long? = null,

        @ColumnInfo(name = "id")
        @SerializedName("id")
        var id: String? = "",

        @ColumnInfo(name = "serviceLaneCode")
        @SerializedName("serviceLaneCode")
        var serviceLaneCode: String = "",

        @ColumnInfo(name = "serviceLaneName")
        @SerializedName("serviceLaneName")
        var serviceLaneName: String = "",

        @ColumnInfo(name = "vesselName")
        @SerializedName("vesselName")
        var vesselName: String = "",

        @ColumnInfo(name = "vesselFlag")
        @SerializedName("vesselFlag")
        var vesselFlag: String = "",

        @ColumnInfo(name = "vesselCapacity")
        @SerializedName("vesselCapacity")
        var vesselCapacity: String = "",

        @ColumnInfo(name = "voyageCode")
        @SerializedName("voyageCode")
        var voyageCode: String = "",

        @ColumnInfo(name = "directionCode")
        @SerializedName("directionCode")
        var directionCode: String = "",

        @ColumnInfo(name = "polSeq")
        @SerializedName("polSeq")
        var polSeq: String = "",

        @ColumnInfo(name = "polCode")
        @SerializedName("polCode")
        var polCode: String = "",

        @ColumnInfo(name = "polName")
        @SerializedName("polName")
        var polName: String = "",

        @ColumnInfo(name = "polCountryCode")
        @SerializedName("polCountryCode")
        var polCountryCode: String = "",

        @ColumnInfo(name = "polCountryName")
        @SerializedName("polCountryName")
        var polCountryName: String = "",

        @ColumnInfo(name = "polSubRegionCode")
        @SerializedName("polSubRegionCode")
        var polSubRegionCode: String = "",

        @ColumnInfo(name = "polSubRegionName")
        @SerializedName("polSubRegionName")
        var polSubRegionName: String = "",

        @ColumnInfo(name = "polRegionCode")
        @SerializedName("polRegionCode")
        var polRegionCode: String = "",

        @ColumnInfo(name = "polRegionName")
        @SerializedName("polRegionName")
        var polRegionName: String = "",

        @ColumnInfo(name = "polETADate")
        @SerializedName("polETADate")
        var polETADate: String = "",

        @ColumnInfo(name = "polETATime")
        @SerializedName("polETATime")
        var polETATime: String = "",

        @ColumnInfo(name = "polETDDate")
        @SerializedName("polETDDate")
        var polETDDate: String = "",

        @ColumnInfo(name = "polETDTime")
        @SerializedName("polETDTime")
        var polETDTime: String = "",

        @ColumnInfo(name = "productYearWeek")
        @SerializedName("productYearWeek")
        var productYearWeek: String = "",

        @ColumnInfo(name = "polServiceYearWeek")
        @SerializedName("polServiceYearWeek")
        var polServiceYearWeek: String = "",

        @ColumnInfo(name = "podSeq")
        @SerializedName("podSeq")
        var podSeq: String = "",

        @ColumnInfo(name = "podCode")
        @SerializedName("podCode")
        var podCode: String = "",

        @ColumnInfo(name = "podName")
        @SerializedName("podName")
        var podName: String = "",

        @ColumnInfo(name = "podCountryCode")
        @SerializedName("podCountryCode")
        var podCountryCode: String = "",

        @ColumnInfo(name = "podCountryName")
        @SerializedName("podCountryName")
        var podCountryName: String = "",

        @ColumnInfo(name = "podSubRegionCode")
        @SerializedName("podSubRegionCode")
        var podSubRegionCode: String = "",

        @ColumnInfo(name = "podSubRegionName")
        @SerializedName("podSubRegionName")
        var podSubRegionName: String = "",

        @ColumnInfo(name = "podRegionCode")
        @SerializedName("podRegionCode")
        var podRegionCode: String = "",

        @ColumnInfo(name = "podRegionName")
        @SerializedName("podRegionName")
        var podRegionName: String = "",

        @ColumnInfo(name = "podETADate")
        @SerializedName("podETADate")
        var podETADate: String = "",

        @ColumnInfo(name = "podETATime")
        @SerializedName("podETATime")
        var podETATime: String = "",

        @ColumnInfo(name = "podETDDate")
        @SerializedName("podETDDate")
        var podETDDate: String = "",

        @ColumnInfo(name = "podETDTime")
        @SerializedName("podETDTime")
        var podETDTime: String = "",

        @ColumnInfo(name = "podServiceYearWeek")
        @SerializedName("podServiceYearWeek")
        var podServiceYearWeek: String = "",

        @ColumnInfo(name = "ownerCode")
        @SerializedName("ownerCode")
        var ownerCode: String = "",

        @ColumnInfo(name = "deleteYn")
        @SerializedName("deleteYn")
        var deleteYn: String = "",

        ////////////////////////////////////////////////////////////////////////////////////////////
        // + added by jkim for sell offer route selection

        var isLaneChecked: Boolean = false,
        var isPolChecked: Boolean = false,
        var isPodChecked: Boolean = false

        ////////////////////////////////////////////////////////////////////////////////////////////

) : Serializable