package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player

@Composable
fun ScoreBoardScreen(
    game: Game,
    modifier: Modifier = Modifier
) {
    val standings = game.playerList.map { player ->
        PlayerStanding(
            player = player,
            totalScore = game.score.sumOf { round -> round.scoreMap[player.id] ?: 0 }
        )
    }.sortedByDescending { it.totalScore }

    val totalRounds = game.score.size
    val leaderScore = standings.firstOrNull()?.totalScore

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .statusBarsPadding(),
        color = Color.Transparent
    ) {
        if (standings.isEmpty()) {
            EmptyScoreState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ScoreBoardHeader(
                    gameTitle = game.gameTitle,
                    totalRounds = totalRounds
                )

                PodiumSection(
                    podium = standings.take(3),
                    leaderScore = leaderScore ?: 0
                )

                SummarySection(
                    standings = standings,
                    totalRounds = totalRounds
                )

                val remainingPlayers = standings.drop(3)
                if (remainingPlayers.isNotEmpty()) {
                    RemainingPlayersList(
                        standings = remainingPlayers,
                        leaderScore = leaderScore ?: 0,
                        startRank = 4
                    )
                }
            }
        }
    }
}

private data class PlayerStanding(
    val player: Player,
    val totalScore: Int
)

@Composable
private fun ScoreBoardHeader(
    gameTitle: String,
    totalRounds: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Oyun Sonuçları",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = gameTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Toplam $totalRounds el tamamlandı",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PodiumSection(
    podium: List<PlayerStanding>,
    leaderScore: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        podium.getOrNull(0)?.let { first ->
            PodiumCard(
                standing = first,
                place = 1,
                leaderScore = leaderScore,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (podium.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                podium.getOrNull(1)?.let { second ->
                    PodiumCard(
                        standing = second,
                        place = 2,
                        leaderScore = leaderScore,
                        modifier = Modifier.weight(1f)
                    )
                }
                podium.getOrNull(2)?.let { third ->
                    PodiumCard(
                        standing = third,
                        place = 3,
                        leaderScore = leaderScore,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumCard(
    standing: PlayerStanding,
    place: Int,
    leaderScore: Int,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (place) {
        1 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }

    val icon = if (place == 1) Icons.Filled.EmojiEvents else Icons.Filled.MilitaryTech
    val subtitle = when {
        place == 1 -> "Birincilik senin!"
        leaderScore == standing.totalScore -> "Liderle aynı puan"
        else -> {
            val diff = leaderScore - standing.totalScore
            if (diff == 0) "Liderle aynı puan" else "$diff puan geride"
        }
    }

    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$place. ${standing.player.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
            Text(
                text = "${standing.totalScore} puan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun SummarySection(
    standings: List<PlayerStanding>,
    totalRounds: Int
) {
    val leader = standings.first()
    val runnerUp = standings.getOrNull(1)
    val leaderDifference = runnerUp?.let { leader.totalScore - it.totalScore }
    val totalPoints = standings.sumOf { it.totalScore }
    val averagePerRound = if (totalRounds > 0) totalPoints.toFloat() / totalRounds else 0f

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Özet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            SummaryRow(
                label = "El Sayısı",
                value = totalRounds.toString()
            )
            SummaryRow(
                label = "Lider Avantajı",
                value = leaderDifference?.let { if (it == 0) "Berabere" else "+$it" } ?: "-"
            )
            SummaryRow(
                label = "Toplam Puan",
                value = totalPoints.toString()
            )
            SummaryRow(
                label = "Ortalama (El Başına)",
                value = "%.1f".format(averagePerRound)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RemainingPlayersList(
    standings: List<PlayerStanding>,
    leaderScore: Int,
    startRank: Int
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "Diğer Oyuncular",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            LazyColumn {
                itemsIndexed(standings) { index, standing ->
                    val rank = startRank + index
                    RemainingPlayerRow(
                        standing = standing,
                        rank = rank,
                        leaderScore = leaderScore,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index != standings.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RemainingPlayerRow(
    standing: PlayerStanding,
    rank: Int,
    leaderScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$rank. ${standing.player.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            val diff = leaderScore - standing.totalScore
            Text(
                text = when {
                    diff == 0 -> "Liderle aynı puan"
                    diff > 0 -> "$diff puan geride"
                    else -> "+${-diff} puan önde"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${standing.totalScore}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyScoreState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Henüz skor girilmemiş",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Oyunu tamamlamak için skor ekleyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
