package com.daniebeler.nowandagain.domain

import com.daniebeler.nowandagain.data.Task
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale

fun Task.dueDate(): LocalDate = lastDoneDate.plusDays(intervalDays.toLong())

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
/** Fraction of the interval already used (0f..1f+), for the progress indicator. */
fun Task.progress(today: LocalDate = LocalDate.now()): Float {
    val elapsed = ChronoUnit.DAYS.between(lastDoneDate, today)
    return elapsed.toFloat() / intervalDays
}

fun Task.markDone(today: LocalDate = LocalDate.now()) = copy(lastDoneDate = today)

fun statusText(daysLeft: Long): String = when {
    daysLeft < -1 -> "Overdue by ${-daysLeft} days"
    daysLeft == -1L -> "Overdue by 1 day"
    daysLeft == 0L -> "Due today"
    daysLeft == 1L -> "Due tomorrow"
    else -> "In $daysLeft days"
}