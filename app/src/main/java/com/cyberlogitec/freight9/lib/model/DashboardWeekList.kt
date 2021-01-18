package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class DashboardWeekList (
        val userId: String?,                    // "USR92"
        val offerTypeCode: String?,
        val polCode: String?,
        val podCode: String?,
        val baseYearWeek: List<String>
) : Serializable