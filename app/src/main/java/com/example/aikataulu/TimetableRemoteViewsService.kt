package com.example.aikataulu

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService

// https://android.googlesource.com/platform/development/+/master/samples/WeatherListWidget/src/com/example/android/weatherlistwidget/WeatherWidgetService.java
class TimetableRemoteViewsService : RemoteViewsService() {
    companion object {
        const val TAG = "TIMETABLE.RemoteViewsService"
    }

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return ViewFactory(applicationContext, intent!!)
    }
    class ViewFactory(private val _context: Context, private val _intent: Intent) : RemoteViewsService.RemoteViewsFactory {
        private var _widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
//        private lateinit var departures: ArrayList<Departure>
        private var _cursor: Cursor? = null

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
            return position.toLong()
        }

        override fun onDataSetChanged() {
            Log.i(TAG, "onDataSetChanged()")
            _cursor?.close()
            // https://www.sitepoint.com/killer-way-to-show-a-list-of-items-in-android-collection-widget/
            _cursor = _context.contentResolver.query(TimetableDataProvider.CONTENT_URI, null, null, null)
        }

        override fun hasStableIds(): Boolean {
            Log.i(TAG, "hasStableIds()")
            return true
        }

        override fun getViewAt(position: Int): RemoteViews? {
            if (position == AdapterView.INVALID_POSITION || _cursor?.moveToPosition(position) != true) {
                return null
            }
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