@file:Suppress("UnnecessaryVariable") // TODO remove

package com.example.aikataulu.api

import com.example.aikataulu.models.Arrival
import com.example.aikataulu.models.Stop

object Api {

    fun getStopsContainingText(text: String): ArrayList<Stop> {
        val stops = ArrayList<Stop>()

        return stops
    }

    fun getArrivalsForStop(): ArrayList<Arrival> {
        val arrivals = ArrayList<Arrival>()

        return arrivals
    }
}