package com.example.voyager.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Service for handling emergency alarm functionality
 * Manages SOS alerts and emergency notifications
 *
 * This service explicitly extends android.app.Service and provides
 * a default constructor as required by the Android framework.
 */
class EmergencyAlarmService : Service() {

    companion object {
        private const val TAG = "EmergencyAlarmService"
    }

    /**
     * Default constructor - automatically provided by Kotlin
     * but documented here for clarity regarding manifest requirements
     */
    init {
        Log.d(TAG, "EmergencyAlarmService instance created")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "EmergencyAlarmService onCreate()")
        // Initialize emergency alarm components
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "EmergencyAlarmService onStartCommand()")

        // TODO: Handle emergency alarm logic
        // - Trigger SOS alerts
        // - Send SMS to emergency contacts (if permission granted)
        // - Make emergency calls
        // - Show emergency notification

        when (intent?.action) {
            ACTION_TRIGGER_SOS -> {
                // Handle SOS trigger
                triggerEmergencyAlert()
            }
            ACTION_CANCEL_SOS -> {
                // Cancel ongoing emergency alert
                cancelEmergencyAlert()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This service doesn't support binding
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "EmergencyAlarmService onDestroy()")
        // Clean up resources
    }

    private fun triggerEmergencyAlert() {
        // TODO: Implement emergency alert logic
        Log.d(TAG, "Triggering emergency alert")
    }

    private fun cancelEmergencyAlert() {
        // TODO: Implement cancel logic
        Log.d(TAG, "Canceling emergency alert")
    }

    companion object {
        const val ACTION_TRIGGER_SOS = "com.example.voyager.ACTION_TRIGGER_SOS"
        const val ACTION_CANCEL_SOS = "com.example.voyager.ACTION_CANCEL_SOS"
    }
}