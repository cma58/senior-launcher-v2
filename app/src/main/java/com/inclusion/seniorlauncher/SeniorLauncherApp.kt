package com.inclusion.seniorlauncher

import android.app.Application

/**
 * Application entry point. Kept minimal — add dependency-injection setup
 * (Hilt / Koin) and analytics initialization here when wiring in the
 * domain/data modules.
 */
class SeniorLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: init DI container, crash reporting (privacy-first only), etc.
    }
}
