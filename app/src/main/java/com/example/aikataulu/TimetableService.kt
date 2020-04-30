package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aikataulu.api.Api
import com.example.aikataulu.models.formatArrivals
import com.example.aikataulu.ui.main.MainActivity
import java.util.*

class TimetableService : Service() {
    private lateinit var _timerTask: TimerTask
    private lateinit var _timer: Timer

    companion object {
        private const val TAG = "TIMETABLE.Service"
        const val ACTION_SETTINGS_CHANGED = "ACTION_SETTINGS_CHANGED"
        const val creationNotificationId = 12559999
        const val destructionNotificationId = 12559998
    }

    private fun ensureTimedTaskCanceled() {
        if (this::_timerTask.isInitialized)
            _timerTask.cancel()
    }

    private fun setAutoUpdate(b: Boolean) {
        ensureTimedTaskCanceled()
        val config = TimetableConfiguration.ensureLoaded(applicationContext)
        val stopName = config.stopName
        if (b && stopName != null) {
            val stops = Api.getStopsContainingText(stopName)
            if (stops.any()) {
                val stop = stops.first()
                _timerTask = object: TimerTask() {
                    override fun run() {
                        Log.i(TAG, "Fetching data for stop ${stop.name} (${stop.hrtId})...")
                        val arrivals = Api.getArrivalsForStop(stop.hrtId)
                        Log.i(TAG, "Received ${arrivals.count()} arrivals")
                        setWidgetText(formatArrivals(arrivals))
                    }
                }
                _timer = Timer()
                _timer.scheduleAtFixedRate(_timerTask, 0, (1000 * config.updateIntervalS).toLong())
            }
        }
    }

    fun setWidgetText(text: CharSequence) {
        val remoteViews = RemoteViews(this.applicationContext.packageName, R.layout.widget)
        val appWidgetManager = AppWidgetManager.getInstance(this.applicationContext)
        remoteViews.setTextViewText(R.id.widgetTextView, text)
        appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, WidgetProvider::class.java))
            .forEach { widgetId -> appWidgetManager.updateAppWidget(widgetId, remoteViews) }
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        startForeground(creationNotificationId, builder.build())
        Log.i(TAG, "Service was created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received onStartCommand with intent name ${intent?.action}")
        // On any start command, start/stop/restart auto-updating.
        ensureTimedTaskCanceled()
        val config = TimetableConfiguration.ensureLoaded(applicationContext)
        setAutoUpdate(config.autoUpdate)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        val builder = NotificationCompat
            .Builder(this, MainActivity.notificationChannelId(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager))
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("Timetable")
            .setContentText("Service was stopped.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(destructionNotificationId, builder.build())
        }
        return super.stopService(name)
    }

    override fun onDestroy() {
        if (this::_timerTask.isInitialized) _timerTask.cancel()
        Log.i(TAG, "onDestroy()")
        super.onDestroy()
    }
}