package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.aikataulu.api.Api
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.database.contracts.StopContract
import com.example.aikataulu.models.Departure
import com.example.aikataulu.models.Stop
import com.example.aikataulu.models.Timetable
import com.example.aikataulu.ui.MainActivity
import com.google.gson.Gson
import java.util.*
import kotlin.collections.HashMap

class TimetableService : Service() {
    private val _tasks = HashMap<Int, TimerTask>()
    private val _timers = HashMap<Int, Timer>()
    private val _observer = ConfigurationObserver(applicationContext) { config ->
        if (config != null) {
            ensureTimedTaskCanceled(config.widgetId ?: 0)
            updateTask(config)
        }
    }

    companion object {
        private const val TAG = "TIMETABLE.Service"
    }

    private class ConfigurationObserver(
        val context: Context,
        val callback: (TimetableConfiguration?) -> Unit
    ) : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            // Find and return configuration for this widget
            val cursor = context.contentResolver.query(
                TimetableDataProvider.CONFIGURATION_URI,
                null,
                null,
                null,
                null
            )
            while (cursor != null && cursor.moveToNext()) {
                callback(ConfigurationContract.ConfigurationEntry.cursorToPoco(cursor))
            }
        }
    }

    private fun ensureTimedTaskCanceled(widgetId: Int) {
        _tasks.filter { it.key == widgetId }.forEach { it.value.cancel() }
    }


    private fun updateTask(config: TimetableConfiguration?) {
        if (config == null) {
            return
        }
        val c = config!!
        val widgetId = c.widgetId!!
        ensureTimedTaskCanceled(c.widgetId ?: 0)
        Log.i(
            TAG,
            "${if (c.autoUpdate) "Starting" else "Stopping"} auto-updating widgetId=$widgetId stopId=${c.stopId}"
        )
        if (c.autoUpdate) {
            val cursor = applicationContext.contentResolver.query(
                TimetableDataProvider.STOP_URI,
                null,
                "${StopContract.StopEntry.COLUMN_NAME_HRTID} = ?",
                arrayOf(config.stopId),
                null,
                null
            )
            cursor!!.moveToFirst()
            val stop = Stop(
                cursor.getString(cursor.getColumnIndex(StopContract.StopEntry.COLUMN_NAME_STOPNAME)),
                cursor.getString(cursor.getColumnIndex(StopContract.StopEntry.COLUMN_NAME_HRTID))
            )
            Log.i(TAG, "Chose the stop ${stop.hrtId} (${stop.name})")
            _tasks[widgetId] = object : TimerTask() {
                override fun run() {
                    val departures =
                        Api.getDeparturesForStopId(stop.hrtId).map { Departure(it) }
                    Log.d(
                        TAG,
                        "[WidgetId=$widgetId]: Received ${departures.count()} departures for stop ${stop.name} (${stop.hrtId})"
                    )

                    // Update content
                    applicationContext.contentResolver.update(
                        TimetableDataProvider.TIMETABLE_DATA_URI,
                        ContentValues().apply {
                            put(
                                TimetableDataProvider.COLUMN_TIMETABLE,
                                Gson().toJson(Timetable(widgetId, stop, departures))
                            )
                        },
                        stop.hrtId,
                        emptyArray<String>()
                    )
                    // Notify WidgetProvider of the changes
                    TimetableWidgetProvider.sendUpdateWidgetBroadcast(
                        applicationContext,
                        widgetId
                    )
                }
            }
            _timers[widgetId] = Timer()
            _timers[widgetId]!!.scheduleAtFixedRate(
                _tasks[widgetId],
                0,
                (1000 * config.updateIntervalS).toLong()
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TIMETABLE", "Starting foreground service...")
        val builder = NotificationCompat
            .Builder(
                this,
                MainActivity.notificationChannelId(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            )
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("Timetable")
            .setContentText("Service was created.")
            .setPriority(NotificationCompat.PRIORITY_MIN)

        startForeground(944, builder.build())
        Log.d("TIMETABLE", "Foreground service started.")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        applicationContext.contentResolver.registerContentObserver(
            TimetableDataProvider.CONFIGURATION_URI,
            true,
            _observer
        )
        // Notify of change so that the initial tasks are started
        applicationContext.contentResolver.notifyChange(
            TimetableDataProvider.CONFIGURATION_URI,
            null
        )
        super.onCreate()
    }

    override fun onDestroy() {
        // Cancel all tasks and unregister observer to avoid memory leaks
        _tasks.forEach { it.value.cancel() }
        applicationContext.contentResolver.unregisterContentObserver(_observer)
        super.onDestroy()
    }
}