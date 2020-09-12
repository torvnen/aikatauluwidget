package com.example.aikataulu

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.net.Uri
import android.util.Log
import com.example.aikataulu.database.TimetableDbHelper
import com.example.aikataulu.database.contracts.StopContract
import com.example.aikataulu.models.Stop

class StopProvider : ContentProvider() {
    private lateinit var dbHelper: TimetableDbHelper

    companion object {
        private val entry = StopContract.StopEntry
        const val TAG = "TIMETABLE.ConfigurationProvider"
        val STOP_URI: Uri =
            Uri.parse("content://com.example.android.aikataulu.stop_provider")

        fun getStopByIdOrNull(hrtId: String, context: Context): Stop? {
            val cursor = context.contentResolver.query(
                STOP_URI,
                null,
                "${entry.COLUMN_NAME_HRTID} = ?",
                arrayOf(hrtId),
                null,
                null
            )
            return if (cursor?.moveToFirst() == true) entry.cursorToPoco(cursor) else null
        }
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
        return dbHelper.readableDatabase.query(
            entry.TABLE_NAME,
            entry.allColumns(),
            selection,
            selectionArgs,
            null,
            null,
            null
        )
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return dbHelper.writableDatabase.update(
            entry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val id = dbHelper.writableDatabase.delete(
            entry.TABLE_NAME,
            selection,
            selectionArgs
        )
        Log.d(TAG, "Deleted row id $id")
        return id
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val id = dbHelper.writableDatabase.insertWithOnConflict(
            entry.TABLE_NAME,
            null,
            values,
            CONFLICT_REPLACE
        )
        Log.d(TAG, "Inserted or updated a row with id=$id")
        return uri
    }

    override fun getType(uri: Uri): String? {
        // Mime type "cursor.dir" implies a cursor for 0..n items
        // , the rest is for the returnee object type
        when (uri) {
            STOP_URI -> return "vnd.android.cursor.dir/vnd.aikataulu.models.stop"
        }
        return null
    }
}