package com.example.aikataulu.ui

import android.app.*
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.aikataulu.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val NOTIFICATION_CHANNEL_NAME = "timetable_nc"
        private const val NOTIFICATION_CHANNEL_ID = "35151513122"
        private const val NOTIFICATION_CHANNEL_DESC = "Timetable Notification Channel"
        private const val TAG = "TIMETABLE.MainActivity"
        var notificationChannelInitiated = false

        fun notificationChannelId(notificationManager: NotificationManager): String {
            if (!notificationChannelInitiated) {
                Log.i("TIMETABLE", "Initiating notification channel...")
                // Create the NotificationChannel
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
                channel.description = NOTIFICATION_CHANNEL_DESC
                channel.lightColor = Color.MAGENTA
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(channel)
                notificationChannelInitiated =  true
                Log.i("TIMETABLE", "Notification channel initiated.")
            }
            return NOTIFICATION_CHANNEL_ID
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        if( intent.getBooleanExtra("Exit", false)){
            Log.d(TAG, "Exit flag was set - finishing activity.")
            finish()
        }
        setContentView(R.layout.main_activity)
        if (!notificationChannelInitiated)
            notificationChannelId(
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            )
    }

}
