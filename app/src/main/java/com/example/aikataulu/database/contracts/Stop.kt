package com.example.aikataulu.database.contracts

import android.database.Cursor
import android.provider.BaseColumns
import com.example.aikataulu.models.Stop

object StopContract {
    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${StopEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${StopEntry.COLUMN_NAME_HRTID} TEXT," +
                "${StopEntry.COLUMN_NAME_STOPNAME} TEXT)"

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${StopEntry.TABLE_NAME}"

    object StopEntry : BaseColumns {
        const val TABLE_NAME = "stops"
        const val COLUMN_NAME_HRTID = "hrtid"
        const val COLUMN_NAME_STOPNAME = "name"
        fun allColumns(): Array<String> {
            return arrayOf(
                COLUMN_NAME_HRTID,
                COLUMN_NAME_STOPNAME
            )
        }
        fun cursorToPoco(cursor: Cursor?): Stop? {
            return if (cursor != null) {
                val entry = StopEntry
                val hrtId =
                    cursor.getString(cursor.getColumnIndex(entry.COLUMN_NAME_HRTID))
                val name =
                    cursor.getString(cursor.getColumnIndex(entry.COLUMN_NAME_STOPNAME))
                Stop(name, hrtId)
            } else null
        }
    }
}
