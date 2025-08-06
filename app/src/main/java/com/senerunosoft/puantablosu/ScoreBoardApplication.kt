package com.senerunosoft.puantablosu

import android.app.Application
import com.senerunosoft.puantablosu.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class that initializes dependency injection.
 * Follows Dependency Inversion Principle (DIP) - sets up the DI container.
 */
class ScoreBoardApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@ScoreBoardApplication)
            modules(appModule)
        }
    }
}