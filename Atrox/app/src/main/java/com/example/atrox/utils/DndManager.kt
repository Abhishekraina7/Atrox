package com.example.atrox.utils

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized Do Not Disturb manager.
 * Extracted from duplicated logic in DashboardViewModel, FocusSessionViewModel, and FocusBreakViewModel.
 */
@Singleton
class DndManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var dndActivated = false
    private var originalInterruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL

    fun activateDnd() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            originalInterruptionFilter = notificationManager.currentInterruptionFilter
            val policy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_CALLS,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY
            )
            notificationManager.setNotificationPolicy(policy)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            dndActivated = true
        }
    }

    fun restoreDnd() {
        if (dndActivated) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(originalInterruptionFilter)
            }
            dndActivated = false
        }
    }

    fun isActivated(): Boolean = dndActivated
}
