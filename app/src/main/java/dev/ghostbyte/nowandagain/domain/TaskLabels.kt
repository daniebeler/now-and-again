package dev.ghostbyte.nowandagain.domain

import android.text.format.DateFormat
import dev.ghostbyte.nowandagain.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit

import android.content.res.Resources

fun dev.ghostbyte.nowandagain.data.Task.dueLabel(resources: Resources, today: LocalDate = LocalDate.now()): String {
    val left = daysLeft(today)
    val due = dueDate()
    val locale = resources.configuration.locales[0]
    return when {
        left == -1L -> resources.getString(R.string.overdue_one_day)
        left < -1 -> resources.getString(R.string.overdue_days, -left)
        left == 0L -> resources.getString(R.string.due_today)
        left == 1L -> resources.getString(R.string.until_tomorrow)
        left < 7 -> resources.getString(
            R.string.until_date,
            due.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
        )
        else -> {
            val pattern = DateFormat.getBestDateTimePattern(locale, "EEEdMMM")
            resources.getString(
                R.string.until_date,
                due.format(DateTimeFormatter.ofPattern(pattern, locale)),
            )
        }
    }
}

fun dev.ghostbyte.nowandagain.data.Task.intervalLabel(resources: Resources): String = when {
    intervalDays == 1 -> resources.getString(R.string.interval_every_day)
    intervalDays == 7 -> resources.getString(R.string.interval_every_week)
    intervalDays % 7 == 0 && intervalDays <= 28 ->
        resources.getString(R.string.interval_every_weeks, intervalDays / 7)
    else -> resources.getString(R.string.interval_every_days, intervalDays)
}

fun dev.ghostbyte.nowandagain.data.Task.lastDoneLabel(resources: Resources, today: LocalDate = LocalDate.now()): String {
    val done = lastDoneDate ?: return resources.getString(R.string.last_done_never)
    val ago = ChronoUnit.DAYS.between(done, today)
    return when {
        ago <= 0L -> resources.getString(R.string.last_done_today)
        ago == 1L -> resources.getString(R.string.last_done_yesterday)
        else -> resources.getString(R.string.last_done_days_ago, ago)
    }
}