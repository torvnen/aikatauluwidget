package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.providers.TimetableDataProvider
import com.example.aikataulu.ui.ConfigurationActivity

/**
 * Handles widget-related events.
 * The single instance of this class is shared by all widgets.
 * Defined in [R.xml.app_widget_provider].
 */
class TimetableWidgetProvider : AppWidgetProvider() {
    /** Start the service when any widget is enabled (added to home screen) */
    override fun onEnabled(context: Context?) {
        Log.d(TAG, "onEnabled()")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(Intent(context, TimetableService::class.java))
        } else throw Exception("TIMETABLE: SDK Level too low. Cannot start foreground service.")
    }

    /**
     * Set the widget content to match the updated configuration.
     * This includes the click handler that will reopen the [ConfigurationActivity].
     */
    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        Log.i(TAG, "[WidgetId=$appWidgetId]: Notifying data as changed.")
        val widgetConfig =
            ConfigurationProvider.getExistingConfigurationOrNull(appWidgetId, context!!)
        val stopName = if (widgetConfig == null) "" else StopProvider.getStopByIdOrNull(
            widgetConfig.stopId!!,
            context
        )!!.name
        AppWidgetManager.getInstance(context).apply {
            updateAppWidget(
                appWidgetId,
                RemoteViews(context!!.packageName, R.layout.widget).apply {
                    /** Set the [android.widget.TextView] title text */
                    setTextViewText(R.id.widgetTitle, "Departures for stop $stopName")

                    /** Set [TimetableRemoteViewsService] as the Remote Adapter for this widget */
                    setRemoteAdapter(
                        R.id.widget_content_target,
                        Intent(context!!, TimetableRemoteViewsService::class.java).apply {
                            type = "set-remote-adapter-$appWidgetId"
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                    )

                    /** Open [ConfigurationActivity] when the widget is clicked */
                    setOnClickPendingIntent(R.id.widgetContainer,
                        Intent(context, ConfigurationActivity::class.java)
                            .let { intent ->
                                intent.putExtra(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                    appWidgetId
                                )
                                intent.action = "configure_widget-$appWidgetId"
                                intent.type = "configure_widget-$appWidgetId"
                                PendingIntent.getActivity(
                                    context,
                                    appWidgetId,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                            })
                })
        }
    }

    /**
     * Perform cleanup.
     * - Remove configuration object from the database
     * - If no more widgets are active after removing this, stop the service.
     */
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        // Remove this config from DB
        appWidgetIds?.forEach {
            context?.contentResolver?.delete(
                TimetableDataProvider.CONFIGURATION_URI,
                "${ConfigurationContract.ConfigurationEntry.COLUMN_NAME_WIDGET_ID} = ?",
                arrayOf(it.toString())
            )
        }
        // Only stop the service if this is the only added widget
        if (context != null && getExistingWidgetIds(context.applicationContext).size <= 1) {
            Log.i(TAG, "Stopping service...")
            context.stopService(Intent(context, TimetableService::class.java))
        } else Log.w(TAG, "Context is null when widget provider is deleted. Cannot stop service.")
    }

    companion object {
        private const val TAG = "TIMETABLE.WidgetProvider"

        /** Finds the WidgetIds of all enabled widgets */
        fun getExistingWidgetIds(context: Context): IntArray {
            return AppWidgetManager
                .getInstance(context)
                .getAppWidgetIds(
                    ComponentName(
                        context.packageName,
                        TimetableWidgetProvider::class.qualifiedName!!
                    )
                )
        }
    }
}