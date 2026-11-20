package dev.ghostbyte.nowandagain.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dev.ghostbyte.nowandagain.data.SettingsRepository
import java.time.Duration
import java.time.ZonedDateTime

object ReminderScheduler {
    private const val WORK_NAME = "daily_reminder"

    /**
     * KEEP: app start, does nothing if a job is pending.
     * REPLACE: the user changed the time.
     * APPEND_OR_REPLACE: the worker schedules the next day.
     */
    fun schedule(context: Context, policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val settings = SettingsRepository.get(context).settings.value
        val now = ZonedDateTime.now()
        var next = now.toLocalDate()
            .atTime(settings.reminderHour, settings.reminderMinute)
            .atZone(now.zone)
        if (!next.isAfter(now)) next = next.plusDays(1)

        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(Duration.between(now, next))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
    }
}