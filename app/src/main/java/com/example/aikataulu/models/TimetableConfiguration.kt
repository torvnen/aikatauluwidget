package com.example.aikataulu.models

data class TimetableConfiguration(
    var updateIntervalS: Int = 10,
    var stopId: String? = null,
    var autoUpdate: Boolean = false,
    var widgetId: Int? = null,
    var stopName: String? = null
) {
    fun getUpdateIntervalText(): String {
        val m = updateIntervalS / 60
        val s = updateIntervalS % 60
        return "Every${if (m > 0) " $m minute${if (m == 1) "" else "s"}" else ""}${if (s > 0) " $s seconds" else ""}"
    }
}