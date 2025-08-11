package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.ui.compose.components.GenericButton
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Home screen Composable - demonstrates Jetpack Compose migration
 * This is a proof of concept for replacing HomeFragment with Compose
 */
@Composable
fun HomeScreen(
    onNewGameClick: () -> Unit = {},
    onOldGameClick: () -> Unit = {},
    onScoreCalculatorClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 100.dp), // Add margin bottom for modern spacing
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Add game.png image at the top
        Image(
            painter = painterResource(id = R.drawable.game),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 40.dp)
        )


        // use GenericButton for New Game and Old Game buttons
        GenericButton(
            text = "Yeni Oyun",
            value = Unit,
            onClick = { onNewGameClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        GenericButton(
            text = "Geçmiş Oyunlar",
            value = Unit,
            onClick = { onOldGameClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Puan Hesapla Section (enabled, modern look)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Puan Hesapla",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                GenericButton(
                    text = "Hesapla",
                    value = Unit,
                    onClick = { onScoreCalculatorClick() },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Çok Yakında",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ScoreBoardTheme {
        HomeScreen()
    }
}