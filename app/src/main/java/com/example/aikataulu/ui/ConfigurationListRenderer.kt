package com.example.aikataulu.ui

import android.database.ContentObserver
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import com.example.aikataulu.ConfigurationProvider
import com.example.aikataulu.R
import com.example.aikataulu.StopProvider
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.models.TimetableConfiguration
import com.example.aikataulu.providers.TimetableDataProvider
import com.google.gson.Gson

class ConfigurationListRenderer(
    private val activity: ConfigurationActivity,
    val widgetId: Int
) : ContentObserver(Handler()) {
    private val applicationContext = activity.applicationContext
    private lateinit var intervalDialog: IntervalDialog

    init {
        // Register this as a content observer. Remember to also unregister it!
        applicationContext.contentResolver.registerContentObserver(
            TimetableDataProvider.CONFIGURATION_URI,
            true,
            this
        )
        // Notify of a change to trigger initial render.
        activity.applicationContext.contentResolver.notifyChange(
            ConfigurationProvider.CONFIGURATION_URI,
            null
        )
    }

    /**
     * [Invoked by application]
     * When this widget's configuration changes (or an initial query is requested),
     * find the configuration, and invoke the rendering of the config items.
     */
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Log.d(TAG, "[selfChange=$selfChange, WidgetId=$widgetId] Observer triggered")
        if (!selfChange) {
            val existingConfig =
                ConfigurationProvider.getExistingConfigurationOrNull(
                    widgetId,
                    activity.applicationContext
                )!!
            render(existingConfig)
        }
    }

    /** Update this configuration's values to database. */
    private fun update(config: TimetableConfiguration, redraw: Boolean = false) {
        val entry = ConfigurationContract.ConfigurationEntry
        val values = ConfigurationActivity.configToContentValues(config)

        applicationContext.contentResolver.update(
            TimetableDataProvider.CONFIGURATION_URI,
            values,
            "${entry.COLUMN_NAME_WIDGET_ID} = ?",
            arrayOf(widgetId.toString())
        )
        applicationContext.contentResolver.notifyChange(
            TimetableDataProvider.CONFIGURATION_URI,
            if (redraw) null else this
        )
    }

    /** [Invoked by overridden [onChange] method] Builds the configuration list. */
    private fun render(config: TimetableConfiguration) {
        val configJson = Gson().toJson(config) // For debugging purposes
        Log.d(TAG, "Rendering configuration items. Config JSON: $configJson")

        // Target container
        val configurationList = activity.findViewById<LinearLayout>(R.id.configurationList)
        configurationList.removeAllViews()


        // Add a dialog-type configuration item for selecting the Stop
        // Format the text
        val stopText = if (config.stopId != null) {
            StopProvider.getStopByIdOrNull(
                config.stopId!!,
                applicationContext
            ).let {
                val stop = it!!
                "${stop.name} (${stop.hrtId})"
            }
        } else "Touch to select a stop"
        configurationList.addView(
            createConfigurationItem(
                "Stop",
                stopText,
                {
                    val transaction = activity.supportFragmentManager.beginTransaction()
                    val stopDialog = StopDialog(config) {
                        update(it, true)
                    }
                    transaction.add(stopDialog, StopDialog.TAG)
                    transaction.commit()
                },
                activity.resources.getDrawable(R.drawable.outline_pin_drop_black_48, null)
            )
        )

        // Add a switch-type configuration item for enabling periodical updates
        configurationList.addView(
            createConfigurationItem(
                "Automatic updates",
                "Enable periodical updates",
                {
                    val switch = it.findViewById<Switch>(R.id.autoUpdateSwitch)
                    switch.toggle()
                },
                activity.resources.getDrawable(R.drawable.outline_update_black_48, null),
                Switch(activity.applicationContext).apply {
                    id = R.id.autoUpdateSwitch
                    text = ""
                    isChecked = config.autoUpdate
                    setOnCheckedChangeListener { _, isChecked: Boolean ->
                        config.autoUpdate = isChecked
                        update(config)
                    }
                })
        )

        // Add a dialog-type configuration item for Update Interval
        configurationList.addView(
            createConfigurationItem(
                "Update interval",
                config.getUpdateIntervalText(),
                {
                    val transaction = activity.supportFragmentManager.beginTransaction()
                    intervalDialog =
                        IntervalDialog(config.widgetId!!, config.updateIntervalS, activity)
                    transaction.add(intervalDialog, IntervalDialog.TAG)
                    transaction.commit()
                },
                activity.resources.getDrawable(R.drawable.outline_timer_black_48, null)
            )
        )
    }

    /** Inflate the layout and fill the assigned containers with the corresponding content. */
    private fun createConfigurationItem(
        itemName: String,
        value: String?,
        onClick: (View) -> Unit,
        iconDrawable: Drawable? = null,
        input: View? = null
    ): View {
        val item =
            activity.layoutInflater.inflate(R.layout.configuration_item, null).apply {
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

    companion object {
        private const val TAG = "TIMETABLE.ConfigurationListRenderer"
    }
}