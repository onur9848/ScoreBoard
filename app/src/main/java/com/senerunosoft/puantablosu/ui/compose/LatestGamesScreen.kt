package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Latest Games Screen Composable
 * Shows a list of previously played games
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestGamesScreen(
    games: List<Game> = emptyList(),
    onGameSelected: (Game) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Background color matching original teal_700
    val backgroundColor = Color(0xFF00796B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.eski_oyunlar),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // Games list
        if (games.isEmpty()) {
            // Empty state
            EmptyGamesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(games) { game ->
                    GameCard(
                        game = game,
                        onClick = { onGameSelected(game) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGamesState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Henüz oyun bulunmuyor",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "İlk oyununuzu oluşturmak için 'Yeni Oyun' butonuna tıklayın",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Game title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = game.gameTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Players count and rounds info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${game.playerList.size} oyuncu",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                
                Text(
                    text = "${game.score.size} tur",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            
            // Player names
            if (game.playerList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Oyuncular: ${game.playerList.joinToString(", ") { it.name }}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LatestGamesScreenPreview() {
    ScoreBoardTheme {
        val sampleGames = listOf(
            Game(
                gameId = "1",
                gameTitle = "Test Oyunu 1",
                playerList = listOf(
                    Player("1", "Ahmet"),
                    Player("2", "Mehmet"),
                    Player("3", "Ayşe")
                ),
                score = mutableListOf()
            ),
            Game(
                gameId = "2",
                gameTitle = "Test Oyunu 2",
                playerList = listOf(
                    Player("4", "Fatma"),
                    Player("5", "Ali")
                ),
                score = mutableListOf()
            )
        )
        LatestGamesScreen(games = sampleGames)
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyLatestGamesScreenPreview() {
    ScoreBoardTheme {
        LatestGamesScreen(games = emptyList())
    }
}