package com.example.aikataulu

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.aikataulu.api.Api

class TimetableServiceConnection : ServiceConnection {
    private lateinit var _service: TimetableService
    private var _isBound = false

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i("TIMETABLE", "Component $name invoked onServiceDisconnected")
        _isBound = false
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i("TIMETABLE", "Component $name is connecting to the service...")
        _service = (service as TimetableServiceBinder).service!!
        _isBound = true
        Log.i("TIMETABLE", "Component $name is connected to the service.")
    }

    fun updateStopName(stopName: String): Boolean {
        if (!_isBound) return false
        val stops = Api.getStopsContainingText(stopName)
        val hasStops = stops.count() > 0
        if (hasStops) {
            _service.setStopId(stops[0].hrtId)
        }
        return hasStops
    }

    fun startAutoUpdate(): Boolean {
        if (!_isBound) {
            Log.w("TIMETABLE", "Cannot start auto-update: service not bound")
            return false
        }
        Log.i("TIMETABLE", "Starting auto-update")
        _service.setAutoUpdate(true)
        return true
    }

    fun stopAutoUpdate(): Boolean {
        if (!_isBound) {
            Log.w("TIMETABLE", "Cannot stop auto-update: service not bound")
            return false
        }
        Log.i("TIMETABLE", "Stopping auto-update")
        _service.setAutoUpdate(false)
        return true
    }
}