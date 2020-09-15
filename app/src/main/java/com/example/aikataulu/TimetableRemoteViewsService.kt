package com.example.aikataulu

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

class TimetableRemoteViewsService : RemoteViewsService() {
    /**
     * [Invoked by [TimetableWidgetProvider]] when setting this as RemoteAdapter.
     */
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val widgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: 0
        Log.d(TAG, "[WidgetId=$widgetId] onGetViewFactory")
        return ViewFactory(widgetId, applicationContext)
    }

    class ViewFactory(
        private val _widgetId: Int,
        private val _context: Context
    ) : RemoteViewsFactory {
        private var _cursor: Cursor? = null

        /**
         * Moves the cursor to point to an updated data set.
         * This triggers the in-framework view handling.
         */
        override fun onDataSetChanged() {
            _cursor?.close()
            // Get all data for widgets that have out-of-date data
            _cursor = _context.contentResolver.query(
                TimetableDataProvider.TIMETABLE_DATA_URI,
                null,
                "${Timetable.WIDGET_ID} = ?",
                arrayOf(_widgetId.toString()),
                null
            )
        }

        /**
         * [Invoked by framework]
         * Called when the cursor is updated.
         * Constructs a view from an element of the cursor-pointed data collection.
         */
        override fun getViewAt(position: Int): RemoteViews? {
            if (position == AdapterView.INVALID_POSITION || _cursor?.moveToPosition(position) != true) {
                Log.w(TAG, "Invalid position in getViewAt()")
                return null
            }

            val cursor = _cursor!!
            return RemoteViews(_context.packageName, R.layout.single_departure).apply {
                // Route name
                setTextViewText(
                    R.id.sd_tvRouteName,
                    cursor.getString(cursor.getColumnIndex(Timetable.COLUMN_ROUTE_SHORT_NAME))
                )
                // Headsign
                setTextViewText(
                    R.id.sd_tvHeadsign,
                    cursor.getString(cursor.getColumnIndex(Timetable.COLUMN_HEADSIGN))
                )
                // Departure (scheduled)
                setTextViewText(
                    R.id.sd_tvDepartureScheduled,
                    cursor.getString(cursor.getColumnIndex(Timetable.COLUMN_DEPARTURE_SCHEDULED))
                )
                // Departure (actual)
                val isOnTime = "true".equals(
                    cursor.getString(cursor.getColumnIndex(Timetable.COLUMN_IS_ON_TIME)),
                    true
                )
                setTextViewText(
                    R.id.sd_tvDepartureRealtime,
                    if (isOnTime) "" else " (${cursor.getString(cursor.getColumnIndex(Timetable.COLUMN_DEPARTURE_REALTIME))})"
                )
            }
        }

        /** Perform cleanup: destroy cursor. */
        override fun onDestroy() {
            _cursor?.close()
        }

        /** Required framework method */
        override fun onCreate() {
            return
        }

        /** Required framework method */
        override fun getLoadingView(): RemoteViews? {
            return null
        }


        /** Required framework method */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /** Required framework method */
        override fun getCount(): Int {
            return _cursor?.count ?: 0
        }

        /** Required framework method */
        override fun getViewTypeCount(): Int {
            return 1
        }

        /** Required framework method */
        override fun hasStableIds(): Boolean {
            return true
        }
    }

    companion object {
        const val TAG = "TIMETABLE.RemoteViewsService"
    }
}