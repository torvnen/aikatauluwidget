package com.example.aikataulu

import org.junit.Test
import com.example.aikataulu.api.Api
import org.junit.Assert.*

class ApiTests {
    @Test
    fun query_arrivalsForStop_getsResults() {
        val stopId = "HSL:1140447"
        Api.getArrivalsForStop()
    }
    @Test
    fun query_stopByName_getsResults() {
        val stops = Api.getStopsContainingText("herttoniemi")
        assertTrue(stops.count() > 0)
    }
}
