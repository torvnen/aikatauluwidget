package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.text.TimeZoneFormat
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aikataulu.api.Api
import java.io.FileDescriptor
import java.util.*
import kotlin.collections.ArrayList

class TimetableService : Service() {
    private val binder = TimetableServiceBinder()
    private lateinit var _timerTask: TimerTask
    private lateinit var _timer: Timer

    companion object {
        const val creationNotificationId = 12559999
        const val destructionNotificationId = 12559998
        var isAutoUpdate = false
        var stopId = ""
        var interval = 10
    }

    inner class TimetableServiceBinder : IBinder {
        fun getService(): TimetableService {
            return this@TimetableService
        }

        override fun getInterfaceDescriptor(): String? {
            TODO("Not yet implemented")
        }

        override fun isBinderAlive(): Boolean {
            TODO("Not yet implemented")
        }

        override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
            TODO("Not yet implemented")
        }

        override fun queryLocalInterface(descriptor: String): IInterface? {
            TODO("Not yet implemented")
        }

        override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            TODO("Not yet implemented")
        }

        override fun dumpAsync(fd: FileDescriptor, args: Array<out String>?) {
            TODO("Not yet implemented")
        }

        override fun dump(fd: FileDescriptor, args: Array<out String>?) {
            TODO("Not yet implemented")
        }

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean {
            TODO("Not yet implemented")
        }

        override fun pingBinder(): Boolean {
            TODO("Not yet implemented")
        }
    }

    fun setAutoUpdate(b: Boolean) {
        _timerTask.cancel()
        if (b) {
            _timer = Timer()
            _timer.scheduleAtFixedRate(_timerTask, 0, (1000 * interval).toLong())
        }
    }
    fun setStopId(id: String) {
        stopId = id
        setAutoUpdate(isAutoUpdate)
    }

    override fun onBind(intent: Intent?): TimetableServiceBinder {
        Log.i("TIMETABLE", "Service.onBind()")
        return binder
    }

    override fun onCreate() {
        Log.i("TIMETABLE", "Service.onCreate()")
        super.onCreate()

        if (!MainActivity.notificationChannelInitiated) {
            MainActivity.initiateNotificationChannel(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        }

        val builder = NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_NAME)
            .setSmallIcon(R.mipmap.icon)
            .setContentText("Timetable service started")
            .setPriority(NotificationCompat.PRIORITY_MIN)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            //startForeground(creationNotificationId, builder.build())
        }

        _timerTask = object: TimerTask() {
            override fun run() {
                Log.i("TIMETABLE", "Fetching data...")
                val arrivals = Api.getArrivalsForStop(stopId)
                Log.i("TIMETABLE", "Received ${arrivals.count()} arrivals")
            }
        }
    }

    override fun onDestroy() {
        _timerTask.cancel()
        Log.i("TIMETABLE", "Service.onDestroy()")
        super.onDestroy()
    }
}