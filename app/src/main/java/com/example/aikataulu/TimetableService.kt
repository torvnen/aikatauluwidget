package com.example.aikataulu

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TimetableService : Service() {
    private val binder = TimetableServiceBinder()

    companion object NotificationIds {
        const val creation = 12559999
        const val destruction = 12559998
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("TIMETABLE", "Service.onBind()")
        return binder
    }

    override fun onCreate() {
        Log.i("TIMETABLE", "Service.onCreate()")
        super.onCreate()
        val builder = NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.icon)
            .setContentText("Timetable service started")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(NotificationIds.creation, builder.build())
        }
    }

    override fun onDestroy() {
        Log.i("TIMETABLE", "Service.onDestroy()")
        val builder = NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.icon)
            .setContentText("Timetable service stopped")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(NotificationIds.destruction, builder.build())
        }
        super.onDestroy()
    }
}