package com.example.aikataulu.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.aikataulu.R
import com.example.aikataulu.TimetableConfiguration
import com.example.aikataulu.providers.TimetableDataProvider
import com.example.aikataulu.api.Api
import com.example.aikataulu.database.contracts.StopContract
import com.example.aikataulu.models.Stop
import com.jakewharton.rxbinding4.widget.textChanges

class StopDialog(var config: TimetableConfiguration, val saveFn: (TimetableConfiguration) -> Unit) :
    DialogFragment() {
    private val searches = HashMap<String, ArrayList<Stop>>()
    private var suggestions: List<String> = ArrayList<String>()
    private var searchTerm: String = ""
    private var selectedStopId: String? = null
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>
    private lateinit var autoComplete: AutoCompleteTextView

    companion object {
        const val TAG = "TIMETABLE.StopDialog"
    }

    init {
        // Empty search always gives empty results
        searches[""] = arrayListOf()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity!!
        // Create adapter before subscribing to any events
        autoCompleteAdapter = ArrayAdapter(
            activity,
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        )
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.fragment_stopconfig, null)
        autoComplete = view.findViewById<AutoCompleteTextView>(R.id.stopAutoComplete).apply {
            threshold = 0
            // Execute an API call to fill the suggestions
            textChanges().subscribe { text ->
                val textStr = text.toString()
                if (text.isNotEmpty() && !searches.containsKey(textStr)) {
                    Api.getStopsContainingText(textStr, {
                        searches[textStr] = it
                        Log.d(TAG, "Found ${it.size} stops for search term $textStr")
                        if (autoComplete.text.toString() == textStr) {
                            activity!!.runOnUiThread { setSuggestions(it) }
                        }
                    }, {})
                }
            }
            // Set the suggestions to be the search results when text is changed.
            textChanges().subscribe { text ->
                val textStr = text.toString()
                if (searchTerm != textStr) {
                    searchTerm = textStr
                    if (searches.containsKey(searchTerm)) {
                        Log.d(TAG, "A search result for str=\"$searchTerm\" exists.")
                        setSuggestions(searches[searchTerm]!!)
                    }
                }
            }
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val text = (view as TextView).text
                    val stopName = text.substring(0, text.lastIndexOf('(') - 1)
                    val idStart = text.lastIndexOf('(') + 1
                    val idEnd = text.lastIndexOf(')')
                    val hrtId = text.substring(idStart, idEnd)
                    selectedStopId = hrtId

                    // Insert selected stop to DB
                    activity!!.applicationContext.contentResolver.insert(
                        TimetableDataProvider.STOP_URI,
                        ContentValues().apply {
                            val entry = StopContract.StopEntry
                            put(entry.COLUMN_NAME_STOPNAME, stopName)
                            put(entry.COLUMN_NAME_HRTID, hrtId)
                        })
                }
            setAdapter(autoCompleteAdapter)
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select stop")
        builder.setView(view)
        builder.setPositiveButton("OK") { d: DialogInterface, _ ->
            // TODO validate
            if (selectedStopId != null) {
                config.stopId = selectedStopId
                saveFn(config)
                d.dismiss()
            } else d.cancel()
        }
        builder.setNegativeButton("Cancel") { d: DialogInterface, _ ->
            d.dismiss()
        }
        return builder.create()
    }

    private fun setSuggestions(newSuggestions: List<Stop>) {
        if (suggestions.contains(searchTerm)) {
            // The user has selected an item. Skip setting suggestions.
            return
        }
        Log.d(TAG, "Setting ${newSuggestions.size} suggestions")
        suggestions = newSuggestions.map { stop -> stop.name + " (${stop.hrtId})" }
        autoCompleteAdapter.clear()
        autoCompleteAdapter.addAll(suggestions)
        autoCompleteAdapter.notifyDataSetChanged()
        // HACK: reset text to force a change
        val textSelection = Pair(autoComplete.selectionStart, autoComplete.selectionEnd)
        autoComplete.text = autoComplete.text
        autoComplete.setSelection(textSelection.first, textSelection.second)
    }
}