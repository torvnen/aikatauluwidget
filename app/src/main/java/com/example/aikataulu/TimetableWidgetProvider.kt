package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.models.Timetable
import com.example.aikataulu.providers.TimetableDataProvider
import com.example.aikataulu.ui.ConfigurationActivity

class TimetableWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val TAG = "TIMETABLE.WidgetProvider"

        // Send data to be handled by ViewFactory
        fun sendUpdateWidgetBroadcast(context: Context, widgetId: Int) {
            Log.d(TAG, "[WidgetId=$widgetId]: Broadcasting ACTION_APPWIDGET_UPDATE.")
            context.sendBroadcast(
                Intent(context, TimetableWidgetProvider::class.java)
                    .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            )
        }

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

    override fun onEnabled(context: Context?) {
        Log.d(TAG, "onEnabled()")
        // Start service when a widget has been added
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(Intent(context, TimetableService::class.java))
        } else throw Exception("TIMETABLE: SDK Level too low. Cannot start foreground service.")
        super.onEnabled(context)
    }

    // Update widget based on new data
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (widgetId != -1) {
                Log.i(TAG, "[WidgetId=$widgetId]: Notifying data as changed.")
                val widgetConfig =
                    ConfigurationProvider.getExistingConfigurationOrNull(widgetId, context!!)
                val stopName = if (widgetConfig == null) "" else StopProvider.getStopByIdOrNull(
                    widgetConfig.stopId!!,
                    context
                )!!.name
                AppWidgetManager.getInstance(context).apply {
                    // Set the RemoteViews for Widgets to have a view adapter
                    // Remember to also call updateAppWidget, the RemoteViews will not apply itself.
                    updateAppWidget(
                        widgetId,
                        RemoteViews(context!!.packageName, R.layout.widget).apply {
                            setTextViewText(R.id.widgetTitle, "Departures for stop $stopName")
                            setRemoteAdapter(
                                R.id.widget_content_target,
                                Intent(context!!, TimetableRemoteViewsService::class.java).apply {
                                    type = "set-remote-adapter-$widgetId"
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                }
                            )
                            setOnClickPendingIntent(R.id.widgetContainer,
                                Intent(context, ConfigurationActivity::class.java)
                                    .let { intent ->
                                        intent.putExtra(
                                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                                            widgetId
                                        )
                                        intent.action = "configure_widget-$widgetId"
                                        intent.type = "configure_widget-$widgetId"
                                        PendingIntent.getActivity(
                                            context,
                                            widgetId,
                                            intent,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                    })
                        })

                    notifyAppWidgetViewDataChanged(widgetId, R.id.widget_content_target)
                }
            } else Log.w(
                TAG,
                "Widget ID not defined when ACTION_APPWIDGET_UPDATE was invoked. ComponentName: ${intent.component?.className}"
            )
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.i(TAG, "onUpdate()")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

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
}