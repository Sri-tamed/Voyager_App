package com.example.voyager.di

import android.content.Context
import com.example.voyager.emergency.EmergencyManager

/**
 * Simple Dependency Injection for Emergency Module
 * No Hilt/Dagger required - uses singleton pattern
 */
object EmergencyModule {

    @Volatile
    private var emergencyManager: EmergencyManager? = null

    /**
     * Get or create EmergencyManager instance
     * Thread-safe singleton
     */
    fun provideEmergencyManager(context: Context): EmergencyManager {
        return emergencyManager ?: synchronized(this) {
            emergencyManager ?: EmergencyManager.getInstance(context.applicationContext).also {
                emergencyManager = it
            }
        }
    }
}

/**
 * USAGE IN COMPOSABLES:
 *
 * @Composable
 * fun MyScreen() {
 *     val context = LocalContext.current
 *     val emergencyManager = remember {
 *         EmergencyModule.provideEmergencyManager(context)
 *     }
 *
 *     EmergencyScreen(
 *         emergencyManager = emergencyManager,
 *         onCancel = { }
 *     )
 * }
 */