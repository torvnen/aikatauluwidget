package com.example.aikataulu.ui.main

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import com.example.aikataulu.*

class ConfigurationActivity : AppCompatActivity() {
    private val TAG = "TIMETABLE.ConfigurationActivity"
    var widgetId: Int? = null
    lateinit var config: TimetableConfigurationData

    private fun attachEventHandlers() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        val stopName = findViewById<EditText>(R.id.stopName)
        val autoUpdate = findViewById<Switch>(R.id.autoUpdate)
        val updateInterval = findViewById<EditText>(R.id.updateInterval)
        updateInterval.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If string is not int-parseable, set default config value
                config.updateIntervalS = s.toString().toIntOrNull()
                    ?: TimetableConfigurationData().updateIntervalS
            }
        })
        stopName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                config.stopName = s.toString()
            }
        })
        autoUpdate.setOnCheckedChangeListener { _, isChecked ->
            config.autoUpdate = isChecked
        }
        saveButton.setOnClickListener {
            // Update global object
            TimetableConfiguration.data[widgetId!!] = config
            // Save to disk
            TimetableConfiguration.saveToFile(applicationContext)
            // Notify Service by invoking its Start method
            val serviceIntent = Intent(applicationContext, TimetableService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId!!)
            applicationContext.startForegroundService(serviceIntent)

            val widgetId = widgetId
            if (widgetId != null) {
                // Update app widget
                // TODO check if this works without specifying EXTRA_APPWIDGET_ID to AppWidgetManager
                RemoteViews(this.packageName, R.layout.widget).also { views->
                    AppWidgetManager.getInstance(this).updateAppWidget(widgetId.toInt(), views)
                }
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                }
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            } else Log.d("TIMETABLE", "Cannot finish activity: widgetId is not set.")
        }
    }

    private fun loadUiState() {
        val stopName = findViewById<EditText>(R.id.stopName)
        val autoUpdate = findViewById<Switch>(R.id.autoUpdate)
        val updateInterval = findViewById<EditText>(R.id.updateInterval)

        autoUpdate.isChecked = config.autoUpdate
        stopName.setText(config.stopName)
        updateInterval.setText(config.updateIntervalS.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuration_activity)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        config = TimetableConfiguration.loadConfigForWidget(applicationContext, widgetId!!)
        attachEventHandlers()
        loadUiState()
    }

}
