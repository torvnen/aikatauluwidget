package com.example.aikataulu.models

import com.example.aikataulu.DeparturesForStopIdQuery
import java.lang.StringBuilder

class Departure(stoptimeWithoutPattern: DeparturesForStopIdQuery.StoptimesWithoutPattern) {
    var scheduledDeparture = "??:??"
    var realtimeDeparture = "??:??"
    val isOnTime = stoptimeWithoutPattern.departureDelay() == null || kotlin.math.abs(stoptimeWithoutPattern.departureDelay()!!.toInt()) < 60
    val headsign = stoptimeWithoutPattern.headsign()
    val routeShortName = stoptimeWithoutPattern.trip()?.route()?.shortName()
    init {
        scheduledDeparture = timeToString(stoptimeWithoutPattern.scheduledDeparture())
        realtimeDeparture = timeToString(stoptimeWithoutPattern.realtimeDeparture())
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$routeShortName   $headsign    $scheduledDeparture")
        if (!isOnTime) sb.append(" ($realtimeDeparture)")
        return sb.toString()
    }
}