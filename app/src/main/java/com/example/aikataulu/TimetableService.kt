package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.aikataulu.providers.TimetableDataProvider
import com.example.aikataulu.ui.MainActivity


class TimetableService : Service() {
    private lateinit var _observer: TimetableTaskRunner

    /**
     * [Invoked by application]
     * Start a foreground service.
     * Android framework requires a notification to be present for any foreground service.
     * The notification cannot be worked around as of Android 7.3.
     * The preferred way to have no notification is to advice the end-user to block it.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TIMETABLE", "Starting foreground service...")

        val notification = NotificationCompat
            .Builder(
                this,
                MainActivity.notificationChannelId(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            )
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("Timetable")
            .setContentText("Service was created.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(944, notification)

        Log.d("TIMETABLE", "Foreground service started.")

        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * [Invoked by framework]
     * Register a content observer, which will manage the service's tasks.
     */
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

    /** Perform cleanup: cancel all tasks and unregister content observer. */
    override fun onDestroy() {
        _observer.cancelAllTasks()
        applicationContext.contentResolver.unregisterContentObserver(_observer)
    }

    /** Required by framework. Non-functional in this context. */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "TIMETABLE.Service"
    }
}