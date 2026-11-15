package com.daniebeler.nowandagain.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val intervalDays: Int,
    val lastDoneDate: LocalDate = LocalDate.now(),
    val archived: Boolean = false,
)