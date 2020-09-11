package com.example.aikataulu.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.database.contracts.StopContract

class TimetableDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 6
        const val DATABASE_NAME = "aikataulu.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ConfigurationContract.SQL_CREATE_ENTRIES)
        db.execSQL(StopContract.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ConfigurationContract.SQL_DELETE_ENTRIES)
        db.execSQL(StopContract.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun getStopByHrtId(hrtId: String): Cursor? {
        val db = readableDatabase
        val entry = StopContract.StopEntry
        val cursor = db.query(
            entry.TABLE_NAME,
            entry.allColumns(),
            "${entry.COLUMN_NAME_HRTID} = ?",
            arrayOf(hrtId),
            null,
            null,
            null
        )
        return cursor
    }

    fun getConfigByWidgetId(widgetId: Int): Cursor? {
        val db = readableDatabase
        val entry = ConfigurationContract.ConfigurationEntry
        val cursor = db.query(
            entry.TABLE_NAME,
            entry.allColumns(),
            "${entry.COLUMN_NAME_WIDGET_ID} = ?",
            arrayOf(widgetId.toString()),
            null,
            null,
            null
        )
        return cursor
    }

    fun updateInterval(widgetId: Int, newInterval: Int) {
        /*
        Proper way:
        val db = dbHelper.writableDatabase

        // New value for one column
        val title = "MyNewTitle"
        val values = ContentValues().apply {
            put(FeedEntry.COLUMN_NAME_TITLE, title)
        }

        // Which row to update, based on the title
        val selection = "${FeedEntry.COLUMN_NAME_TITLE} LIKE ?"
        val selectionArgs = arrayOf("MyOldTitle")
        val count = db.update(
                FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs)

         */
        /* Easy way: */
        val entry = ConfigurationContract.ConfigurationEntry
        writableDatabase.execSQL("UPDATE ${entry.TABLE_NAME} SET ${entry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS} = $newInterval WHERE ${entry.COLUMN_NAME_WIDGET_ID} = $widgetId")
    }
}
