package dev.ghostbyte.nowandagain.domain

import dev.ghostbyte.nowandagain.data.startDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun dev.ghostbyte.nowandagain.data.Task.dueDate(): LocalDate = startDate.plusDays(intervalDays.toLong())

fun dev.ghostbyte.nowandagain.data.Task.daysLeft(today: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(today, dueDate())   // negative = overdue


fun dev.ghostbyte.nowandagain.data.Task.progress(now: LocalDateTime = LocalDateTime.now()): Float {
    val daysSinceStart = ChronoUnit.DAYS.between(startDate, now.toLocalDate())
    val fractionOfToday = now.toLocalTime().toSecondOfDay() / 86_400f
    return ((daysSinceStart - 1 + fractionOfToday) / intervalDays).coerceAtLeast(0f)
}

fun dev.ghostbyte.nowandagain.data.Task.markDone(today: LocalDate = LocalDate.now()) = copy(lastDoneDate = today)

enum class TaskStatus { DoneToday, Fine, DueToday, Overdue }

fun dev.ghostbyte.nowandagain.data.Task.status(today: LocalDate = LocalDate.now()): TaskStatus {
    val left = daysLeft(today)
    val done = lastDoneDate
    return when {
        done != null && !done.isBefore(today) -> TaskStatus.DoneToday
        left < 0 -> TaskStatus.Overdue
        left == 0L -> TaskStatus.DueToday
        else -> TaskStatus.Fine
    }
}