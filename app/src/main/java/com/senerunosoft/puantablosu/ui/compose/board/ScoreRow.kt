package com.senerunosoft.puantablosu.ui.compose.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score

/**
 * Single score row component for BoardScreen
 * Displays scores for all players in a round
 */
@Composable
fun ScoreRow(
    roundScore: Score,
    players: List<Player>,
    roundNumber: Int,
    isPenalty: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (roundNumber % 2 == 1)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isPenalty) "-" else "#$roundNumber",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        players.forEachIndexed { index, player ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (index % 2 == 0)
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
            ) {
                val score = roundScore.scoreMap[player.id] ?: 0
                Text(
                    text = score.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (index < players.size - 1) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}
