package dev.ghostbyte.nowandagain.data

import android.content.Context

class HintPreference(context: Context) {
    private val prefs = context.getSharedPreferences("hints", Context.MODE_PRIVATE)

    val widgetHintDismissed: Boolean
        get() = prefs.getBoolean(KEY_WIDGET, false)

    fun dismissWidgetHint() = prefs.edit().putBoolean(KEY_WIDGET, true).apply()

    private companion object { const val KEY_WIDGET = "widget_hint_dismissed" }
}