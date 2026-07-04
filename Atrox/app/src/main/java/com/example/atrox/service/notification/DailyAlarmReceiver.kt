package com.example.atrox.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // When the alarm fires, we simply kick off a OneTimeWorkRequest
        // The WorkManager handles the Coroutine Scope and dependency injection nicely.
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        // Reschedule alarms for the next day
        NotificationScheduler.scheduleExactAlarms(context)
    }
}
