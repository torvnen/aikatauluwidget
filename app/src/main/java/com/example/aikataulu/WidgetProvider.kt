package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import com.example.aikataulu.ui.main.ConfigurationActivity
import com.example.aikataulu.ui.main.MainActivity


class WidgetProvider : AppWidgetProvider() {
    val TAG = "TIMETABLE.WidgetProvider"

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
                    setTextViewText(R.id.widgetTextView2, "#$appWidgetId")
                }

            appWidgetManager.updateAppWidget(appWidgetId, views)
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