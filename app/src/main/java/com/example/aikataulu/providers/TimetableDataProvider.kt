package com.example.aikataulu.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.net.Uri
import android.util.Log
import com.example.aikataulu.database.TimetableDbHelper
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.database.contracts.StopContract
import com.example.aikataulu.models.Timetable
import com.google.gson.Gson


class TimetableDataProvider : ContentProvider() {
    private val _data = HashMap<Int, Timetable>()
    private lateinit var dbHelper: TimetableDbHelper

    companion object {
        const val COLUMN_TIMETABLE = "TIMETABLE"
        const val TAG = "TIMETABLE.DataProvider"
        val TIMETABLE_DATA_URI: Uri =
            Uri.parse("content://com.example.android.aikataulu.data_provider")
        val CONFIGURATION_URI: Uri =
            Uri.parse("content://com.example.android.aikataulu.configuration_provider")
        val STOP_URI: Uri =
            Uri.parse("content://com.example.android.aikataulu.stop_provider")
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
        // Return out-of-date data sets
        val widgetId = selectionArgs!!.first().toInt()
        val data = if (_data.containsKey(widgetId)) _data[widgetId] else null
        return MatrixCursor(Timetable.allColumns).apply {
            data?.toMatrixRows()?.forEach { row ->
                addRow(row)
            }
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val stopId = selection!!
        val timetableJson = values!!.getAsString(COLUMN_TIMETABLE)
        val timetable = Gson().fromJson(timetableJson, Timetable::class.javaObjectType)

        Log.i(TAG, "Stop $stopId has ${timetable.departures.size} departures")
        _data[timetable.widgetId] = timetable

        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uri == CONFIGURATION_URI) {
            val entry = ConfigurationContract.ConfigurationEntry
            val rowsAffected = dbHelper.writableDatabase.delete(
                entry.TABLE_NAME,
                selection,
                selectionArgs
            )
            Log.d(TAG, "A delete configuration operation affected $rowsAffected rows.")
        }
        return 0 // Deletion not supported.
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uri == CONFIGURATION_URI) {
            val entry = ConfigurationContract.ConfigurationEntry
            val id = dbHelper.writableDatabase.insertWithOnConflict(
                entry.TABLE_NAME,
                null,
                values,
                CONFLICT_REPLACE
            )
            Log.d(TAG, "Inserted or updated a row with id=$id")
        } else if (uri == STOP_URI) {
            val entry = StopContract.StopEntry
            val id = dbHelper.writableDatabase.insertWithOnConflict(
                entry.TABLE_NAME,
                null,
                values,
                CONFLICT_REPLACE
            )
            Log.d(TAG, "Inserted or updated a row with id=$id")
        }
        return null // Insertion not supported.
    }

    override fun getType(uri: Uri): String? {
        // Mime type "cursor.dir" implies a cursor for 0..n items
        // , the rest is for the returnee object type
        when (uri) {
            TIMETABLE_DATA_URI -> return "vnd.android.cursor.dir/vnd.aikataulu.models.timetable"
        }
        return null
    }
}