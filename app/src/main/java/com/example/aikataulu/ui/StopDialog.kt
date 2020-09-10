package com.example.aikataulu.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import com.example.aikataulu.R

class StopDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_stopconfig, null)
        val autoComplete = view.findViewById<AutoCompleteTextView>(R.id.stopAutoComplete)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select stop")
        builder.setView(view)
        return super.onCreateDialog(savedInstanceState)
    }
}