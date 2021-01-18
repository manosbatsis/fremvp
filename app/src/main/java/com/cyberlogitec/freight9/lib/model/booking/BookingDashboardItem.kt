package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class BookingDashboardItem(
        var isExpand: Boolean = false,
        val booking: Booking,
        val commodity: List<Commodity>,
        val container: List<Container>,
        val header: Header,
        val id: Int,
        val inventoryNumber: String,
        val messageType: Int,
        val party: Party,
        val remark: Remark,
        val transport: List<Transport>
): Serializable