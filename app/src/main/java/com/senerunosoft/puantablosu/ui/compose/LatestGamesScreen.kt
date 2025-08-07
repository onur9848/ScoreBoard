package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Surface(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = stringResource(id = R.string.add_player),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.eski_oyunlar),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Divider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Games list
            if (games.isEmpty()) {
                // Empty state
                EmptyGamesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Henüz oyun bulunmuyor",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "İlk oyununuzu oluşturmak için 'Yeni Oyun' butonuna tıklayın",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Game title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Games,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = game.gameTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Players count and rounds info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = "${game.playerList.size} oyuncu",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                    tonalElevation = 1.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Tour,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${game.score.size} tur",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 10.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                }
            }
            // Player names
            if (game.playerList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    game.playerList.take(5).forEach { player ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = player.name,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (game.playerList.size > 5) {
                        Text(
                            text = "+${game.playerList.size - 5}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
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