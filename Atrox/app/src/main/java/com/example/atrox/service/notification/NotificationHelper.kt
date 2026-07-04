package com.example.atrox.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.atrox.MainActivity
import com.example.atrox.R

object NotificationHelper {

    const val CHANNEL_ID_GOALS = "atrox_goal_reminders"
    const val CHANNEL_ID_INSIGHTS = "atrox_insights"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val goalChannel = NotificationChannel(
                CHANNEL_ID_GOALS,
                "Goal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to hit your daily focus goals and protect your streak."
            }

            val insightChannel = NotificationChannel(
                CHANNEL_ID_INSIGHTS,
                "Analytics & Insights",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly reports and general insights."
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(goalChannel)
            notificationManager.createNotificationChannel(insightChannel)
        }
    }

    fun sendNotification(context: Context, notificationId: Int, title: String, message: String, channelId: String = CHANNEL_ID_GOALS) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Replace with a transparent status bar icon later if available
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (channelId == CHANNEL_ID_GOALS) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle missing POST_NOTIFICATIONS permission
            }
        }
    }
}
