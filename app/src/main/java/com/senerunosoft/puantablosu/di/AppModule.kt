package com.senerunosoft.puantablosu.di

import android.content.Context
import android.content.pm.PackageManager
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.analytics.AnalyticsService
import com.senerunosoft.puantablosu.analytics.IAnalyticsService
import com.senerunosoft.puantablosu.analytics.SessionManager
import com.senerunosoft.puantablosu.data.repository.GamesRepository
import com.senerunosoft.puantablosu.data.repository.GamesRepositoryImpl
import com.senerunosoft.puantablosu.data.source.GameDataSource
import com.senerunosoft.puantablosu.data.source.SharedPreferencesDataSource
import com.senerunosoft.puantablosu.service.impl.CompositeGameService
import com.senerunosoft.puantablosu.service.impl.GameManagerService
import com.senerunosoft.puantablosu.service.impl.JsonGameSerializerService
import com.senerunosoft.puantablosu.service.impl.PlayerManagerService
import com.senerunosoft.puantablosu.service.impl.ScoreCalculatorService
import com.senerunosoft.puantablosu.service.interfaces.IGameManager
import com.senerunosoft.puantablosu.service.interfaces.IGameSerializer
import com.senerunosoft.puantablosu.service.interfaces.IPlayerManager
import com.senerunosoft.puantablosu.service.interfaces.IScoreCalculator
import com.senerunosoft.puantablosu.strategy.IScoringStrategy
import com.senerunosoft.puantablosu.strategy.StandardScoringStrategy
import com.senerunosoft.puantablosu.viewmodel.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module for the ScoreBoard application.
 * Follows Dependency Inversion Principle (DIP) - configures abstractions and implementations.
 * Supports extensibility through Open/Closed Principle (OCP).
 */
val appModule = module {
    
    // Strategy layer - scoring strategies following Strategy Pattern
    single<IScoringStrategy> { StandardScoringStrategy() }
    
    // Data layer - Repository pattern for data abstraction
    single<GameDataSource> { 
        SharedPreferencesDataSource(
            context = androidContext(),
            gameService = get()
        ) 
    }
    
    single<GamesRepository> { 
        GamesRepositoryImpl(dataSource = get()) 
    }
    
    // Service layer - focused services following Single Responsibility Principle
    single<IGameManager> { GameManagerService() }
    single<IPlayerManager> { PlayerManagerService() }
    single<IScoreCalculator> { ScoreCalculatorService(scoringStrategy = get()) }
    single<IGameSerializer> { JsonGameSerializerService() }
    
    // Composite service for backward compatibility
    single<IGameService> {
        CompositeGameService(
            gameManager = get(),
            playerManager = get(),
            scoreCalculator = get(),
            gameSerializer = get()
        )
    }
    
    // Analytics layer
    single { SessionManager(context = androidContext()) }
    single<IAnalyticsService> {
        val ctx: Context = androidContext()
        val appVersion = try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"
        } catch (_: PackageManager.NameNotFoundException) {
            "unknown"
        }
        AnalyticsService(
            context = ctx,
            sessionManager = get(),
            appVersion = appVersion
        )
    }
    
    // ViewModel layer
    viewModel { 
        GameViewModel(
            gameService = get(),
            gamesRepository = get()
        ) 
    }
}