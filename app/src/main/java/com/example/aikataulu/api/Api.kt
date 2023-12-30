package com.example.aikataulu.api

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.request.RequestHeaders
import com.example.aikataulu.DeparturesForStopIdQuery
import com.example.aikataulu.StopByIdQuery
import com.example.aikataulu.StopByNameQuery
import com.example.aikataulu.models.Stop
import java.lang.Thread.sleep

object Api {
    private val apolloClient: ApolloClient = ApolloClient.builder().serverUrl(Config.apiUrl).build()
    const val TAG = "TIMETABLE.Api"

    fun getPatternsByStopId(id: String): List<StopByIdQuery.Pattern> {
        val patterns = ArrayList<StopByIdQuery.Pattern>()
        var ready = false
        apolloClient.query(StopByIdQuery(id))
            .enqueue(object : ApolloCall.Callback<StopByIdQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    ready = true
                }

                override fun onResponse(response: Response<StopByIdQuery.Data>) {
                    for (pattern in response.data!!.stop()!!.patterns()!!.iterator()) {
                        patterns.add(pattern)
                    }
                    ready = true
                }
            })

        while (!ready) sleep(10)

        return patterns
    }

    private fun createRequestHeaders(): RequestHeaders {
        return RequestHeaders.builder()
            .addHeader(Config.apiKeyHeaderName, Config.apiKeyValue)
            .build()
    }

    fun getStopsContainingText(
        text: String,
        successCallback: (ArrayList<Stop>) -> Unit,
        failureCallback: (ApolloException) -> Unit
    ) {
        Log.v(TAG, "getStopsContainingText($text, ...)")
        val stops = ArrayList<Stop>()
        apolloClient.query(StopByNameQuery(text))
            .requestHeaders(createRequestHeaders())
            .enqueue(object : ApolloCall.Callback<StopByNameQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, "getStopsContainingText failed", e)
                    failureCallback(e)
                }

                override fun onResponse(response: Response<StopByNameQuery.Data>) {
                    for (queryStop in response.data!!.stops()!!.iterator()) {
                        stops.add(Stop(queryStop.name(), queryStop.gtfsId()))
                    }
                    Log.v(TAG, "getStopsContainingText succeeded")
                    successCallback(stops)
                }
            })
    }

    // Note! This is a blocking call
    fun getStopsContainingText(text: String): ArrayList<Stop> {
        val stops = ArrayList<Stop>()
        var ready = false
        apolloClient.query(StopByNameQuery(text))
            .enqueue(object : ApolloCall.Callback<StopByNameQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    stops.add(Stop(e.message.toString()))
                    ready = true
                }

                override fun onResponse(response: Response<StopByNameQuery.Data>) {
                    for (queryStop in response.data!!.stops()!!.iterator()) {
                        stops.add(Stop(queryStop.name(), queryStop.gtfsId()))
                    }
                    ready = true
                }
            })

        while (!ready) sleep(10)

        return stops
    }

    fun getDeparturesForStopId(stopId: String): ArrayList<DeparturesForStopIdQuery.StoptimesWithoutPattern> {
        val arrivals = ArrayList<DeparturesForStopIdQuery.StoptimesWithoutPattern>()
        var ready = false
        apolloClient.query(DeparturesForStopIdQuery(stopId))
            .requestHeaders(createRequestHeaders())
            .enqueue(object : ApolloCall.Callback<DeparturesForStopIdQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    Log.e(TAG, "getDeparturesForStopId failed", e)

                    ready = true
                }

                override fun onResponse(response: Response<DeparturesForStopIdQuery.Data>) {
                    Log.v(TAG, "getDeparturesForStopId succeeded")
                    val stoptimesWithoutPattern = response.data?.stop()?.stoptimesWithoutPatterns()
                    if (stoptimesWithoutPattern != null) arrivals.addAll(stoptimesWithoutPattern)
                    ready = true
                }
            })

        while (!ready) sleep(10)

        return arrivals
    }
}