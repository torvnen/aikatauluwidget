package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aikataulu.api.Api
import com.example.aikataulu.models.Departure
import com.example.aikataulu.models.formatDepartures
import com.example.aikataulu.ui.MainActivity
import com.google.gson.Gson
import java.util.*
import kotlin.collections.HashMap

class TimetableService : Service() {
    private val _timerTasks = HashMap<Int, TimerTask>()
    private val _timers = HashMap<Int, Timer>()

    companion object {
        private const val TAG = "TIMETABLE.Service"
        const val ACTION_SETTINGS_CHANGED = "ACTION_SETTINGS_CHANGED"
        const val creationNotificationId = 12559999
        const val destructionNotificationId = 12559998
    }

    private fun ensureTimedTaskCanceled(widgetId: Int) {
        _timerTasks.filter { it.key == widgetId }.forEach { it.value.cancel() }
    }

    private fun setAutoUpdate(widgetId: Int, b: Boolean) {
        ensureTimedTaskCanceled(widgetId)
        val config = TimetableConfiguration.loadConfigForWidget(applicationContext, widgetId)
        val stopName = config.stopName
        if (b && stopName != null) {
            val stops = Api.getStopsContainingText(stopName)
            if (stops.any()) {
                val stop = stops.first()
                _timerTasks[widgetId] = object: TimerTask() {
                    override fun run() {
                        Log.i(TAG, "Fetching data for stop ${stop.name} (${stop.hrtId})...")
                        val departures = Api.getDeparturesForStopId(stop.hrtId)
                        Log.i(TAG, "Received ${departures.count()} departures")
                        val departuresJson = departures.map { Departure(it) }.let { Gson().toJson(it) }
                        setWidgetText(widgetId, formatDepartures(departures))
//                        startActivity(Intent() // TODO TODO TODO USE APPWIDGETMANAGER.UPDATEWIDGET FOR THIS
//                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            .setAction(WidgetProvider.ACTION_RECEIVE_DEPARTURES)
//                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
//                            .putExtra(WidgetProvider.EXTRA_DEPARTURES, departuresJson))
                    }
                }
                _timers[widgetId] = Timer()
                _timers[widgetId]!!.scheduleAtFixedRate(_timerTasks[widgetId], 0, (1000 * config.updateIntervalS).toLong())
            }
        }
    }

    fun setWidgetText(widgetId: Int, text: CharSequence) {
        val remoteViews = RemoteViews(this.applicationContext.packageName, R.layout.widget)
        val appWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        remoteViews.setTextViewText(R.id.widgetTextView, text)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.i(TAG, "Creating service...")
        super.onCreate()

        val builder = NotificationCompat
            .Builder(this, MainActivity.notificationChannelId(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager))
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("Timetable")
            .setContentText("Service was created.")
            .setPriority(NotificationCompat.PRIORITY_MIN)

        startForeground(creationNotificationId, builder.build())
        Log.i(TAG, "Service was created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received onStartCommand with intent name ${intent?.action}")
        val widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        ensureTimedTaskCanceled(widgetId)
        val config = TimetableConfiguration.loadConfigForWidget(applicationContext, widgetId)
        setAutoUpdate(widgetId, config.autoUpdate)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        val builder = NotificationCompat
            .Builder(this, MainActivity.notificationChannelId(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager))
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("Timetable")
            .setContentText("Service was stopped.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
        with(NotificationManagerCompat.from(this)) {
            notify(destructionNotificationId, builder.build())
        }
        return super.stopService(name)
    }

    override fun onDestroy() {
        _timerTasks.forEach { it.value.cancel() }
        Log.i(TAG, "onDestroy()")
        super.onDestroy()
    }
}