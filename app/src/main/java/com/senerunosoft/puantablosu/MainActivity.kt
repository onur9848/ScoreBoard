package com.senerunosoft.puantablosu

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.senerunosoft.puantablosu.ui.compose.ScoreBoardNavigation
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Main activity for the ScoreBoard application.
 * Follows Dependency Inversion Principle (DIP) - uses injected ViewModel.
 */
class MainActivity : AppCompatActivity() {

    // Inject ViewModel using Koin
    private val gameViewModel: GameViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
}