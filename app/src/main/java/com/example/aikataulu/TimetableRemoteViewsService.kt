package com.example.aikataulu

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.TextView

// https://android.googlesource.com/platform/development/+/master/samples/WeatherListWidget/src/com/example/android/weatherlistwidget/WeatherWidgetService.java
class TimetableRemoteViewsService : RemoteViewsService() {
    companion object {
        const val TAG = "TIMETABLE.RemoteViewsService"
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ViewFactory(applicationContext, intent!!)
    }
    class ViewFactory(private val _context: Context, private val _intent: Intent) : RemoteViewsService.RemoteViewsFactory {
        private var _stopId: String? = null
        private var _widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
        private var _cursor: Cursor? = null

        companion object {
            const val EXTRA_STOP_ID = "EXTRA_STOP_ID"
        }

        override fun onCreate() {
            _widgetId = _intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            Log.i(TAG, "Created RemoteViewsFactory for widget (id=$_widgetId)")
//            val departuresJson = intent.getStringExtra(TimetableWidgetProvider.EXTRA_DEPARTURES)
//            departures = Gson().fromJson(departuresJson, TimetableWidgetProvider.departuresJsonType)
        }

        override fun getLoadingView(): RemoteViews? {
            Log.i(TAG, "getLoadingView()")
            return null
        }

        override fun getItemId(position: Int): Long {
            Log.i(TAG, "getItemId($position)")
//            return if (_cursor?.moveToPosition(position) == true) _cursor!!.getLong(0) else position.toLong()
            return position.toLong()
        }

        override fun onDataSetChanged() {
            Log.i(TAG, "onDataSetChanged()")
            _cursor?.close()
            // https://www.sitepoint.com/killer-way-to-show-a-list-of-items-in-android-collection-widget/
            _cursor = _context.contentResolver.query(TimetableDataProvider.CONTENT_URI, null, null, null, null)
        }

        override fun hasStableIds(): Boolean {
            Log.i(TAG, "hasStableIds()")
            return true
        }

        override fun getViewAt(position: Int): RemoteViews? {
            if (position == AdapterView.INVALID_POSITION || _cursor?.moveToPosition(position) != true) {
                return null
            }
//            val x: View = LayoutInflater.from(_context).inflate(R.layout.single_departure, null) // TODO avoid passing null
//                .apply {
//                    findViewById<TextView>(R.id.sd_tvRouteName).text = "_TEST_"
//                }
            return RemoteViews(_context.packageName, R.layout.single_departure).apply {
                setTextViewText(R.id.sd_tvRouteName, "TEST")
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