package com.example.aikataulu

import org.junit.Test
import com.example.aikataulu.api.Api
import org.junit.Assert.*

class ApiTests {
    @Test
    fun query_stopByName_getsResults() {
        val stops = Api.getStopsContainingText("herttoniemi")
        assertTrue(stops.count() > 0)
    }
    @Test
    fun query_arrivalsFor_foundStop_getsResults() {
        val stops = Api.getStopsContainingText("töölö")
        assertTrue(stops.count() > 0)
        val stopId = stops.first().hrtId
        val arrivals = Api.getDeparturesForStopId(stopId)
        assertTrue(arrivals.count() > 0)
    }
}
