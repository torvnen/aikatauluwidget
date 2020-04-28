package com.example.aikataulu.models

import com.example.aikataulu.ArrivalsForStopIdQuery
import java.lang.StringBuilder

class Arrival(stoptimeWithoutPattern: ArrivalsForStopIdQuery.StoptimesWithoutPattern) {
    var scheduledDeparture = "??:??"
    var realtimeDeparture = "??:??"
    val isOnTime = stoptimeWithoutPattern.departureDelay() != null && kotlin.math.abs(stoptimeWithoutPattern.departureDelay()!!.toInt()) > 60
    val headsign = stoptimeWithoutPattern.headsign()
    init {
        scheduledDeparture = timeToString(stoptimeWithoutPattern.scheduledDeparture())
        realtimeDeparture = timeToString(stoptimeWithoutPattern.realtimeDeparture())
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$headsign\t$scheduledDeparture")
        if (!isOnTime) sb.append("($realtimeDeparture)")
        return sb.toString()
    }
}