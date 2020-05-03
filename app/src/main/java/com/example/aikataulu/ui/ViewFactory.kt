package com.example.aikataulu.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.aikataulu.TimetableWidgetProvider
import com.example.aikataulu.models.Departure
import com.google.gson.Gson

class ViewFactory(val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var departures: ArrayList<Departure>

    override fun onCreate() {
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val departuresJson = intent.getStringExtra(TimetableWidgetProvider.EXTRA_DEPARTURES)
        departures = Gson().fromJson(departuresJson, TimetableWidgetProvider.departuresJsonType)
    }

    override fun getLoadingView(): RemoteViews {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun getItemId(position: Int): Long {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun onDataSetChanged() {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun hasStableIds(): Boolean {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun getViewAt(position: Int): RemoteViews {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun getViewTypeCount(): Int {
        val i = 0
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        val i = 0
        TODO("Not yet implemented")
    }
}