package com.example.atrox.data.repository

import com.example.atrox.data.local.db.TaskDao
import com.example.atrox.data.local.db.TaskItem
import com.example.atrox.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val firebaseAuth: Lazy<FirebaseAuth>
) : ITaskRepository {
    override val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    
    override fun getTasksForDate(date: String): Flow<List<TaskItem>> {
        return taskDao.getTasksForDate(date)
    }

    override suspend fun saveTasks(tasks: List<TaskItem>) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedTasks = tasks.map { 
            it.copy(userId = uid, updatedAt = System.currentTimeMillis(), isSynced = false) 
        }
        taskDao.insertTasks(updatedTasks)
    }
    
    override suspend fun insertTask(task: TaskItem) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedTask = task.copy(
            userId = uid, 
            updatedAt = System.currentTimeMillis(), 
            isSynced = false
        )
        taskDao.insertTask(updatedTask)
    }
    
    override suspend fun updateTask(task: TaskItem) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedTask = task.copy(
            userId = uid, 
            updatedAt = System.currentTimeMillis(), 
            isSynced = false
        )
        taskDao.updateTask(updatedTask)
    }
    
    override suspend fun deleteTaskById(taskId: String) {
        // Hard deletes won't sync offline well without a tombstone,
        // but for now we execute the local delete.
        taskDao.deleteTaskById(taskId)
    }
}
