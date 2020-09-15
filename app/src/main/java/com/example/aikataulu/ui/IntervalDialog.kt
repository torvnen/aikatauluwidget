package com.example.aikataulu.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.aikataulu.ConfigurationProvider
import com.example.aikataulu.R
import com.example.aikataulu.database.contracts.ConfigurationContract


/**
 * Handles the selecting of the update interval in ConfigurationActivity.
 * The selected value is set as the [DialogResult]
 */
class IntervalDialog(
    private val widgetId: Int,
    private val currentIntervalS: Int,
    private val activity: ConfigurationActivity
) : DialogFragment() {
    private val entry = ConfigurationContract.ConfigurationEntry
    private val onIntervalRadioButtonClicked = View.OnClickListener {
        var updateIntervalS: Int = currentIntervalS
        when (it.id) {
            R.id.interval_10s -> updateIntervalS = 10
            R.id.interval_30s -> updateIntervalS = 30
            R.id.interval_1m -> updateIntervalS = 60
            R.id.interval_5m -> updateIntervalS = 300
            R.id.interval_15m -> updateIntervalS = 900
            R.id.interval_30m -> updateIntervalS = 1800
        }

        // Update database
        activity.contentResolver.update(
            ConfigurationProvider.CONFIGURATION_URI,
            ContentValues().apply {
                put(entry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS, updateIntervalS)
            },
            "${entry.COLUMN_NAME_WIDGET_ID} = ?",
            arrayOf(widgetId.toString())
        )

        // Notify observers
        activity.contentResolver.notifyChange(
            ConfigurationProvider.CONFIGURATION_URI,
            null
        )

        closeDialog()
    }

    /**
     * [Invoked by framework]
     * Builds the view from the layout file, attaches click handlers,
     * and sets the UI state.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_intervalconfig, null)

        // Set Radio Group attributes
        view.findViewById<RadioGroup>(R.id.intervalRadioGroup).apply {
            // Attach click handlers for the radio buttons
            for (i in 0..childCount) {
                val child = getChildAt(i)
                if (child is RadioButton) child.setOnClickListener(onIntervalRadioButtonClicked)
            }
            // Set current value
            when (currentIntervalS) {
                10 -> check(R.id.interval_10s)
                30 -> check(R.id.interval_30s)
                60 -> check(R.id.interval_1m)
                300 -> check(R.id.interval_5m)
                900 -> check(R.id.interval_15m)
                1800 -> check(R.id.interval_30m)
            }
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select update interval")
        builder.setView(view)
        return builder.create()
    }

    /**
     * When the new Interval value is selected, the data will be updated immediately.
     * Because of ContentObservers, there is no need to set any dialog results or such.
     */
    private fun closeDialog() {
        dialog!!.dismiss()
    }

    companion object {
        const val TAG = "TIMETABLE.IntervalDialog"
        const val DIALOG_RESULT_OK = 1
        const val EXTRA_SELECTED_INTERVAL = "selectedInterval"
    }
}