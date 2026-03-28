package com.example.atrox

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The mandatory Application class for Hilt Dependency Injection.
 * Without this class declared in the Manifest, any @AndroidEntryPoint (like our MainActivity)
 * will cause the app to crash immediately upon launch.
 */
@HiltAndroidApp
class AtroxApplication : Application(){
    
}
