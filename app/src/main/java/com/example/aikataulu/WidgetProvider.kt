package com.example.aikataulu;

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetProvider : AppWidgetProvider() {
    private val TAG = "TIMETABLE.WidgetProvider"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Start Service
        Log.i(TAG, "Invoking service startup")
        val intent = Intent(context, TimetableService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        context.startForegroundService(intent)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context != null) {
            Log.i(TAG, "Stopping service...")
            context.stopService(Intent(context, TimetableService::class.java))
        } else Log.w(TAG, "Context is null when widget provider is deleted. Cannot stop service.")
        super.onDeleted(context, appWidgetIds)
    }
}