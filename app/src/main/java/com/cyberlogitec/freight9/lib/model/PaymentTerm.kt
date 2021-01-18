package com.cyberlogitec.freight9.lib.model

data class PaymentTerm (
    val commonDataName: String?,
    val commonErrorCode: String?,
    val commonErrorMessage: String?,
    val commonTreatCode: String?,
    val commonTreatMessage: String?,
    val id: Long? = 0,
    val paymentTermCode: String,
    val paymentTermName: String?
)