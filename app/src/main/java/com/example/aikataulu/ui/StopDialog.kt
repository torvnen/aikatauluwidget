package com.example.aikataulu.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import com.apollographql.apollo.ApolloCall
import com.example.aikataulu.R
import com.example.aikataulu.StopByNameQuery
import com.example.aikataulu.api.Api
import com.example.aikataulu.models.Stop
import com.jakewharton.rxbinding4.widget.textChanges
import okhttp3.internal.notify
import java.util.concurrent.TimeUnit

class StopDialog : DialogFragment() {
    companion object {
        const val TAG = "TIMETABLE.StopDialog"
    }

    private val searches = HashMap<String, ArrayList<Stop>>()
    private val suggestions = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_stopconfig, null)
        val autoComplete = view.findViewById<AutoCompleteTextView>(R.id.stopAutoComplete)
        autoComplete.textChanges().debounce(500, TimeUnit.MILLISECONDS)
            .subscribe { text ->
                val textStr = text.toString()
                if (text.isNotEmpty() && !searches.containsKey(textStr)) {
                    Api.getStopsContainingText(textStr, {
                        searches[textStr] = it
                        if (autoComplete.text.toString() == text.toString()) {
                            activity!!.runOnUiThread {setSuggestions(it)}
                        }
                    }, {})
                }
            }
        autoComplete.textChanges().subscribe { text ->
            val textStr = text.toString()
            if (searches.containsKey(textStr)) {
                setSuggestions(searches[textStr]!!)
            }
        }
        adapter = ArrayAdapter<String>(
            activity!!,
            android.R.layout.simple_dropdown_item_1line,
            suggestions
        ).apply {
            setNotifyOnChange(true)
        }
        autoComplete.setAdapter(adapter)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select stop")
        builder.setView(view)
        return builder.create()
    }

    private fun setSuggestions(newSuggestions: List<Stop>) {
        Log.d(TAG, "Setting suggestions")
        adapter.clear()
        adapter.addAll(newSuggestions.map { stop -> stop.name + " (${stop.hrtId})" })
    }
}