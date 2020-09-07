package com.example.aikataulu.models

import android.database.MatrixCursor

class Timetable(val widgetId: Int, val stop: Stop, val departures: List<Departure>) {
    var isViewUpdated: Boolean = false
    companion object {
        const val COLUMN_STOPNAME = "STOPNAME"
        const val COLUMN_DEPARTURE_SCHEDULED = "DEPARTURE_SCHEDULED"
        const val COLUMN_DEPARTURE_REALTIME = "DEPARTURE_REALTIME"
        const val COLUMN_IS_ON_TIME = "IS_ON_TIME"
        const val COLUMN_HEADSIGN = "HEADSIGN"
        const val COLUMN_ROUTE_SHORT_NAME = "ROUTE_SHORT_NAME"
        const val IS_VIEW_UPDATED = "VIEW_UPDATED"
        const val WIDGET_ID = "WIDGET_ID"
        val allColumns = arrayOf(
            COLUMN_STOPNAME,
            COLUMN_ROUTE_SHORT_NAME,
            COLUMN_HEADSIGN,
            COLUMN_DEPARTURE_SCHEDULED,
            COLUMN_DEPARTURE_REALTIME,
            COLUMN_IS_ON_TIME,
            IS_VIEW_UPDATED,
            WIDGET_ID
        )
    }

    fun toMatrixCursor(): MatrixCursor {
        return MatrixCursor(allColumns).apply {
            toMatrixRows().forEach { addRow(it) }
        }
    }

    fun toMatrixRows(): List<Array<*>> {
        return departures.map { d ->
             arrayOf(
                stop.name,
                d.routeShortName,
                d.headsign,
                d.scheduledDeparture,
                d.realtimeDeparture,
                d.isOnTime,
                 isViewUpdated,
                 widgetId
            )
        }
    }
}