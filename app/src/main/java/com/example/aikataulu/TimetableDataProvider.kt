package com.example.aikataulu

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.example.aikataulu.models.Timetable
import com.google.gson.Gson

// https://developer.android.com/guide/topics/providers/content-provider-basics
class TimetableDataProvider : ContentProvider() {
    private val _data = ArrayList<Timetable>()

    companion object {
        const val COLUMN_TIMETABLE = "TIMETABLE"
        const val TAG = "TIMETABLE.DataProvider"
        val CONTENT_URI: Uri = Uri.parse("content://com.example.android.aikataulu.data_provider")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "Received query with selection $selection")
        // Return whole data set if selection is null.
        val data = if (selection == null) _data else _data.filter { it.stop.hrtId == selection }
        return MatrixCursor(Timetable.allColumns).apply {
            data.forEach {
                it.toMatrixRows().forEach {row ->
                    addRow(row)
                }
            }
        }
    }

    override fun onCreate(): Boolean {
        return true
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

        _data.removeIf {
            it.stop.hrtId == stopId
        }
        _data.add(timetable)

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
        return "vnd.android.cursor.dir/vnd.aikataulu.models.timetable"
    }
}