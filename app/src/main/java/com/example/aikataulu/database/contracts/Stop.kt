package com.example.aikataulu.database.contracts

import android.provider.BaseColumns

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
    }
}
