package com.example.aikataulu.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.RemoteViews
import com.example.aikataulu.DeparturesForStopIdQuery
import com.example.aikataulu.R

fun createDepartureContent(widgetId: Int, context: Context, departures: ArrayList<DeparturesForStopIdQuery.StoptimesWithoutPattern>) {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget)
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // Empty widget content
    remoteViews.removeAllViews(R.id.widgetContainer)

    // Create view content
    val view = inflater.inflate(R.layout.single_departure, null)

    // Update _only this_ widget with this remote view
    appWidgetManager.updateAppWidget(widgetId, remoteViews)
}