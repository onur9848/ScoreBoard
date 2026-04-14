package com.senerunosoft.puantablosu

import android.app.Application
import com.senerunosoft.puantablosu.analytics.IAnalyticsService
import com.senerunosoft.puantablosu.analytics.SessionManager
import com.senerunosoft.puantablosu.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class that initializes dependency injection.
 * Follows Dependency Inversion Principle (DIP) - sets up the DI container.
 */
class ScoreBoardApplication : Application() {
    
    private val sessionManager: SessionManager by inject()
    private val analyticsService: IAnalyticsService by inject()

    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@ScoreBoardApplication)
            modules(appModule)
        }

        // Start a fresh session and fire app_open on every cold start
        sessionManager.startNewSession()
        analyticsService.trackAppOpen()
    }
}