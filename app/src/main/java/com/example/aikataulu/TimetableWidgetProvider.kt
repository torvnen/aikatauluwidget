package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

        // Send data to be handled by ViewFactory
        fun sendUpdateWidgetBroadcast(context: Context, widgetId: Int) {
            context.sendBroadcast(Intent(context, TimetableWidgetProvider::class.java)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        }

        fun getExistingWidgetIds(context: Context): IntArray {
            return AppWidgetManager
                .getInstance(context)
                .getAppWidgetIds(
                    ComponentName(context.packageName, TimetableWidgetProvider::class.qualifiedName!!))
        }
    }

    override fun onEnabled(context: Context?) {
        Log.d(TAG, "onEnabled()")
        // Start service when a widget has been added
        context!!.startForegroundService(Intent(context, TimetableService::class.java))
        // Perform this loop procedure for each App Widget that belongs to this provider
        getExistingWidgetIds(context).forEach { widgetId ->
            Log.i(TAG, "Attaching click handler to widget id $widgetId")
            AppWidgetManager.getInstance(context)
                .updateAppWidget(widgetId, RemoteViews(context.packageName, R.layout.widget)
                    .apply {
//                        setOnClickPendingIntent(R.id.btn_configureWidget,
//                            Intent(context, ConfigurationActivity::class.java)
//                                .let { intent ->
//                                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
//                                    intent.action = "configure_widget-$widgetId"
//                                    PendingIntent.getActivity(
//                                        context,
//                                        0,
//                                        intent,
//                                        PendingIntent.FLAG_UPDATE_CURRENT
//                                    )
//                                })
                    })
        }
        super.onEnabled(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        // Invoke start method of Service - it will handle all required actions to update widgets.
        context!!.startForegroundService(Intent(context, TimetableService::class.java))
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    // Update widget based on new data
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (widgetId != -1) {
                Log.i(TAG, "Updating app widget (id=$widgetId)")
                AppWidgetManager.getInstance(context).apply {
                    updateAppWidget(
                        widgetId,
                        RemoteViews(context!!.packageName, R.layout.widget).apply {
                            // TODO Optimization: check if remote adapter could be set in onEnabled()
                            setRemoteAdapter(
                                R.id.widget_content_target,
                                Intent(context, TimetableRemoteViewsService::class.java)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                            )
                        })
                    notifyAppWidgetViewDataChanged(widgetId, R.id.widget_content_target)
                }
            }
            else Log.w(TAG, "Widget ID not defined when ACTION_APPWIDGET_UPDATE was invoked. ComponentName: ${intent.component?.className}")
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
        // Only stop the service if this is the only added widget
        if (context != null && getExistingWidgetIds(context).size <= 1) {
            Log.i(TAG, "Stopping service...")
            context.stopService(Intent(context, TimetableService::class.java))
        } else Log.w(TAG, "Context is null when widget provider is deleted. Cannot stop service.")
        super.onDeleted(context, appWidgetIds)
    }
}