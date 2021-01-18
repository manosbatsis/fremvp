package com.cyberlogitec.freight9.lib.util

fun String.getContainerTypeSizeName(): String {
    when(this.toUpperCase()) {
        "22B0", "22G0", "22H0", "22P1", "22P3", "22T0", "22U1", "22U6", "22V0", "29P0", "28U1" -> {return "Dry 20'"}
        "22R1" -> {return "Reefer 20'"}
        "42G0", "42H0", "42P1", "42P3", "42T0", "42U1", "42U6", "42V0", "45G0", "49P0" -> {return "Dry 40'"}
        "42R1" -> {return "Reefer 40'"}
        "45R1" -> {return "Reefer 40' HC"}
        "L5G1" -> {return "45' HC"}
        "L5R1" -> {return "Reefer 45' HC"}
        "45U6" -> {return "40' HC"}
    }
    return ""
}

fun String.getContainerSizeName():String {
    when(this.toUpperCase()) {
        "22B0", "22G0", "22H0", "22P1", "22P3", "22T0", "22U1", "22U6", "22V0", "29P0", "28U1" -> {return "20'"}
        "22R1" -> {return "20'"}
        "42G0", "42H0", "42P1", "42P3", "42T0", "42U1", "42U6", "42V0", "45G0", "49P0" -> {return "40'"}
        "42R1" -> {return "40'"}
        "45R1" -> {return "40'"}
        "L5G1" -> {return "45'"}
        "L5R1" -> {return "45'"}
        "45U6" -> {return "40'"}
    }
    return ""
}
