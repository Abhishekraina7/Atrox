package com.example.atrox.domain.repository

import com.example.atrox.data.local.db.TaskItem
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {
    val tasks: Flow<List<TaskItem>>
    fun getTasksForDate(date: String): Flow<List<TaskItem>>
    suspend fun saveTasks(tasks: List<TaskItem>)
    suspend fun insertTask(task: TaskItem)
    suspend fun updateTask(task: TaskItem)
    suspend fun deleteTaskById(taskId: String)
}
