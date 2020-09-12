package com.example.aikataulu.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aikataulu.*
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.providers.TimetableDataProvider
import com.google.gson.Gson

class ConfigurationActivity : AppCompatActivity() {

    companion object {
        var widgetId: Int? = null
        private const val TAG = "TIMETABLE.ConfigurationActivity"
        private lateinit var intervalDialog: IntervalDialog
        private var viewModel: TimetableConfiguration? = null
        private lateinit var observer: ConfigurationObserver
        private var doesWidgetExist: Boolean? = null
    }

    class ConfigurationObserver(
        private val context: Context,
        private val widgetId: Int,
        val callback: (TimetableConfiguration?) -> Unit
    ) : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "[selfChange=$selfChange] ConfigurationObserver triggered")
            // Find and return configuration for this widget
            val cursor = context.contentResolver.query(
                TimetableDataProvider.CONFIGURATION_URI,
                null,
                "${ConfigurationContract.ConfigurationEntry.COLUMN_NAME_WIDGET_ID} = ?",
                arrayOf(widgetId.toString()),
                null
            )
            cursor?.moveToFirst()
            callback(ConfigurationContract.ConfigurationEntry.cursorToPoco(cursor))
        }
    }

    private fun contentValues(vm: TimetableConfiguration): ContentValues {
        val entry = ConfigurationContract.ConfigurationEntry
        return ContentValues().apply {
            put(entry.COLUMN_NAME_WIDGET_ID, widgetId)
            put(
                entry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS,
                vm.updateIntervalS
            )
            put(
                entry.COLUMN_NAME_AUTO_UPDATE_ENABLED,
                vm.autoUpdate
            )
            put(
                entry.COLUMN_NAME_SELECTED_STOP_ID,
                vm.stopId
            )
        }
    }

    private fun save(vm: TimetableConfiguration) {
        val values = contentValues(vm)
        applicationContext.contentResolver.insert(
            TimetableDataProvider.CONFIGURATION_URI,
            values
        )
        // Always notify observers
        applicationContext.contentResolver.notifyChange(
            TimetableDataProvider.CONFIGURATION_URI,
            observer
        )
    }

    private fun update(vm: TimetableConfiguration, redraw: Boolean = false) {
        if (doesWidgetExist == true) {
            val entry = ConfigurationContract.ConfigurationEntry
            val values = contentValues(vm)
            applicationContext.contentResolver.update(
                TimetableDataProvider.CONFIGURATION_URI,
                values,
                "${entry.COLUMN_NAME_WIDGET_ID} = ?",
                arrayOf(widgetId!!.toString())
            )
            // No reason to update if it's the initial creation phase
            applicationContext.contentResolver.notifyChange(
                TimetableDataProvider.CONFIGURATION_URI,
                if (redraw) null else observer
            )
        } else if (redraw) render(vm)
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
            update(vm, true)
            intervalDialog.closeDialog()
        }
    }

    private fun getExistingConfigurationOrNull(widgetId: Int): TimetableConfiguration? {
        val cursor = applicationContext.contentResolver.query(
            ConfigurationProvider.CONFIGURATION_URI, null,
            "${ConfigurationContract.ConfigurationEntry.COLUMN_NAME_WIDGET_ID} = ?",
            arrayOf(widgetId.toString()),
            null,
            null
        )
        return if (cursor?.moveToFirst() == true) ConfigurationContract.ConfigurationEntry.cursorToPoco(
            cursor
        ) else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Creating configuration view for widget (id=$widgetId)")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuration_activity)

        // Process Extras
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        viewModel = getExistingConfigurationOrNull(widgetId!!)
        doesWidgetExist = viewModel != null

        // Register an observer that will trigger (re)rendering the view
        observer =
            ConfigurationObserver(this, widgetId!!) {
                render(it)
            }
        applicationContext.contentResolver.registerContentObserver(
            TimetableDataProvider.CONFIGURATION_URI,
            true,
            observer
        )
        // Trigger the initial render
        render(viewModel)
        // Attach Save (or Exit) functionality
        findViewById<Button>(R.id.saveButton).let {
            it.text = "Save and exit"
            it.setOnClickListener {
                save(viewModel!!)
                val wId = widgetId!! // Make it throw right away if null/unset. Should never occur.
                // Notify Widget Provider of config changes
                // Send empty bundle (for now), the config will be read via other methods
                AppWidgetManager.getInstance(applicationContext)
                    .updateAppWidgetOptions(wId, Bundle())

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
    }

    private fun render(currentConfig: TimetableConfiguration?) {
        viewModel = currentConfig ?: viewModel ?: TimetableConfiguration()
        viewModel!!.widgetId = widgetId
        Log.d(TAG, "Rendering configuration items. Config VM: ${Gson().toJson(viewModel!!)}")
        val configurationList = findViewById<LinearLayout>(R.id.configurationList)
        configurationList.removeAllViews()
        fun createConfigurationItem(
            itemName: String,
            value: String?,
            onClick: (View) -> Unit,
            iconDrawable: Drawable? = null,
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
                if (iconDrawable == null) visibility = android.view.View.INVISIBLE
                else setImageDrawable(iconDrawable)
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

        val stopText = if (viewModel?.stopId != null) StopProvider.getStopByIdOrNull(
            viewModel!!.stopId!!,
            applicationContext
        ).let {
            "${it!!.name} (${it!!.hrtId})"
        } else "Touch to select a stop"
        configurationList.addView(
            createConfigurationItem(
                "Stop",
                stopText,
                {
                    val transaction = supportFragmentManager.beginTransaction()
                    val stopDialog = StopDialog(viewModel!!) {
                        update(it, true)
                    }
                    transaction.add(stopDialog, StopDialog.TAG)
                    transaction.commit()
                },
                resources.getDrawable(R.drawable.outline_pin_drop_black_48, null)
            )
        )
        configurationList.addView(
            createConfigurationItem(
                "Automatic updates",
                "Enable periodical updates",
                {
                    val switch = it.findViewById<Switch>(R.id.autoUpdateSwitch)
                    switch.toggle()
                },
                resources.getDrawable(R.drawable.outline_update_black_48, null),
                Switch(this).apply {
                    id = R.id.autoUpdateSwitch
                    text = ""
                    isChecked = viewModel!!.autoUpdate
                    setOnCheckedChangeListener { _, isChecked: Boolean ->
                        viewModel!!.autoUpdate = isChecked
                        update(viewModel!!)
                    }
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
                },
                resources.getDrawable(R.drawable.outline_timer_black_48, null)
            )
        )
    }
}
