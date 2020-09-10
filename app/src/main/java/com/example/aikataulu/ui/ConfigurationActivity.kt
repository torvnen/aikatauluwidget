package com.example.aikataulu.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aikataulu.*
import com.example.aikataulu.database.contracts.ConfigurationContract

class ConfigurationActivity : AppCompatActivity() {
    private lateinit var intervalDialog: IntervalDialog
    private val TAG = "TIMETABLE.ConfigurationActivity"
    var widgetId: Int? = null
    private var viewModel: TimetableConfigurationData? = null

    class ConfigurationChangeHandler : Handler() {}

    class ConfigurationObserver(
        val context: Context,
        val widgetId: Int,
        val callback: (TimetableConfigurationData?) -> Unit
    ) : ContentObserver(ConfigurationChangeHandler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            val cursor = context.contentResolver.query(
                TimetableDataProvider.CONFIGURATION_URI,
                null,
                widgetId.toString(),
                null,
                null
            )
            // Find configuration for this widget
            if (cursor != null && cursor.moveToFirst()) {
                val entry = ConfigurationContract.ConfigurationEntry
                val updateIntervalS =
                    cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS))
                val stopId =
                    cursor.getString(cursor.getColumnIndex(entry.COLUMN_NAME_SELECTED_STOP_ID))
                val isAutoUpdateEnabled =
                    cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_AUTO_UPDATE_ENABLED)) == 1
                val config =
                    TimetableConfigurationData(updateIntervalS, stopId, isAutoUpdateEnabled)
                callback(config)
            } else callback(null)
        }
    }

    private fun attachEventHandlers() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        val stopName = findViewById<EditText>(R.id.stopName)
        val autoUpdate = findViewById<Switch>(R.id.autoUpdate)
        val updateInterval = findViewById<EditText>(R.id.updateInterval)
        updateInterval.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If string is not int-parseable, set default config value
                viewModel!!.updateIntervalS = s.toString().toIntOrNull()
                    ?: TimetableConfigurationData().updateIntervalS
            }
        })
        stopName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel!!.stopId = s.toString()
            }
        })
        autoUpdate.setOnCheckedChangeListener { _, isChecked ->
            viewModel!!.autoUpdate = isChecked
        }
        saveButton.setOnClickListener {
            val wId = widgetId!! // Make it throw right away if null/unset. Should never occur.
            // Update global object
            TimetableConfiguration.data[wId] = viewModel!!
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
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wId)
            )
            finish()
        }
    }

    private fun updateInterval(seconds: Int) {
        viewModel?.updateIntervalS = seconds
        applicationContext.contentResolver.update(
            TimetableDataProvider.CONFIGURATION_URI,
            ContentValues().apply {
                put(
                    ConfigurationContract.ConfigurationEntry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS,
                    seconds
                )
            },
            widgetId!!.toString(),
            null
        )
    }

    fun onIntervalRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            when (view.id) {
                R.id.interval_10s -> updateInterval(10)
                R.id.interval_30s -> updateInterval(30)
                R.id.interval_1m -> updateInterval(60)
                R.id.interval_5m -> updateInterval(300)
                R.id.interval_15m -> updateInterval(900)
                R.id.interval_30m -> updateInterval(1800)
            }
            intervalDialog.closeDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Creating configuration view for widget (id=$widgetId)")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuration_activity)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        applicationContext.contentResolver.registerContentObserver(
            TimetableDataProvider.CONFIGURATION_URI,
            true,
            ConfigurationObserver(this, widgetId!!) { config -> populateConfigurationView(config) }
        )
        applicationContext.contentResolver.notifyChange(
            TimetableDataProvider.CONFIGURATION_URI,
            null
        )
        attachEventHandlers()
        populateConfigurationView(viewModel)
    }

    private fun populateConfigurationView(currentConfig: TimetableConfigurationData?) {
        viewModel = currentConfig ?: viewModel ?: TimetableConfigurationData()
        val configurationList = findViewById<LinearLayout>(R.id.configurationList)
        configurationList.removeAllViews()
        fun createConfigurationItem(
            itemName: String,
            value: String?,
            onClick: () -> Unit,
            iconAsset: String? = null
        ): View {
            val item =
                layoutInflater.inflate(R.layout.configuration_item, configurationList).apply {
                    setOnClickListener {
                        onClick()
                    }
                }
            // Icon
            item.findViewById<ImageView>(R.id.configurationItemIcon).apply {
                if (iconAsset == null) visibility = android.view.View.INVISIBLE
            }
            // Main label (configuration item name)
            item.findViewById<TextView>(R.id.configItemName).apply { text = itemName }
            // Current value
            item.findViewById<TextView>(R.id.configItemValue).apply { text = value }
            return item!!
        }
        createConfigurationItem("Stop", viewModel!!.stopId, {
            val transaction = supportFragmentManager.beginTransaction()
            val stopDialog = StopDialog()
        })
        createConfigurationItem("Update interval", viewModel!!.getUpdateIntervalText(), {
            val transaction = supportFragmentManager.beginTransaction()
            intervalDialog = IntervalDialog(viewModel!!.updateIntervalS)
            transaction.add(intervalDialog, IntervalDialog.TAG)
            transaction.commit()
        })
    }
}
