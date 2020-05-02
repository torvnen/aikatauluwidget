package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.aikataulu.models.Departure
import com.example.aikataulu.ui.ConfigurationActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class WidgetProvider : AppWidgetProvider() {
    companion object {
        private const val TAG = "TIMETABLE.WidgetProvider"
        private val departuresJsonType = object: TypeToken<ArrayList<Departure>>(){}.type
        const val ACTION_RECEIVE_DEPARTURES = "RECEIVE_DEPARTURES"
        const val EXTRA_DEPARTURES = "DEPARTURES"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.i(TAG, "onUpdate()")
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            Log.i(TAG, "Attaching click handler to widget id $appWidgetId")
            val pendingIntent: PendingIntent = Intent(context, ConfigurationActivity::class.java)
                .let { intent ->
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                    intent.action = "configure_widget-$appWidgetId"
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
            val views: RemoteViews = RemoteViews(context.packageName, R.layout.widget).apply {
                }.apply {
                    setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
                }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // Handle receiving of data from service
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null && intent.action == ACTION_RECEIVE_DEPARTURES) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val departures = Gson().fromJson<ArrayList<Departure>>(intent.getStringExtra(EXTRA_DEPARTURES), departuresJsonType)
            Log.d(TAG, "WidgetProvider received ${departures.count()} departures for widget (id=$widgetId)")
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (context != null) {
            Log.i(TAG, "Stopping service...")
            Log.i(TAG, "There are ${appWidgetIds?.size} widget ids.")
            context.stopService(Intent(context, TimetableService::class.java))
        } else Log.w(TAG, "Context is null when widget provider is deleted. Cannot stop service.")
        super.onDeleted(context, appWidgetIds)
    }
}