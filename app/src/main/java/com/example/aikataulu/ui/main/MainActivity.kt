package com.example.aikataulu.ui.main

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.example.aikataulu.R
import com.example.aikataulu.TimetableService
import com.example.aikataulu.WidgetConfiguration

class MainActivity : AppCompatActivity() {
    companion object {
        private const val NOTIFICATION_CHANNEL_NAME = "timetable_nc"
        private const val NOTIFICATION_CHANNEL_ID = "35151513122"
        private const val NOTIFICATION_CHANNEL_DESC = "Timetable Notification Channel"
        private const val TAG = "TIMETABLE.MainActivity"
        var notificationChannelInitiated = false
        var appWidgetId: Int? = null

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

    private fun attachEventHandlers() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        val stopName = findViewById<EditText>(R.id.stopName)
        val autoUpdate = findViewById<Switch>(R.id.autoUpdate)
        stopName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                WidgetConfiguration.stopName = s.toString()
            }
        })
        autoUpdate.setOnCheckedChangeListener { _, isChecked ->
            WidgetConfiguration.autoUpdate = isChecked
        }
        saveButton.setOnClickListener {
            // Notify Service
            val serviceIntent = Intent(applicationContext, TimetableService::class.java).apply {
                this.action = TimetableService.ACTION_SETTINGS_CHANGED
            }
            startService(serviceIntent)

            val widgetId = appWidgetId
            if (widgetId != null) {
                // Update app widget
                RemoteViews(this.packageName,
                    R.layout.widget
                ).also { views->
                    AppWidgetManager.getInstance(this).updateAppWidget(widgetId.toInt(), views)
                }
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        widgetId
                    )
                }
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            } else Log.d("TIMETABLE", "Cannot finish activity: widgetId is not set.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (!notificationChannelInitiated)
            notificationChannelId(
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            )

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        attachEventHandlers()
    }

}
