package com.example.aikataulu.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.aikataulu.R
import com.example.aikataulu.TimetableService
import com.example.aikataulu.TimetableServiceConnection
import com.example.aikataulu.databinding.MainFragmentBinding

class MainFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private var _binding: MainFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val serviceConnection = TimetableServiceConnection()

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (context != null) {
            Log.i("TIMETABLE", "Binding fragment to service...")
            val isSuccess = context?.bindService(Intent(context, TimetableService::class.java), serviceConnection, 0)// TODO check flags
            if (isSuccess == true) Log.i("TIMETABLE", "Successfully bound fragment to service.")
            else Log.w("TIMETABLE", "Failed to bind fragment to service.")
        } else Log.w("TIMETABLE", "Cannot bind service to context: context is null")

        // Setup ViewModel event handlers
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding.stopName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.stopName = s.toString()
                serviceConnection.updateStopName(s.toString())
            }
        })
        binding.autoUpdate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) serviceConnection.startAutoUpdate()
            else serviceConnection.stopAutoUpdate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
