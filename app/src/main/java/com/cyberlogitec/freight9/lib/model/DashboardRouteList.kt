package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class DashboardRouteList (
        val userId: String?,                    // "USR92"
        val offerTypeCode: String?,
        val rteList: List<RouteList>
) : Serializable
{
    data class RouteList(
            val polCode: String,
            val polName: String,
            val podCode: String,
            val podName: String
    ) : Serializable
}