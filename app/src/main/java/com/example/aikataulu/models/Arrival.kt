package com.example.aikataulu.models

import com.example.aikataulu.ArrivalsForStopIdQuery

class Arrival(stoptimeWithoutPattern: ArrivalsForStopIdQuery.StoptimesWithoutPattern) {
    var scheduledDeparture = "??:??"
    var realtimeDeparture = "??:??"
    val isLate = stoptimeWithoutPattern.departureDelay() != null && stoptimeWithoutPattern.departureDelay()!!.toInt() > 60
    val headsign = stoptimeWithoutPattern.headsign()
    init {
        scheduledDeparture = timeToString(stoptimeWithoutPattern.scheduledDeparture())
        realtimeDeparture = timeToString(stoptimeWithoutPattern.realtimeDeparture())
    }
}