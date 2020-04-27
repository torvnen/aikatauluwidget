package com.example.aikataulu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.aikataulu.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "TimetableNC"
        const val NOTIFICATION_CHANNEL_ID = "35151513123"
        const val NOTIFICATION_CHANNEL_DESC = "Timetable Notification Channel"
        var notificationChannelInitiated = false

        fun initiateNotificationChannel(notificationManager: NotificationManager) {
            Log.i("TIMETABLE", "Initiating notification channel...")
            // Create the NotificationChannel
            val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
            mChannel.description = NOTIFICATION_CHANNEL_DESC
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(mChannel)
            Log.i("TIMETABLE", "Notification channel initiated.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        if (!notificationChannelInitiated) initiateNotificationChannel(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
    }
}
