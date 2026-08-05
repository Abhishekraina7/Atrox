package com.example.atrox.data.repository

import com.example.atrox.data.local.db.TaskDao
import com.example.atrox.data.local.db.TaskItem
import com.example.atrox.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atrox.domain.sync.CloudSyncManager
import com.example.atrox.worker.SyncWorker
import com.example.atrox.data.local.db.DeletedItemDao
import com.example.atrox.data.local.db.DeletedItemEntity
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val deletedItemDao: DeletedItemDao,
    private val firebaseAuth: Lazy<FirebaseAuth>,
    @ApplicationContext private val context: Context,
    private val cloudSyncManager: CloudSyncManager
) : ITaskRepository {
    override val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    
    override fun getTasksForDate(date: String): Flow<List<TaskItem>> {
        return taskDao.getTasksForDate(date)
    }

    private fun triggerSync() {
        cloudSyncManager.syncPushOnlyAsync()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override suspend fun saveTasks(tasks: List<TaskItem>) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedTasks = tasks.map { 
            it.copy(userId = uid, updatedAt = System.currentTimeMillis(), isSynced = false) 
        }
        taskDao.insertTasks(updatedTasks)
        triggerSync()
    }
    
    override suspend fun insertTask(task: TaskItem) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedTask = task.copy(
            userId = uid, 
            updatedAt = System.currentTimeMillis(), 
            isSynced = false
        )
        taskDao.insertTask(updatedTask)
        triggerSync()
    }
    
    override suspend fun updateTask(task: TaskItem) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedTask = task.copy(
            userId = uid, 
            updatedAt = System.currentTimeMillis(), 
            isSynced = false
        )
        taskDao.updateTask(updatedTask)
        triggerSync()
    }
    
    override suspend fun deleteTaskById(taskId: String) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        taskDao.deleteTaskById(taskId)
        if (uid.isNotEmpty()) {
            deletedItemDao.insertTombstone(DeletedItemEntity(taskId, "TASK", uid))
            triggerSync()
        }
    }
}
