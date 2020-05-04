package com.example.aikataulu

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.aikataulu.models.Timetable
import com.google.gson.Gson

// https://developer.android.com/guide/topics/providers/content-provider-basics
class TimetableDataProvider : ContentProvider() {
    private val _data = HashMap<Int, Timetable>()

    companion object {
        const val COLUMN_TIMETABLE = "TIMETABLE"
        val CONTENT_URI = Uri.parse("content://com.example.aikataulu.data_provider")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return MatrixCursor(arrayOf(COLUMN_TIMETABLE)).apply {
            if (selection != null) {
                val widgetId = selection.toInt()
                if (_data.containsKey(widgetId)) addRow(arrayOf(_data[widgetId]))
                else _data[widgetId] = Timetable()
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
        val widgetId = selection!!.toInt()
        val timetableJson = values!!.getAsString(COLUMN_TIMETABLE)

        _data[widgetId] = Gson().fromJson(timetableJson, Timetable::class.javaObjectType)

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