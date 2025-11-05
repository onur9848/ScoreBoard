package com.senerunosoft.puantablosu.ui.compose.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Player

/**
 * Player header row component for BoardScreen
 * Displays all player names in a horizontal row
 */
@Composable
fun PlayerHeaderRow(
    players: List<Player>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.turn),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(56.dp)
                .padding(horizontal = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(4.dp))
        players.forEachIndexed { index, player ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (index % 2 == 0)
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                        else
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
            ) {
                Text(
                    text = player.name,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (index < players.size - 1) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}
