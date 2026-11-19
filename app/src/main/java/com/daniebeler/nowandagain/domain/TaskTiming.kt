package com.daniebeler.nowandagain.domain

import com.daniebeler.nowandagain.data.Task
import com.daniebeler.nowandagain.data.startDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale

fun Task.dueDate(): LocalDate = startDate.plusDays(intervalDays.toLong())

fun Task.daysLeft(today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(today, dueDate())   // negative = overdue

/** Compact label for space-limited places like the widget. */
fun Task.dueLabel(today: LocalDate = LocalDate.now(), locale: Locale = Locale.getDefault()): String {
    val left = daysLeft(today)
    val due = dueDate()
    return when {
        left < 0 -> statusText(left)                      // "Overdue by 2 days"
        left == 0L -> "Due today"
        left == 1L -> "Until tomorrow"
        left < 7 -> "Until ${due.dayOfWeek.getDisplayName(TextStyle.FULL, locale)}"
        else -> "Until ${due.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)}, " +
                "${due.dayOfMonth} ${due.month.getDisplayName(TextStyle.SHORT, locale)}"
    }
}

fun Task.progress(now: LocalDateTime = LocalDateTime.now()): Float {
    val daysSinceStart = ChronoUnit.DAYS.between(startDate, now.toLocalDate())
    val fractionOfToday = now.toLocalTime().toSecondOfDay() / 86_400f
    return ((daysSinceStart - 1 + fractionOfToday) / intervalDays).coerceAtLeast(0f)
}

fun Task.markDone(today: LocalDate = LocalDate.now()) = copy(lastDoneDate = today)

fun statusText(daysLeft: Long): String = when {
    daysLeft < -1 -> "Overdue by ${-daysLeft} days"
    daysLeft == -1L -> "Overdue by 1 day"
    daysLeft == 0L -> "Due today"
    daysLeft == 1L -> "Due tomorrow"
    else -> "In $daysLeft days"
}

enum class TaskStatus { DoneToday, Fine, DueToday, Overdue }

fun Task.status(today: LocalDate = LocalDate.now()): TaskStatus {
    val left = daysLeft(today)
    val done = lastDoneDate
    return when {
        done != null && !done.isBefore(today) -> TaskStatus.DoneToday
        left < 0 -> TaskStatus.Overdue
        left == 0L -> TaskStatus.DueToday
        else -> TaskStatus.Fine
    }
}

fun Task.intervalLabel(): String = when {
    intervalDays == 1 -> "Every day"
    intervalDays == 7 -> "At least every week"
    intervalDays % 7 == 0 && intervalDays <= 28 -> "At least every ${intervalDays / 7} weeks"
    else -> "At least every $intervalDays days"
}

fun Task.lastDoneLabel(today: LocalDate = LocalDate.now()): String {
    val done = lastDoneDate ?: return "Not done yet"
    return when (val ago = ChronoUnit.DAYS.between(done, today)) {
        in Long.MIN_VALUE..0L -> "Last done today"
        1L -> "Last done yesterday"
        else -> "Last done $ago days ago"
    }
}