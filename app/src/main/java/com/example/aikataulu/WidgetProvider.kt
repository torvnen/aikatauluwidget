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
import com.example.aikataulu.ui.main.ConfigurationActivity
import com.example.aikataulu.ui.main.MainActivity


class WidgetProvider : AppWidgetProvider() {
    companion object {
        const val TAG = "TIMETABLE.WidgetProvider"
        fun attachWidgetClickHandler(context: Context, widgetId: Int) {
            Log.i(TAG, "Setting onClick event for app widget (id=$widgetId)")
            val intent = Intent(context, ConfigurationActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val views = RemoteViews(context.packageName, R.layout.widget)
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Start Service (don't worry about repeating, multiple calls will be handled by the system)
        Log.i(TAG, "onUpdate()")

        // Attach click handler for all widgets
        appWidgetIds.forEach {
            attachWidgetClickHandler(context, it) }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(ComponentName(context!!.applicationContext, WidgetProvider::class.java))
            .forEach {
                attachWidgetClickHandler(context, it)
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