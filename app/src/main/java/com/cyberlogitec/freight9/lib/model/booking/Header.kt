package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Header(
    val id: Int,
    val messageReferenceNumber: String,
    val messageTypeIdentifier: String,
    val messageTypeVersion: String,
    val recipientIdentification: String,
    val senderIdentification: String
): Serializable