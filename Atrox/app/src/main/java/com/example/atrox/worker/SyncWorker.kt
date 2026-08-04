package com.example.atrox.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.atrox.domain.sync.CloudSyncManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cloudSyncManager: CloudSyncManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "doWork: Triggering guaranteed background push")
        return try {
            cloudSyncManager.syncPushOnly()
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "doWork: Background push failed", e)
            Result.retry()
        }
    }
}
