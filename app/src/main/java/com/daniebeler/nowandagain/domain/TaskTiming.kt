package com.daniebeler.nowandagain.domain

import com.daniebeler.nowandagain.data.Task
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun Task.dueDate(): LocalDate = lastDoneDate.plusDays(intervalDays.toLong())

fun Task.daysLeft(today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(today, dueDate())   // negative = overdue

fun Task.isOverdue(today: LocalDate = LocalDate.now()) = daysLeft(today) < 0

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