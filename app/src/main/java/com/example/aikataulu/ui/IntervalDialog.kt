package com.example.aikataulu.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.aikataulu.R

class IntervalDialog(val currentIntervalS: Int) : DialogFragment() {
    companion object {
        const val TAG = "TIMETABLE.IntervalDialog"
    }

    fun closeDialog() {
        dialog?.dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_intervalconfig, null)
        val radioGroup = view.findViewById<RadioGroup>(R.id.intervalRadioGroup)
        when (currentIntervalS) {
            10 -> radioGroup.check(R.id.interval_10s)
            30 -> radioGroup.check(R.id.interval_30s)
            60 -> radioGroup.check(R.id.interval_1m)
            300 -> radioGroup.check(R.id.interval_5m)
            900 -> radioGroup.check(R.id.interval_15m)
            1800 -> radioGroup.check(R.id.interval_30m)
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select update interval")
        builder.setView(view)
        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}