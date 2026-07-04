package com.example.atrox.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationScheduler.schedulePeriodicChecks(context)
            NotificationScheduler.scheduleExactAlarms(context)
        }
    }
}
