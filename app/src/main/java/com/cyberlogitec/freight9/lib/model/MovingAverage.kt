package com.cyberlogitec.freight9.lib.model

import java.io.Serializable

data class MovingAverage(
        var selected: Boolean?,
        var peroid: String?,
        var offset: String?,
        var color: Int
): Serializable

