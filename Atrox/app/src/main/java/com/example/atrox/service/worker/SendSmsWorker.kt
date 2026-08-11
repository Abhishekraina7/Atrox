package com.example.atrox.service.worker

import android.content.Context
import android.telephony.SmsManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendSmsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val phoneNumber = inputData.getString(KEY_PHONE_NUMBER)
        val message = inputData.getString(KEY_MESSAGE)

        if (phoneNumber.isNullOrBlank() || message.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        try {
            val subscriptionManager = applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager
            var sent = false
            
            if (subscriptionManager != null) {
                try {
                    val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
                    if (!activeSubscriptions.isNullOrEmpty()) {
                        for (subInfo in activeSubscriptions) {
                            try {
                                val smsManager = SmsManager.getSmsManagerForSubscriptionId(subInfo.subscriptionId)
                                val parts = smsManager.divideMessage(message)
                                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                                sent = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    // Fallback to default if READ_PHONE_STATE is missing
                }
            }
            
            if (!sent) {
                val smsManager = SmsManager.getDefault()
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val KEY_PHONE_NUMBER = "KEY_PHONE_NUMBER"
        const val KEY_MESSAGE = "KEY_MESSAGE"
    }
}
