package com.example.aikataulu.models

fun timeToString(timestamp: Int?): String {
    if (timestamp == null) return "??:??"
    val h = timestamp / (60 * 60)
    val m = (timestamp - (h * 60 * 60)) / 60

    return "${toDoubleDigitString(h)}:${toDoubleDigitString(m)}"
}

fun toDoubleDigitString(i: Int?): String {
    if (i == null) return "??"
    val j = i.toInt()
    return if (j >= 10) j.toString() else "0${j}"
}