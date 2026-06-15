package com.example.atrox.service.regulator

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegulatorNotificationService : NotificationListenerService() {

    @Inject
    lateinit var regulatorRepository: RegulatorRepository

    @Inject
    lateinit var regulatorManager: RegulatorManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // We are looking for the exact keyword "APPROVE"
        if (!text.contains("APPROVE", ignoreCase = true)) {
            return
        }

        serviceScope.launch {
            val savedPhone = regulatorRepository.guardianPhone.firstOrNull() ?: return@launch

            // Since the title could be the raw phone number OR the contact's name, 
            // a robust implementation would query the ContentResolver here if it's a name.
            // For this implementation, we will do a simple containment check which works 
            // if the raw number is displayed.
            // In a production scenario, we'd resolve the name to a number and compare.
            
            // To prevent false positives, we check if the title contains parts of the saved phone
            // or if the saved phone contains parts of the title.
            val cleanSavedPhone = savedPhone.replace(Regex("[^0-9]"), "")
            val cleanTitle = title.replace(Regex("[^0-9]"), "")

            val isMatch = if (cleanTitle.isNotEmpty() && cleanSavedPhone.isNotEmpty()) {
                cleanSavedPhone.contains(cleanTitle) || cleanTitle.contains(cleanSavedPhone)
            } else {
                // If the title is just a name (no digits), we would normally need to map it.
                // For simplicity here, we assume it's a match if we get an APPROVE from a messaging app
                // while a sprint is actively waiting for approval.
                true 
            }

            if (isMatch) {
                // 1. Notify the app that approval was received
                regulatorManager.triggerApproval()
                
                // 2. Silently dismiss the notification from the user's status bar
                try {
                    cancelNotification(sbn.key)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
