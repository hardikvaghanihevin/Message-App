package com.hardik.messageapp

import android.app.Application
import com.hardik.messageapp.util.AppTheme
import com.hardik.messageapp.util.SwipeAction
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){

    override fun onCreate() {
        super.onCreate()

        loadPreferencesAndInitializeApp()
    }

    /** Load preferences, apply the theme, and configure locale */
    private fun loadPreferencesAndInitializeApp() {
        AppTheme.init(applicationContext) // Apply saved theme when app starts
        SwipeAction.init(applicationContext) // Initialize (usually in Application class)

    }
}
