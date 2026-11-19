package dev.ghostbyte.nowandagain.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime

object ReminderScheduler {
    private const val WORK_NAME = "daily_reminder"
    private const val HOUR = 9

    /** Schedules the next 9:00. KEEP on app start, APPEND_OR_REPLACE from the worker. */
    fun schedule(context: Context, afterRun: Boolean = false) {
        val now = ZonedDateTime.now()
        var next = now.toLocalDate().atTime(HOUR, 0).atZone(now.zone)
        if (!next.isAfter(now)) next = next.plusDays(1)

        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(Duration.between(now, next))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            if (afterRun) ExistingWorkPolicy.APPEND_OR_REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }
}