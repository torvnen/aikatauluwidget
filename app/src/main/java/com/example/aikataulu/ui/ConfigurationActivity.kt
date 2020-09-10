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
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aikataulu.*
import com.example.aikataulu.database.contracts.ConfigurationContract

class ConfigurationActivity : AppCompatActivity() {
    companion object {
        var widgetId: Int? = null
        private const val TAG = "TIMETABLE.ConfigurationActivity"
        private lateinit var intervalDialog: IntervalDialog
        private var viewModel: TimetableConfiguration? = null
        private lateinit var observer: ConfigurationObserver
    }

    class ConfigurationObserver(
        val context: Context,
        val widgetId: Int,
        val callback: (TimetableConfiguration?) -> Unit
    ) : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            // Find and return configuration for this widget
            val cursor = context.contentResolver.query(
                TimetableDataProvider.CONFIGURATION_URI,
                null,
                widgetId.toString(),
                null,
                null
            )
            callback(ConfigurationContract.ConfigurationEntry.cursorToPoco(cursor))
        }
    }

    private fun attachEventHandlers() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val wId = widgetId!! // Make it throw right away if null/unset. Should never occur.
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

    private fun update(vm: TimetableConfiguration) {
        val values = ContentValues().apply {
            if (vm.updateIntervalS != viewModel!!.updateIntervalS) {
                put(
                    ConfigurationContract.ConfigurationEntry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS,
                    vm.updateIntervalS
                )
            }
            if (vm.autoUpdate != viewModel!!.autoUpdate) {
                put(
                    ConfigurationContract.ConfigurationEntry.COLUMN_NAME_AUTO_UPDATE_ENABLED,
                    vm.autoUpdate
                )
            }
            if (vm.stopId != viewModel!!.stopId) {
                put(
                    ConfigurationContract.ConfigurationEntry.COLUMN_NAME_SELECTED_STOP_ID,
                    vm.stopId
                )
            }
        }
        applicationContext.contentResolver.update(
            TimetableDataProvider.CONFIGURATION_URI,
            values,
            widgetId!!.toString(),
            null
        )
        populateConfigurationView(viewModel)
    }

    fun onIntervalRadioButtonClicked(view: View) {
        val vm = viewModel!!
        if (view is RadioButton) {
            when (view.id) {
                R.id.interval_10s -> vm.updateIntervalS = 10
                R.id.interval_30s -> vm.updateIntervalS = (30)
                R.id.interval_1m -> vm.updateIntervalS = (60)
                R.id.interval_5m -> vm.updateIntervalS = (300)
                R.id.interval_15m -> vm.updateIntervalS = (900)
                R.id.interval_30m -> vm.updateIntervalS = (1800)
            }
            update(vm)
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
        observer =
            ConfigurationObserver(this, widgetId!!) { config -> populateConfigurationView(config) }
        applicationContext.contentResolver.registerContentObserver(
            TimetableDataProvider.CONFIGURATION_URI,
            true,
            observer
        )
        applicationContext.contentResolver.notifyChange(
            TimetableDataProvider.CONFIGURATION_URI,
            null
        )
        attachEventHandlers()
        populateConfigurationView(viewModel)
    }

    private fun populateConfigurationView(currentConfig: TimetableConfiguration?) {
        viewModel = currentConfig ?: viewModel ?: TimetableConfiguration()
        val configurationList = findViewById<LinearLayout>(R.id.configurationList)
        configurationList.removeAllViews()
        fun createConfigurationItem(
            itemName: String,
            value: String?,
            onClick: (View) -> Unit,
            iconAsset: String? = null,
            input: View? = null
        ): View {
            val item =
                layoutInflater.inflate(R.layout.configuration_item, null).apply {
                    setOnClickListener {
                        onClick(it)
                    }
                }
            // Icon
            item.findViewById<ImageView>(R.id.configurationItemIcon).apply {
                if (iconAsset == null) visibility = android.view.View.INVISIBLE
            }
            // Main label (configuration item name)
            item.findViewById<TextView>(R.id.configItemName).apply {
                text = itemName
            }
            // Current value
            item.findViewById<TextView>(R.id.configItemValue).apply {
                text = value
            }
            item.findViewById<FrameLayout>(R.id.configItemInput).apply {
                // Input, if any
                if (input != null) {
                    addView(input)
                }
            }
            return item!!
        }

        configurationList.addView(createConfigurationItem("Stop", viewModel!!.stopId, {
            val transaction = supportFragmentManager.beginTransaction()
            val stopDialog = StopDialog()
            transaction.add(stopDialog, StopDialog.TAG)
            transaction.commit()
        }))
        configurationList.addView(
            createConfigurationItem(
                "Automatic updates",
                "Enable periodical updates",
                {
                    val switch = it.findViewById<Switch>(R.id.autoUpdateSwitch)
                    switch.toggle()
                    viewModel!!.autoUpdate = switch.isChecked
                },
                null,
                Switch(this).apply {
                    id = R.id.autoUpdateSwitch
                    text = ""
                })
        )
        configurationList.addView(
            createConfigurationItem(
                "Update interval",
                viewModel!!.getUpdateIntervalText(),
                {
                    val transaction = supportFragmentManager.beginTransaction()
                    intervalDialog = IntervalDialog(viewModel!!.updateIntervalS)
                    transaction.add(intervalDialog, IntervalDialog.TAG)
                    transaction.commit()
                })
        )
    }
}
