package com.cyberlogitec.freight9.lib.model.booking

import java.io.Serializable

data class Remark(
    val amendingReason: String,
    val amsFilingNvoccSCAC: String,
    val canadianCargoControlNo: String,
    val customerAMSFilingFlag: String,
    val customsExportDUCR: String,
    val generalInfo: String,
    val id: Int
): Serializable