package com.example.atrox.data.tasks

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    
    fun getTasksForDate(date: String): Flow<List<TaskItem>> {
        return taskDao.getTasksForDate(date)
    }

    suspend fun saveTasks(tasks: List<TaskItem>) {
        taskDao.insertTasks(tasks)
    }
    
    suspend fun insertTask(task: TaskItem) {
        taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
    }
    
    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }
}
