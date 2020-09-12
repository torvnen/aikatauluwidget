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
}
