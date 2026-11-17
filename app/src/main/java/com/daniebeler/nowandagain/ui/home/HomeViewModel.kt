package com.daniebeler.nowandagain.ui.home

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daniebeler.nowandagain.data.AppDatabase
import com.daniebeler.nowandagain.data.Task
import com.daniebeler.nowandagain.data.TaskRepository
import com.daniebeler.nowandagain.domain.TaskStatus
import com.daniebeler.nowandagain.domain.daysLeft
import com.daniebeler.nowandagain.domain.markDone
import com.daniebeler.nowandagain.domain.progress
import com.daniebeler.nowandagain.domain.status
import com.daniebeler.nowandagain.widget.NextTasksWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

import java.time.Duration
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

data class TaskItem(
    val task: Task,
    val daysLeft: Long,
    val progress: Float,
    val status: TaskStatus,
)

data class HomeUiState(
    val items: List<TaskItem> = emptyList(),
    val loading: Boolean = true,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TaskRepository(AppDatabase.get(app).taskDao())

    // Emits today's date, and again right after every midnight,
    // so overdue states refresh while the app is open.
    private val today: Flow<LocalDate> = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now.toLocalDate())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            delay((Duration.between(now, nextMidnight).toMillis() + 1_000).milliseconds)
        }
    }

    private val now: Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(60_000.milliseconds)
        }
    }

    val state: StateFlow<HomeUiState> =
        combine(repo.tasks, now) { tasks, now ->
            val day = now.toLocalDate()
            val items = tasks
                .map { TaskItem(it, it.daysLeft(day), it.progress(now), it.status(day)) }
                .sortedWith(compareBy({ it.daysLeft }, { -it.progress }))
            HomeUiState(items = items, loading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            repo.tasks.collect {
                NextTasksWidget().updateAll(getApplication<Application>())
            }
        }
    }

    fun addTask(name: String, intervalDays: Int) {
        viewModelScope.launch { repo.add(name, intervalDays) }
    }

    fun markDone(task: Task) {
        viewModelScope.launch { repo.update(task.markDone()) }
    }

    /** Undo = write back the task exactly as it was before markDone. */
    fun restore(previous: Task) {
        viewModelScope.launch { repo.update(previous) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repo.delete(task) }
    }

    fun editTask(task: Task, name: String, intervalDays: Int) {
        viewModelScope.launch {
            repo.update(task.copy(name = name.trim(), intervalDays = intervalDays))
        }
    }

    /** Undo for delete: re-insert the task with its old id and data. */
    fun undoDelete(task: Task) {
        viewModelScope.launch { repo.restore(task) }
    }
}