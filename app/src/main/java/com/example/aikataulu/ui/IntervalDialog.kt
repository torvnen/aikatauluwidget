package com.example.aikataulu.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.aikataulu.R

class IntervalDialog(val widgetId: Int) : DialogFragment() {
    companion object {
        const val TAG = "TIMETABLE.IntervalDialog"
    }

    fun closeDialog() {
        dialog?.dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select update interval")
        builder.setView(R.layout.fragment_intervalconfig)
        return builder.create()
    }
}