package com.example.voyager


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoyagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components here
    }
}