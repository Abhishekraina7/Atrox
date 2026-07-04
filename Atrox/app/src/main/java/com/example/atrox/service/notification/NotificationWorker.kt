package com.example.atrox.service.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val decisionEngine: NotificationDecisionEngine
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val state = decisionEngine.evaluateScenario()
        
        if (state is NotificationState.ShouldSend) {
            NotificationHelper.sendNotification(
                context = context,
                notificationId = state.scenarioId,
                title = state.title,
                message = state.message,
                channelId = state.channelId
            )
        }
        
        return Result.success()
    }
}
