package com.cyberlogitec.freight9.lib.model

data class PaymentPlan (
    val commonDataName: String?=null,
    val commonErrorCode: String?=null,
    val commonErrorMessage: String?=null,
    val commonTreatCode: String?=null,
    val commonTreatMessage: String?=null,
    val id: Long? = 0,
    val paymentPlanCode: String,
    val paymentPlanName: String?=null,
    val initialPaymentRatio: Float = 0.0f,
    val middlePaymentRatio: Float = 0.0f,
    val balancePaymentRatio: Float = 0.0f
)