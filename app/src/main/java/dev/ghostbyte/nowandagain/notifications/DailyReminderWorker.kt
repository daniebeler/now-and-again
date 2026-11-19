package dev.ghostbyte.nowandagain.notifications

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.ghostbyte.nowandagain.domain.daysLeft
import dev.ghostbyte.nowandagain.domain.status
import dev.ghostbyte.nowandagain.widget.NextTasksWidget
import java.time.LocalDate

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            val due = dev.ghostbyte.nowandagain.data.AppDatabase.get(applicationContext).taskDao().getActive()
                .filter {
                    val s = it.status(today)
                    s == dev.ghostbyte.nowandagain.domain.TaskStatus.DueToday || s == dev.ghostbyte.nowandagain.domain.TaskStatus.Overdue
                }
                .sortedBy { it.daysLeft(today) }

            if (due.isNotEmpty()) Notifications.postDue(applicationContext, due, today)

            // Also fixes stale "Done today" and day counts in the widget.
            NextTasksWidget().updateAll(applicationContext)
        } finally {
            ReminderScheduler.schedule(applicationContext, afterRun = true)
        }
        return Result.success()
    }
}