package com.senerunosoft.puantablosu.ui.compose.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.config.RuleConfig
import com.senerunosoft.puantablosu.model.enums.RuleType

/**
 * Dialog for PlayerPenaltyScore rule
 * Allows selecting a player and applies penalty score
 */
@Composable
fun PlayerPenaltyDialog(
    rule: RuleConfig,
    players: List<Player>,
    selectedPlayerId: String?,
    onPlayerSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { 
                Text("Kaydet") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("İptal") 
            }
        },
        title = { Text("${rule.label} - Oyuncu Seç") },
        text = {
            Column {
                Text("Oyuncu Seçin:")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    players.forEach { player ->
                        FilterChip(
                            selected = selectedPlayerId == player.id,
                            onClick = { onPlayerSelected(player.id) },
                            label = { Text(player.name) }
                        )
                    }
                }
            }
        }
    )
}

/**
 * Dialog for FinishScore rule with paired input
 * Allows selecting winner and entering paired score value
 */
@Composable
fun FinishScoreDialog(
    rule: RuleConfig,
    pairedRule: RuleConfig,
    players: List<Player>,
    selectedPlayerId: String?,
    pairedInputValue: String,
    onPlayerSelected: (String) -> Unit,
    onPairedValueChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { 
                Text("Kaydet") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("İptal") 
            }
        },
        title = { Text("${rule.label} - Oyuncu Seç ve ${pairedRule.label} Değeri Gir") },
        text = {
            Column {
                Text("Oyuncu Seçin:")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    players.forEach { player ->
                        FilterChip(
                            selected = selectedPlayerId == player.id,
                            onClick = { onPlayerSelected(player.id) },
                            label = { Text(player.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = pairedInputValue,
                    onValueChange = onPairedValueChanged,
                    label = { Text(pairedRule.label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    )
}

/**
 * Helper to display the correct dialog based on rule type
 */
@Composable
fun RuleDialog(
    rule: RuleConfig,
    pairedRule: RuleConfig?,
    players: List<Player>,
    selectedPlayerId: String?,
    pairedInputValue: String,
    onPlayerSelected: (String) -> Unit,
    onPairedValueChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    when (rule.types.first()) {
        RuleType.PlayerPenaltyScore -> {
            PlayerPenaltyDialog(
                rule = rule,
                players = players,
                selectedPlayerId = selectedPlayerId,
                onPlayerSelected = onPlayerSelected,
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
        RuleType.FinishScore -> {
            if (pairedRule != null) {
                FinishScoreDialog(
                    rule = rule,
                    pairedRule = pairedRule,
                    players = players,
                    selectedPlayerId = selectedPlayerId,
                    pairedInputValue = pairedInputValue,
                    onPlayerSelected = onPlayerSelected,
                    onPairedValueChanged = onPairedValueChanged,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss
                )
            }
        }
        else -> { /* Other rule types not yet implemented */ }
    }
}
