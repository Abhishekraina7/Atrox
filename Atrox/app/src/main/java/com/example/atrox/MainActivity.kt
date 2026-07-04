package com.example.atrox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.atrox.ui.theme.AtroxTheme
import dagger.hilt.android.AndroidEntryPoint

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    com.example.atrox.service.notification.NotificationScheduler.schedulePeriodicChecks(this@MainActivity)
                    com.example.atrox.service.notification.NotificationScheduler.scheduleExactAlarms(this@MainActivity)
                }
            }
            
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    com.example.atrox.service.notification.NotificationScheduler.schedulePeriodicChecks(this@MainActivity)
                    com.example.atrox.service.notification.NotificationScheduler.scheduleExactAlarms(this@MainActivity)
                }
            }

            AtroxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AtroxApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
