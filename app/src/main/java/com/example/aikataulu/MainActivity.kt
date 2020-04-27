package com.example.aikataulu

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.aikataulu.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
    companion object {
        const val NOTIFICATION_CHANNEL_NAME = "TimetableNC"
        const val NOTIFICATION_CHANNEL_ID = "k4k3ohonbino3a34o"
        const val NOTIFICATION_CHANNEL_DESC = "Timetable Notification Channel"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        // Create the NotificationChannel
        val descriptionText = NOTIFICATION_CHANNEL_DESC
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

    }
}
