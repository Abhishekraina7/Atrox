package com.example.atrox.di

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * The mandatory Application class for Hilt Dependency Injection.
 * Without this class declared in the Manifest, any @AndroidEntryPoint (like our MainActivity)
 * will cause the app to crash immediately upon launch.
 */
@HiltAndroidApp
class AtroxHiltApplication : Application(), Configuration.Provider {
    
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        com.example.atrox.service.notification.NotificationHelper.createNotificationChannels(this)
    }
}