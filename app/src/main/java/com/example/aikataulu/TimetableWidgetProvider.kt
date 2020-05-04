package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.aikataulu.models.Departure
import com.example.aikataulu.ui.ConfigurationActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class TimetableWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val TAG = "TIMETABLE.WidgetProvider"
        const val EXTRA_DEPARTURES = "EXTRA_DEPARTURES"
        val departuresJsonType: Type = object: TypeToken<ArrayList<Departure>>(){}.type

        // Send data to be handled by ViewFactory
        // TODO Do not send data via this. Only notify of changes - ViewFactory should query DataProvider.
        fun sendUpdateWidgetBroadcast(context: Context, widgetId: Int, departures: List<Departure>) {
            context.sendBroadcast(Intent(context, TimetableWidgetProvider::class.java)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .putExtra(EXTRA_DEPARTURES, Gson().toJson(departures)))
        }
    }

    // Handle receiving of data from service
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val departuresJson = intent.getStringExtra(EXTRA_DEPARTURES)
            if (departuresJson != null) {
                val departures = Gson().fromJson<ArrayList<Departure>>(departuresJson, departuresJsonType)
                Log.d(TAG, "WidgetProvider received ${departures.count()} departures for widget (id=$widgetId)")
                AppWidgetManager.getInstance(context).apply {
                    updateAppWidget(widgetId, RemoteViews(context!!.packageName, R.layout.widget).apply {
                        setRemoteAdapter(R.id.widget_content_target, Intent(context, TimetableRemoteViewsService::class.java)
                            .apply {
                                putExtra(EXTRA_DEPARTURES, departuresJson)
                            })
                    })
                    notifyAppWidgetViewDataChanged(widgetId, R.id.widget_content_target)
                }
            }
        }
        super.onReceive(context, intent)
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
            appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(context.packageName, R.layout.widget).apply {
                setOnClickPendingIntent(R.id.widgetContainer, Intent(context, ConfigurationActivity::class.java)
                    .let { intent ->
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                        intent.action = "configure_widget-$appWidgetId"
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    })
            })
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
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