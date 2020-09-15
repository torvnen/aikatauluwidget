package com.example.aikataulu

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.aikataulu.api.Api
import com.example.aikataulu.database.contracts.StopContract
import com.example.aikataulu.models.Departure
import com.example.aikataulu.models.Stop
import com.example.aikataulu.models.Timetable
import com.example.aikataulu.models.TimetableConfiguration
import com.example.aikataulu.providers.TimetableDataProvider
import com.google.gson.Gson
import java.util.*

class TimetableTask(private val context: Context, private val config: TimetableConfiguration) :
    TimerTask() {
    private var stop: Stop

    init {
        val cursor = context.contentResolver.query(
            TimetableDataProvider.STOP_URI,
            null,
            "${StopContract.StopEntry.COLUMN_NAME_HRTID} = ?",
            arrayOf(config.stopId),
            null,
            null
        )
        cursor!!.moveToFirst()
        stop = Stop(
            cursor.getString(cursor.getColumnIndex(StopContract.StopEntry.COLUMN_NAME_STOPNAME)),
            cursor.getString(cursor.getColumnIndex(StopContract.StopEntry.COLUMN_NAME_HRTID))
        )
    }

    /**
     * Execute an API call to find departures for a stop, update the [TimetableDataProvider] with
     * the new departure values, and notify any [android.database.ContentObserver] of the changes.
     * Uses the [TimetableDataProvider.TIMETABLE_DATA_URI] to update
     * and notify any [android.content.ContentProvider]s.
     */
    override fun run() {
        val widgetId = config.widgetId!!
        val departures =
            Api.getDeparturesForStopId(config.stopId!!)
                .map { Departure(it) }
        Log.d(
            TAG,
            "[WidgetId=${config.widgetId}]: Received ${departures.count()} departures for stop ${config.stopId})"
        )

        // Update content
        context.contentResolver.update(
            TimetableDataProvider.TIMETABLE_DATA_URI,
            ContentValues().apply {
                put(
                    TimetableDataProvider.COLUMN_TIMETABLE,
                    Gson().toJson(
                        Timetable(
                            widgetId,
                            stop,
                            departures
                        )
                    )
                )
            },
            stop.hrtId,
            null
        )
        context.contentResolver.notifyChange(TimetableDataProvider.TIMETABLE_DATA_URI, null)
    }

    companion object {
        private const val TAG = "TIMETABLE.TimetableTask"
    }
}