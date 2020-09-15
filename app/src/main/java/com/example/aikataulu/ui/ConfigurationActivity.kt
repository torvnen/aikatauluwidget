package com.example.aikataulu.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aikataulu.*
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.models.TimetableConfiguration
import com.example.aikataulu.providers.TimetableDataProvider

class ConfigurationActivity : AppCompatActivity() {
    private lateinit var listRenderer: ConfigurationListRenderer
    private var widgetId: Int? = null

    /**
     * [Invoked by framework]
     * Get required data, set observer, render initial view and attach Save Button event handler.
     * The Content Observer should trigger a re-render of the view if the Configuration is changed.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Required.

        // WidgetId should never be unset here. If it is, just throw immediately.
        widgetId = (intent!!.extras!!.get(AppWidgetManager.EXTRA_APPWIDGET_ID) as Int)
        Log.d(TAG, "Creating configuration view for widget (id=$widgetId)")

        setContentView(R.layout.configuration_activity) // Set content layout

        // Find config...
        val existingConfig = ConfigurationProvider.getExistingConfigurationOrNull(
            widgetId!!,
            applicationContext
        )
        // ...or create a new one.
        if (existingConfig == null) {
            val newConfig = TimetableConfiguration() // Default config
            newConfig.widgetId = widgetId
            // Save to database
            applicationContext.contentResolver.insert(
                ConfigurationProvider.CONFIGURATION_URI, configToContentValues(
                    newConfig
                )
            )
        }

        // Creating the list renderer will also register an observer that will trigger (re)rendering
        listRenderer = ConfigurationListRenderer(this, widgetId!!)

        // Attach Save Button functionality
        findViewById<Button>(R.id.saveButton).let {
            it.text = "Save and exit"
            it.setOnClickListener {
                val widgetId =
                    widgetId!! // Make it throw right away if null/unset. Should never occur.

                /**
                 * IMPORTANT!
                 * Set this widget as enabled.
                 * Also notify of change.
                 */
                ConfigurationProvider.enableWidget(widgetId, applicationContext)
                contentResolver.notifyChange(ConfigurationProvider.CONFIGURATION_URI, listRenderer)

                // Notify Widget Provider of config changes
                // Send empty bundle , the config will be read via other methods
                AppWidgetManager.getInstance(applicationContext)
                    .updateAppWidgetOptions(widgetId, Bundle())

                // Close main activity
                startActivity(Intent(this, MainActivity::class.java)
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("Exit", true)
                    })

                // Set result of this activity and finish
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                )
                finish()
            }
        }
    }

    /** Cleanup: unregister content observer */
    override fun onDestroy() {
        applicationContext.contentResolver.unregisterContentObserver(listRenderer)
        super.onDestroy() // Required.
    }

    companion object {
        private const val TAG = "TIMETABLE.ConfigurationActivity"

        fun configToContentValues(c: TimetableConfiguration): ContentValues {
            val e = ConfigurationContract.ConfigurationEntry
            return ContentValues().apply {
                put(e.COLUMN_NAME_WIDGET_ID, c.widgetId)
                put(e.COLUMN_NAME_UPDATE_INTERVAL_SECONDS, c.updateIntervalS)
                put(e.COLUMN_NAME_AUTO_UPDATE_ENABLED, c.autoUpdate)
                put(e.COLUMN_NAME_SELECTED_STOP_ID, c.stopId)
                put(e.COLUMN_NAME_WIDGET_ENABLED, c.isWidgetEnabled)
            }
        }
    }
}
