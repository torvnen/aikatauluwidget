package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.database.Cursor
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
import com.example.aikataulu.providers.TimetableDataProvider
import com.example.aikataulu.ui.MainActivity
import com.google.gson.Gson
import java.util.*
import kotlin.collections.HashMap


class TimetableTask(private val context: Context, private val config: TimetableConfiguration) :
    TimerTask() {
    private var stop: Stop

    companion object {
        private const val TAG = "TIMETABLE.TimetableTask"
    }

    init {
        val cursor = context.contentResolver.query(
            TimetableDataProvider.STOP_URI,
            null,
            "${StopContract.StopEntry.COLUMN_NAME_HRTID} = ?",
            arrayOf(config.stopId),
            null,
            null
        )
        cursor!!.moveToFirst()
        stop = Stop(
            cursor.getString(cursor.getColumnIndex(StopContract.StopEntry.COLUMN_NAME_STOPNAME)),
            cursor.getString(cursor.getColumnIndex(StopContract.StopEntry.COLUMN_NAME_HRTID))
        )
    }

    override fun run() {
        val widgetId = config.widgetId!!
        val departures =
            Api.getDeparturesForStopId(config.stopId!!).map { Departure(it) }
        Log.d(
            TAG,
            "[WidgetId=${config.widgetId}]: Received ${departures.count()} departures for stop ${config.stopId})"
        )

        // Update content
        context.contentResolver.update(
            TimetableDataProvider.TIMETABLE_DATA_URI,
            ContentValues().apply {
                put(
                    TimetableDataProvider.COLUMN_TIMETABLE,
                    Gson().toJson(Timetable(widgetId, stop, departures))
                )
            },
            stop.hrtId,
            null
        )
        context.contentResolver.notifyChange(TimetableDataProvider.TIMETABLE_DATA_URI, null)
        // Notify WidgetProvider of the changes
        TimetableWidgetProvider.sendUpdateWidgetBroadcast(
            context,
            config.widgetId!!
        )
    }
}

class TimetableTaskRunner(
    private val context: Context
) : ContentObserver(Handler()) {
    private val _tasks = HashMap<Int, Pair<Timer, TimetableTask>>()
    private var cursor: Cursor? = null

    companion object {
        private const val TAG = "TIMETABLE.ServiceConfigurationObserver"
    }

    fun cancelAllTasks() {
        _tasks.all {
            it.value.first.cancel()
            it.value.second.cancel()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Log.d(TAG, "[selfChange=$selfChange] ConfigurationObserver triggered by uri ${uri?.toString()}")
        val entry = ConfigurationContract.ConfigurationEntry

        // Find and return configuration for all widgets
        cursor = context.contentResolver.query(
            TimetableDataProvider.CONFIGURATION_URI,
            null,
            null,
            null,
            null
        )

        // Cancel and clear all tasks and their timers
        cancelAllTasks()
        _tasks.clear()

        while (cursor?.moveToNext() == true) {
            val config = ConfigurationContract.ConfigurationEntry.cursorToPoco(cursor)
            if (config != null) {
                val widgetId = config.widgetId
                if (config.autoUpdate && widgetId != null && widgetId > 0) {
                    Log.d(TAG, "[WidgetId=$widgetId] Starting task with interval [${config.updateIntervalS} seconds]")

                    _tasks[widgetId] = Pair(Timer(), TimetableTask(context, config))
                    _tasks[widgetId]!!.first.scheduleAtFixedRate(
                        _tasks[widgetId]!!.second,
                        0,
                        (config.updateIntervalS * 1000).toLong()
                    )
                } else {
                    Log.i(
                        TAG,
                        "[WidgetId=$widgetId]: AutoUpdate=${config.autoUpdate}. Not starting task."
                    )
                }
            } else Log.w(TAG, "Null configuration")
        }
    }
}

class TimetableService : Service() {
    private lateinit var _observer: TimetableTaskRunner

    companion object {
        private const val TAG = "TIMETABLE.Service"
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
        Log.d(TAG, "Registering configuration observer")
        _observer = TimetableTaskRunner(applicationContext)
        applicationContext.contentResolver.registerContentObserver(
            TimetableDataProvider.CONFIGURATION_URI,
            false,
            _observer
        )
        // Notify of change so that the initial tasks are started
        applicationContext.contentResolver.notifyChange(
            TimetableDataProvider.CONFIGURATION_URI,
            null
        )
    }

    override fun onDestroy() {
        // Cancel all tasks and unregister observer to avoid memory leaks
        _observer.cancelAllTasks()
        applicationContext.contentResolver.unregisterContentObserver(_observer)
    }
}