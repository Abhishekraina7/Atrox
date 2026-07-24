package com.example.atrox.data.repository

import com.example.atrox.data.local.db.TaskDao
import com.example.atrox.data.local.db.TaskItem
import com.example.atrox.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) : ITaskRepository {
    override val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    
    override fun getTasksForDate(date: String): Flow<List<TaskItem>> {
        return taskDao.getTasksForDate(date)
    }

    override suspend fun saveTasks(tasks: List<TaskItem>) {
        taskDao.insertTasks(tasks)
    }
    
    override suspend fun insertTask(task: TaskItem) {
        taskDao.insertTask(task)
    }
    
    override suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
    }
    
    override suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }
}
