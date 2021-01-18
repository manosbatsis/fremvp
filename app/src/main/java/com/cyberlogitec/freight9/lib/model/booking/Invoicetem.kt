package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Invoicetem(
        var isExpand: Boolean = false,
        val paymentState: String,    //paymentInitialState
        val invoiceNo: String,
        val sellerPhone: String,
        val sellerEmail: String,
        val sellerAddress: String,
        val buyerAddress: String,
        val dateOfIssue: String

): Serializable

object PaymentInitialState {
    const val PAYMENT_INITIAL = "01"
    const val PAYMENT_MIDTERM = "02"
    const val PAYMENT_REMAINDER = "03"
}
