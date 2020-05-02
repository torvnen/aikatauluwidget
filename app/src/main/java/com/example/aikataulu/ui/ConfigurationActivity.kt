package com.example.aikataulu.ui.main

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
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
            val wId = widgetId!! // Make it throw right away if null/unset. Should never occur.
            // Update global object
            TimetableConfiguration.data[wId] = config
            // Save to disk
            TimetableConfiguration.saveToFile(applicationContext)
            // Notify Service by invoking its Start method
            val serviceIntent = Intent(applicationContext, TimetableService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wId)
            applicationContext.startForegroundService(serviceIntent)

            // Set result of activity
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wId)
            }
            setResult(Activity.RESULT_OK, resultValue)

            // Notify Widgets of update
            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, WidgetProvider::class.java)
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wId)
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(wId))
            sendBroadcast(updateIntent)

            // Close main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("Exit", true)
            startActivity(intent)
            // Finish activity
            finish()
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuration_activity)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        Log.d(TAG, "Creating configuration view for widget (id=$widgetId)")
        config = TimetableConfiguration.loadConfigForWidget(applicationContext, widgetId!!)
        attachEventHandlers()
        loadUiState()
    }

}
