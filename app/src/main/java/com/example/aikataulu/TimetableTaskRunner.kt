package com.example.aikataulu

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.example.aikataulu.database.contracts.ConfigurationContract
import com.example.aikataulu.providers.TimetableDataProvider
import java.util.*
import kotlin.collections.HashMap

/**
 * Handles the creation of the tasks that make API calls and update Timetable data.
 * Is a [ContentObserver], as the widget configuration is what dictates how a task runs.
 */
class TimetableTaskRunner(
    private val context: Context
) : ContentObserver(Handler()) {
    private val _tasks =
        HashMap<Int, Pair<Timer, TimetableTask>>()
    private var cursor: Cursor? = null

    /**
     * [Invoked by application]
     * When a widget's configuration is changed, get all configurations and re-create all tasks
     * accordingly. This could be improved by not canceling the existing tasks,
     * but in this app's context this is a suitable way of handling the task creation.
     * There is no additional waiting time before the interval hits, because when the task is
     * created, it will be run once immediately.
     *
     * The [uri] parameter is not used here, because this observer should only be registered to
     * listen to [ConfigurationProvider.CONFIGURATION_URI] uri.
     */
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Log.d(
            TAG,
            "[selfChange=$selfChange] ConfigurationObserver triggered by uri ${uri?.toString()}"
        )
        val entry =
            ConfigurationContract.ConfigurationEntry

        // Find and return configuration for all widgets
        cursor = context.contentResolver.query(
            TimetableDataProvider.CONFIGURATION_URI,
            null,
            null,
            null,
            null
        )

        // Cancel and clear all tasks and their timers
        cancelAllTasks()
        _tasks.clear()

        // Loop through all existing configurations and set up their tasks.
        while (cursor?.moveToNext() == true) {
            val config =
                ConfigurationContract.ConfigurationEntry.cursorToPoco(
                    cursor
                )
            if (config != null) {
                val widgetId = config.widgetId
                if (config.autoUpdate && widgetId != null && widgetId > 0) {
                    Log.d(
                        TAG,
                        "[WidgetId=$widgetId] Starting task with interval [${config.updateIntervalS} seconds]"
                    )

                    _tasks[widgetId] = Pair(
                        Timer(),
                        TimetableTask(context, config)
                    )
                    // Run the task
                    _tasks[widgetId]!!.first.scheduleAtFixedRate(
                        _tasks[widgetId]!!.second,
                        0, // Immediately run once
                        (config.updateIntervalS * 1000).toLong()
                    )
                } else {
                    Log.i(
                        TAG,
                        "[WidgetId=$widgetId]: AutoUpdate=${config.autoUpdate}. Not starting task."
                    )
                }
            } else Log.w(TAG, "Null configuration")
        }
    }

    fun cancelAllTasks() {
        _tasks.forEach {
            it.value.first.cancel()
            it.value.second.cancel()
        }
    }

    companion object {
        private const val TAG = "TIMETABLE.ServiceConfigurationObserver"
    }
}