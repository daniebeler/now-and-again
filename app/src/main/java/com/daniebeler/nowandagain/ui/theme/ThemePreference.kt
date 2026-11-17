package com.daniebeler.nowandagain.ui.theme

import android.content.Context

class ThemePreference(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** null = follow the system */
    fun load(): Boolean? =
        if (prefs.contains(KEY)) prefs.getBoolean(KEY, false) else null

    fun save(dark: Boolean) = prefs.edit().putBoolean(KEY, dark).apply()

    private companion object { const val KEY = "dark_theme" }
}