package com.daniebeler.nowandagain.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daniebeler.nowandagain.MainActivity
import com.daniebeler.nowandagain.R
import com.daniebeler.nowandagain.data.Task
import com.daniebeler.nowandagain.domain.dueLabel
import java.time.LocalDate

object Notifications {
    private const val CHANNEL_ID = "daily_reminder"
    private const val NOTIFICATION_ID = 1
    private const val MAX_LINES = 5

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminder",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "A morning summary of tasks that are due"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // areNotificationsEnabled() covers the permission
    fun postDue(context: Context, tasks: List<Task>, today: LocalDate) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        if (tasks.size == 1) {
            val task = tasks.first()
            builder.setContentTitle(task.name)
                .setContentText(task.dueLabel(today))
        } else {
            val inbox = NotificationCompat.InboxStyle()
            tasks.take(MAX_LINES).forEach { inbox.addLine("${it.name} · ${it.dueLabel(today)}") }
            if (tasks.size > MAX_LINES) inbox.setSummaryText("+${tasks.size - MAX_LINES} more")

            builder.setContentTitle("${tasks.size} tasks need you today")
                .setContentText(tasks.joinToString(", ") { it.name })
                .setStyle(inbox)
        }

        manager.notify(NOTIFICATION_ID, builder.build())
    }
}