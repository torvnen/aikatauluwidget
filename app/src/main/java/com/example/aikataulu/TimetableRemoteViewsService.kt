package com.example.aikataulu

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService

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
            _cursor = _context.contentResolver.query()
        }

        override fun hasStableIds(): Boolean {
            Log.i(TAG, "hasStableIds()")
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            return RemoteViews(_context.packageName, R.id.widget_content_target).apply {

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