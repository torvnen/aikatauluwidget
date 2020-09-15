package com.example.aikataulu.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.aikataulu.R

/**
 * Totally useless. Serves no purpose in this application, but is required.
 */
class MainActivity : AppCompatActivity() {
    /**
     * [Invoked by framework]
     * Set the content view and allow the activity to be closed remotely.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        // Set the default view when the app is opened from the launcher
        // TODO have the main activity content view fetch all existing widgets and provide buttons for configuring them?
        setContentView(R.layout.main_activity)

        if (intent.getBooleanExtra(MAIN_ACTIVITY_EXTRA_EXIT, false)) {
            Log.d(TAG, "Exit flag was set - finishing activity.")
            finish()
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_NAME = "timetable_nc"
        private const val NOTIFICATION_CHANNEL_ID = "35151513122"
        private const val NOTIFICATION_CHANNEL_DESC = "Timetable Notification Channel"
        private const val TAG = "TIMETABLE.MainActivity"
        private var notificationChannelInitiated = false
        const val MAIN_ACTIVITY_EXTRA_EXIT = "exit"

        fun notificationChannelId(notificationManager: NotificationManager): String {
            if (!notificationChannelInitiated) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // API Level 26+ requires a registered notification channel.
                    Log.i("TIMETABLE", "Initiating notification channel...")
                    // Create the NotificationChannel
                    val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE
                    )
                    channel.description = NOTIFICATION_CHANNEL_DESC
                    channel.lightColor = Color.MAGENTA
                    channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    notificationManager.createNotificationChannel(channel)
                    notificationChannelInitiated = true
                    Log.i("TIMETABLE", "Notification channel initiated.")
                } else Log.i(
                    "TIMETABLE",
                    "API Version is lower than 26. Not initiating notification channel."
                )
            }
            return NOTIFICATION_CHANNEL_ID
        }
    }
}
