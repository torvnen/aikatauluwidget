package com.example.aikataulu.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
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
            // Notify Widget Provider of config changes
            // Send empty bundle (for now), the config will be read via other methods
            AppWidgetManager.getInstance(applicationContext).updateAppWidgetOptions(wId, Bundle())

            // Close main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("Exit", true)
            startActivity(intent)
            // Set result of activity and finish
            setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wId))
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
