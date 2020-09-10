package com.example.aikataulu.database.contracts

import android.provider.BaseColumns

object ConfigurationContract {
    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${ConfigurationEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${ConfigurationEntry.COLUMN_NAME_WIDGET_ID} INTEGER UNIQUE," +
                "${ConfigurationEntry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS} INTEGER, " +
                "${ConfigurationEntry.COLUMN_NAME_SELECTED_STOP_ID} INTEGER" +
                "${ConfigurationEntry.COLUMN_NAME_AUTO_UPDATE_ENABLED} INTEGER)"

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ConfigurationEntry.TABLE_NAME}"

    object ConfigurationEntry : BaseColumns {
        const val TABLE_NAME = "configurations"
        const val COLUMN_NAME_WIDGET_ID = "widgetid"
        const val COLUMN_NAME_UPDATE_INTERVAL_SECONDS = "updateinterval"
        const val COLUMN_NAME_SELECTED_STOP_ID = "selectedstop"
        const val COLUMN_NAME_AUTO_UPDATE_ENABLED = "autoupdate"
        fun allColumns(): Array<String> {
            return arrayOf(
                COLUMN_NAME_WIDGET_ID,
                COLUMN_NAME_UPDATE_INTERVAL_SECONDS,
                COLUMN_NAME_SELECTED_STOP_ID,
                COLUMN_NAME_AUTO_UPDATE_ENABLED
            )
        }
    }
}
