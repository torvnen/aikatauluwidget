package com.example.aikataulu

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.aikataulu.models.Timetable
import com.example.aikataulu.providers.TimetableDataProvider
import com.example.aikataulu.ui.ConfigurationActivity

// https://android.googlesource.com/platform/development/+/master/samples/WeatherListWidget/src/com/example/android/weatherlistwidget/WeatherWidgetService.java
class TimetableRemoteViewsService : RemoteViewsService() {
    companion object {
        const val TAG = "TIMETABLE.RemoteViewsService"
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: 0
        Log.d(TAG, "[WidgetId=$widgetId] onGetViewFactory")
        return ViewFactory(widgetId, applicationContext, intent!!)
    }

    class ViewFactory(
        private val _widgetId: Int,
        private val _context: Context,
        private val _intent: Intent
    ) : RemoteViewsFactory {
        private var _cursor: Cursor? = null

        override fun onCreate() {
            Log.d(TAG, "Created RemoteViewsFactory. Intent.action=${_intent.action}")
        }

        override fun getLoadingView(): RemoteViews? {
            Log.i(TAG, "getLoadingView()")
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
            Log.i(TAG, "onDataSetChanged()")
            _cursor?.close()
            // Get all data for widgets that have out-of-date data
            _cursor = _context.contentResolver.query(
                TimetableDataProvider.TIMETABLE_DATA_URI,
                null,
                "${Timetable.WIDGET_ID} = ?",
                arrayOf(_widgetId.toString()),
                null
            )
//            Log.d(TAG, "Cursor found ${_cursor?.count} rows")
//            while (_cursor?.moveToNext() == true) {
//                val cursor = _cursor!!
//                // This data set has changed.
//                val widgetId = cursor.getInt(cursor.getColumnIndex(Timetable.WIDGET_ID))
//                AppWidgetManager.getInstance(_context)
//                    .updateAppWidget(
//                        widgetId,
//                        RemoteViews(_context.packageName, R.layout.widget).apply {
//                            // Do manual updating here, the adapter will handle the list creation.
//                            // Set the widget title
//                        })
//            }
        }

        override fun hasStableIds(): Boolean {
            Log.i(TAG, "hasStableIds()")
            return true
        }

        override fun getViewAt(position: Int): RemoteViews? {
            if (position == AdapterView.INVALID_POSITION || _cursor?.moveToPosition(position) != true) {
                Log.w(TAG, "Invalid position in getViewAt()")
                return null
            }

            val cursor = _cursor!!
            val colIdx = object {
                val routeShortName = cursor.getColumnIndex(Timetable.COLUMN_ROUTE_SHORT_NAME)
                val headsign = cursor.getColumnIndex(Timetable.COLUMN_HEADSIGN)
                val departureScheduled = cursor.getColumnIndex(Timetable.COLUMN_DEPARTURE_SCHEDULED)
                val departureRealtime = cursor.getColumnIndex(Timetable.COLUMN_DEPARTURE_REALTIME)
                val isOnTime = cursor.getColumnIndex(Timetable.COLUMN_IS_ON_TIME)
            }

            return RemoteViews(_context.packageName, R.layout.single_departure).apply {
                setTextViewText(R.id.sd_tvRouteName, cursor.getString(colIdx.routeShortName))
                setTextViewText(R.id.sd_tvHeadsign, cursor.getString(colIdx.headsign))
                setTextViewText(
                    R.id.sd_tvDepartureScheduled,
                    cursor.getString(colIdx.departureScheduled)
                )
                val isOnTime = "true".equals(cursor.getString(colIdx.isOnTime), true)
                setTextViewText(
                    R.id.sd_tvDepartureRealtime,
                    if (isOnTime) "" else " (${cursor.getString(colIdx.departureRealtime)})"
                )
            }
        }

        override fun getCount(): Int {
//            return if (this::departures.isInitialized) departures.count() else 0
            return _cursor?.count ?: 0
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {
            Log.i(TAG, "onDestroy()")
        }
    }
}