package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.ui.compose.components.GenericButton
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Add Score Dialog Composable
 * Replaces fragment_add_score_dialog.xml and AddScoreDialogFragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScoreDialog(
    players: List<Player>,
    gameType: com.senerunosoft.puantablosu.model.enums.GameType = com.senerunosoft.puantablosu.model.enums.GameType.GenelOyun,
    onSaveScore: (List<SingleScore>) -> Unit = {},
    onDismiss: () -> Unit = {},
    onInteraction: (String, Map<String, Any>?) -> Unit = { _, _ -> }
) {
    var playerScores by remember { 
        mutableStateOf(players.associate { it.id to "" }) 
    }
    var showError by remember { mutableStateOf(false) }
    var errorTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onInteraction("dialog_opened", mapOf("playerCount" to players.size, "gameType" to gameType.name))
    }

    Dialog(onDismissRequest = {
        onInteraction("dialog_dismissed", null)
        onDismiss()
    }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp), // Very round edges
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Energetic Colorful Header area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color(0xFF1976D2)) // Reverting back to classic primary blue
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stringResource(R.string.skor_ekle)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = when (gameType) {
                                com.senerunosoft.puantablosu.model.enums.GameType.Okey -> "Okey"
                                com.senerunosoft.puantablosu.model.enums.GameType.YuzBirOkey -> "101 Okey"
                                com.senerunosoft.puantablosu.model.enums.GameType.GenelOyun -> "Genel Oyun"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Player Input List (with distinct modern containers based on old colors)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    players.forEachIndexed { index, player ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (index % 2 == 0)
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedTextField(
                                    value = playerScores[player.id] ?: "",
                                    onValueChange = { newScore ->
                                        val isValid = when (gameType) {
                                            com.senerunosoft.puantablosu.model.enums.GameType.YuzBirOkey -> newScore.isEmpty() || newScore.matches(Regex("\\d*"))
                                            else -> newScore.isEmpty() || newScore.matches(Regex("-?\\d*"))
                                        }
                                        if (isValid) {
                                            playerScores = playerScores.toMutableMap().apply { this[player.id] = newScore }
                                        }
                                    },
                                    placeholder = {
                                        Text(text = if (gameType == com.senerunosoft.puantablosu.model.enums.GameType.YuzBirOkey) "0" else "0")
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(90.dp).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onInteraction("cancel_tapped", null)
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val result = validateAndCreateScores(players, playerScores)
                            when {
                                result.isSuccess -> {
                                    onInteraction("save_confirmed", mapOf("scoreCount" to (result.getOrNull()?.size ?: 0)))
                                    onSaveScore(result.getOrNull() ?: emptyList())
                                }
                                else -> {
                                    val error = result.exceptionOrNull() as? ScoreValidationException
                                    errorTitle = error?.title ?: "Hata"
                                    errorMessage = error?.message ?: "Bilinmeyen hata"
                                    showError = true
                                    onInteraction("save_validation_failed", mapOf("errorTitle" to errorTitle))
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    }

    // Error dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = {
                onInteraction("error_dialog_dismissed", null)
                showError = false
            },
            title = { Text(errorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    onInteraction("error_dialog_confirmed", null)
                    showError = false
                }) {
                    Text("Tamam")
                }
            }
        )
    }
}


// Custom exception for score validation
class ScoreValidationException(
    val title: String,
    override val message: String
) : Exception(message)

private fun validateAndCreateScores(
    players: List<Player>,
    playerScores: Map<String, String>
): Result<List<SingleScore>> {
    val singleScoreList = mutableListOf<SingleScore>()
    var hasValidInput = false

    for (player in players) {
        if (player.id.isBlank()) continue

        val scoreText = playerScores[player.id]?.trim() ?: ""
        if (scoreText.isNotEmpty()) {
            try {
                val score = scoreText.toInt()
                singleScoreList.add(SingleScore(player.id, score))
                hasValidInput = true
            } catch (e: NumberFormatException) {
                return Result.failure(
                    ScoreValidationException(
                        "Geçersiz Skor",
                        "Lütfen geçerli sayılar girin: ${player.name}"
                    )
                )
            }
        }
    }

    return when {
        !hasValidInput -> Result.failure(
            ScoreValidationException(
                "Boş Skor",
                "En az bir oyuncu için skor girmelisiniz"
            )
        )
        singleScoreList.size != players.size -> Result.failure(
            ScoreValidationException(
                "Eksik Skor",
                "Tüm oyuncular için skor girmelisiniz"
            )
        )
        else -> Result.success(singleScoreList)
    }
}

@Preview(showBackground = true)
@Composable
fun AddScoreDialogPreview() {
    ScoreBoardTheme {
        val samplePlayers = listOf(
            Player("1", "Oyuncu 1"),
            Player("2", "Oyuncu 2"),
            Player("3", "Oyuncu 3")
        )
        AddScoreDialog(
            players = samplePlayers,
            gameType = com.senerunosoft.puantablosu.model.enums.GameType.YuzBirOkey
        )
    }
}