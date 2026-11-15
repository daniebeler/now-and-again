package com.daniebeler.nowandagain.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    val tasks: Flow<List<Task>> = dao.observeActive()

    suspend fun add(name: String, intervalDays: Int) {
        dao.insert(Task(name = name.trim(), intervalDays = intervalDays))
    }

    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun restore(task: Task) = dao.insert(task)
}