@file:Suppress("UnnecessaryVariable") // TODO remove

package com.example.aikataulu.api

import android.os.AsyncTask
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.example.aikataulu.StopByNameQuery
import com.example.aikataulu.models.Arrival
import com.example.aikataulu.models.Stop
import okhttp3.internal.wait
import java.lang.Thread.sleep

class CallbackTest : ApolloCall.Callback<StopByNameQuery.Data>() {
    override fun onFailure(e: ApolloException) {
        TODO("Not yet implemented")
    }

    override fun onResponse(response: Response<StopByNameQuery.Data>) {
        TODO("Not yet implemented")
    }

}

object Api {
    private val apolloClient: ApolloClient = ApolloClient.builder().serverUrl(Config.apiUrl).build()

    // Note! This is a blocking call
    fun getStopsContainingText(text: String): ArrayList<Stop> {
        val stops = ArrayList<Stop>()
        var ready = false
        apolloClient.query(StopByNameQuery(text)).enqueue(object: ApolloCall.Callback<StopByNameQuery.Data>() {
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

    fun getArrivalsForStop(): ArrayList<Arrival> {
        val arrivals = ArrayList<Arrival>()

        return arrivals
    }
}