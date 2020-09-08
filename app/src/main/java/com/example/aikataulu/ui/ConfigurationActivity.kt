package com.example.aikataulu.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.service.autofill.OnClickAction
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.aikataulu.*

class ConfigurationActivity : AppCompatActivity() {
    private lateinit var intervalDialog: IntervalDialog
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
    fun updateInterval(seconds: Int) {
        config.updateIntervalS = seconds
        populateConfigurationList(config)
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
        populateConfigurationList(config)
    }
    /* BEGIN List-style element population */

    fun populateConfigurationList(currentConfig: TimetableConfigurationData) {
        val configurationList = findViewById<LinearLayout>(R.id.configurationList)
        configurationList.removeAllViews()
        fun createConfigurationItem(itemName: String, value: String?, onClick: () -> Unit, iconAsset: String? = null): View {
            val item = layoutInflater.inflate(R.layout.configuration_item, configurationList).apply{
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
        createConfigurationItem("Stop", currentConfig.stopName, {
            val transaction = supportFragmentManager.beginTransaction()
            val stopDialog = StopDialog()
        })
        createConfigurationItem("Update interval", currentConfig.getUpdateIntervalText(), {
            val transaction = supportFragmentManager.beginTransaction()
            intervalDialog = IntervalDialog(widgetId!!)
            transaction.add(intervalDialog, IntervalDialog.TAG)
            transaction.commit()
        })

    }
    /* END List-style element population */
}
