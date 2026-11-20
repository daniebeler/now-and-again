package dev.ghostbyte.nowandagain.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemeMode { System, Light, Dark }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
)

class SettingsRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _settings.update { it.copy(themeMode = mode) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER, enabled).apply()
        _settings.update { it.copy(reminderEnabled = enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
        _settings.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }

    private fun load(): AppSettings {
        val theme = prefs.getString(KEY_THEME, null)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: when {
                // Old toggle value from before settings existed
                prefs.contains(LEGACY_DARK) ->
                    if (prefs.getBoolean(LEGACY_DARK, false)) ThemeMode.Dark else ThemeMode.Light
                else -> ThemeMode.System
            }
        return AppSettings(
            themeMode = theme,
            reminderEnabled = prefs.getBoolean(KEY_REMINDER, true),
            reminderHour = prefs.getInt(KEY_HOUR, 9),
            reminderMinute = prefs.getInt(KEY_MINUTE, 0),
        )
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_REMINDER = "reminder_enabled"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
        private const val LEGACY_DARK = "dark_theme"

        @Volatile private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
    }
}