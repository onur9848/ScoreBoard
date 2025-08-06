package com.senerunosoft.puantablosu

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.senerunosoft.puantablosu.databinding.ActivityMainBinding
import com.senerunosoft.puantablosu.ui.compose.ScoreBoardNavigation
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameViewModel: GameViewModel

    // Feature flag to enable Compose migration
    private val useCompose = true // Set to false to use original Fragment navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameViewModel = ViewModelProvider(this)[GameViewModel::class.java]

        if (useCompose) {
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
        } else {
            // Use original Fragment navigation
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        }
    }
}