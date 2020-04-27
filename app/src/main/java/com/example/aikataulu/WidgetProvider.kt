package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import android.widget.RemoteViews

class WidgetProvider : AppWidgetProvider() {
    var isServiceStarted = false

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget
            ).apply {
                //setOnClickPendingIntent(R.id.button, pendingIntent)

            }

            // Start Service
            Log.d("TIMETABLE", "serviceFullName $TimetableService::class.java")
            if (!isServiceStarted) {
                Log.i("TIMETABLE", "Starting service...")
                context.startService(Intent(context, TimetableService::class.java))
                isServiceStarted = true
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context != null) {
            Log.i("TIMETABLE", "Stopping service...")
            context.stopService(Intent(context, TimetableService::class.java))
        } else Log.w("TIMETABLE", "Context is null when widget provider is deleted. Cannot stop service.")
        super.onDeleted(context, appWidgetIds)
    }
}