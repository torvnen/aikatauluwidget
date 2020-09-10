package com.example.aikataulu

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.aikataulu.database.TimetableDbHelper
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.models.Timetable
import com.google.gson.Gson

// https://developer.android.com/guide/topics/providers/content-provider-basics
class TimetableDataProvider : ContentProvider() {
    private val _data = ArrayList<Timetable>()
    private lateinit var dbHelper: TimetableDbHelper

    companion object {
        const val COLUMN_TIMETABLE = "TIMETABLE"
        const val TAG = "TIMETABLE.DataProvider"
        val TIMETABLE_DATA_URI: Uri = Uri.parse("content://com.example.android.aikataulu.data_provider")
        val CONFIGURATION_URI: Uri =
            Uri.parse("content://com.example.android.aikataulu.data_provider/configuration")
        val STOP_URI: Uri =
            Uri.parse("content://com.example.android.aikataulu.data_provider/stop")
    }

    override fun onCreate(): Boolean {
        dbHelper = TimetableDbHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "Received query with selection $selection")
        if (uri == TIMETABLE_DATA_URI) {
            // Return out-of-date data sets
            val data =
                if (selection == null) _data.filter { !it.isViewUpdated } else _data.filter { it.widgetId.toString() == selection }
            return MatrixCursor(Timetable.allColumns).apply {
                data.forEach {
                    it.toMatrixRows().forEach { row ->
                        addRow(row)
                    }
                }
            }
        } else if (uri == CONFIGURATION_URI) {
            val widgetId = selection?.toInt() ?: 0
            return dbHelper.getConfigByWidgetId(widgetId)
            /*
            *
        if (cursor != null && cursor.moveToFirst()) {
            val updateIntervalS = cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS))
            val stopId = cursor.getStringOrNull(cursor.getColumnIndex(entry.COLUMN_NAME_SELECTED_STOP_ID))
            val isAutoUpdateEnabled = cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_AUTO_UPDATE_ENABLED)) == 1
            return TimetableConfigurationData(updateIntervalS, stopId, isAutoUpdateEnabled)
        }
            *
            * */
        } else if (uri == STOP_URI) {

        }
        return null
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (uri == TIMETABLE_DATA_URI) {
            val stopId = selection!!
            val timetableJson = values!!.getAsString(COLUMN_TIMETABLE)
            val timetable = Gson().fromJson(timetableJson, Timetable::class.javaObjectType)
            timetable.isViewUpdated = false
            Log.i(TAG, "Stop $stopId has ${timetable.departures.size} departures")

            _data.removeIf {
                it.stop.hrtId == stopId
            }
            _data.add(timetable)

        } else if (uri == CONFIGURATION_URI) {
            if (selection != null && values != null) {
                val widgetId = selection.toInt()
                if (widgetId > 0) {
                    val newInterval = values.getAsInteger(ConfigurationContract.ConfigurationEntry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS)
                    if (newInterval != null) {
                        dbHelper.updateInterval(widgetId, newInterval.toInt())

                    }
                }
            } else return 0
        }
        context!!.contentResolver.notifyChange(uri, null)
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0 // Deletion not supported.
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null // Insertion not supported.
    }

    override fun getType(uri: Uri): String? {
        // Mime type "cursor.dir" implies a cursor for 0..n items
        // , the rest is for the returnee object type
        when (uri) {
            CONFIGURATION_URI -> return "vnd.android.cursor.dir/vnd.aikataulu.timetableconfigurationdata"
            TIMETABLE_DATA_URI -> return "vnd.android.cursor.dir/vnd.aikataulu.models.timetable"
        }
        return null
    }
}