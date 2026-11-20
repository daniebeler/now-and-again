package dev.ghostbyte.nowandagain.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetPinner {
    private fun provider(context: Context) =
        ComponentName(context, NextTasksReceiver::class.java)

    private fun manager(context: Context) = AppWidgetManager.getInstance(context)

    fun hasWidget(context: Context): Boolean =
        manager(context).getAppWidgetIds(provider(context)).isNotEmpty()

    /** Some launchers don't support pinning from inside an app. */
    fun canPin(context: Context): Boolean = manager(context).isRequestPinAppWidgetSupported

    fun requestPin(context: Context) {
        manager(context).requestPinAppWidget(provider(context), null, null)
    }
}