package com.daniebeler.nowandagain.notifications

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daniebeler.nowandagain.data.AppDatabase
import com.daniebeler.nowandagain.domain.TaskStatus
import com.daniebeler.nowandagain.domain.daysLeft
import com.daniebeler.nowandagain.domain.status
import com.daniebeler.nowandagain.widget.NextTasksWidget
import java.time.LocalDate

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            val due = AppDatabase.get(applicationContext).taskDao().getActive()
                .filter {
                    val s = it.status(today)
                    s == TaskStatus.DueToday || s == TaskStatus.Overdue
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