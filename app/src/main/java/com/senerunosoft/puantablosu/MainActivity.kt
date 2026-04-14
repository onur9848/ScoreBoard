package com.senerunosoft.puantablosu

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.senerunosoft.puantablosu.analytics.IAnalyticsService
import com.senerunosoft.puantablosu.ui.compose.ScoreBoardNavigation
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.viewmodel.GameViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Main activity for the ScoreBoard application.
 * Follows Dependency Inversion Principle (DIP) - uses injected ViewModel.
 */
class MainActivity : AppCompatActivity() {

    // Inject ViewModel using Koin
    private val gameViewModel: GameViewModel by viewModel()
    private val analyticsService: IAnalyticsService by inject()

    /** True after the first [onStop] has been called, indicating the app was backgrounded. */
    private var wasInBackground = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Telefon gibi küçük ekranlarda portreye kilitle
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        // Use Jetpack Compose navigation
        enableEdgeToEdge()
        setContent {
            ScoreBoardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScoreBoardNavigation(
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        wasInBackground = true
        analyticsService.trackAppBackground()
    }

    override fun onStart() {
        super.onStart()
        // Only send app_foreground when returning from background (not on initial launch,
        // since app_open is already sent from ScoreBoardApplication.onCreate).
        if (wasInBackground) {
            wasInBackground = false
            analyticsService.trackAppForeground()
        }
    }
}