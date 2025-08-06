package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.R

/**
 * Home screen Composable - demonstrates Jetpack Compose migration
 * This is a proof of concept for replacing HomeFragment with Compose
 */
@Composable
fun HomeScreen(
    onNewGameClick: () -> Unit = {},
    onOldGameClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // New Game Button
        Button(
            onClick = onNewGameClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Yeni Oyun") // New Game
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Old Game Button
        Button(
            onClick = onOldGameClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Geçmiş Oyunlar") // Previous Games
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}