package com.example.atrox.service.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "atrox_periodic_notifications"

    fun schedulePeriodicChecks(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            3, TimeUnit.HOURS, // Run every 3 hours
            1, TimeUnit.HOURS  // Flex interval
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    
    fun cancelPeriodicChecks(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun scheduleExactAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        // We need permission for exact alarms on Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return // Can't schedule without permission, handle gracefully
            }
        }

        // Schedule Morning Kickoff at 9:00 AM
        scheduleAlarmForTime(context, alarmManager, 9, 0, 1001)

        // Schedule Streak Protection at 20:00 (8:00 PM)
        scheduleAlarmForTime(context, alarmManager, 20, 0, 1002)
    }

    private fun scheduleAlarmForTime(
        context: Context,
        alarmManager: android.app.AlarmManager,
        hour: Int,
        minute: Int,
        requestCode: Int
    ) {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }

        // If time has passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        val intent = android.content.Intent(context, DailyAlarmReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Ignored, handled by canScheduleExactAlarms check
        }
    }
}
