package com.example.atrox.utils

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar

object DeviceUsageTracker {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Returns the phone usage time minus the Atrox app usage time for today, in minutes.
     */
    fun getPhoneUsageMinutesToday(context: Context): Int {
        if (!hasUsageStatsPermission(context)) return 0

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalTimeMillis = 0L
        var atroxTimeMillis = 0L
        val atroxPackageName = context.packageName

        if (stats != null) {
            for (usageStats in stats) {
                // To avoid massive over-counting from background system processes, 
                // we only count packages that have some foreground time.
                if (usageStats.totalTimeInForeground > 0) {
                    if (usageStats.packageName == atroxPackageName) {
                        atroxTimeMillis += usageStats.totalTimeInForeground
                    } else {
                        totalTimeMillis += usageStats.totalTimeInForeground
                    }
                }
            }
        }

        // Return only the non-Atrox phone usage time in minutes
        return (totalTimeMillis / (1000 * 60)).toInt()
    }
}
