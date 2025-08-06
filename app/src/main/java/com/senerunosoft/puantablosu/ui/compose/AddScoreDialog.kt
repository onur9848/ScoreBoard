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
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Add Score Dialog Composable
 * Replaces fragment_add_score_dialog.xml and AddScoreDialogFragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScoreDialog(
    players: List<Player>,
    onSaveScore: (List<SingleScore>) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var playerScores by remember { 
        mutableStateOf(players.associate { it.id to "" }) 
    }
    var showError by remember { mutableStateOf(false) }
    var errorTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1976D2) // primary_color
            )
        ) {
            Column(
                modifier = Modifier.padding(0.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.skor_ekle),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                // Divider
                HorizontalDivider(
                    thickness = 2.dp,
                    color = Color.White.copy(alpha = 0.3f)
                )

                // Player input fields
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(players) { player ->
                        PlayerScoreInput(
                            player = player,
                            score = playerScores[player.id] ?: "",
                            onScoreChange = { newScore ->
                                playerScores = playerScores.toMutableMap().apply {
                                    this[player.id] = newScore
                                }
                            }
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            // Validate and save scores
                            val result = validateAndCreateScores(players, playerScores)
                            when {
                                result.isSuccess -> {
                                    onSaveScore(result.getOrNull() ?: emptyList())
                                }
                                else -> {
                                    val error = result.exceptionOrNull() as? ScoreValidationException
                                    errorTitle = error?.title ?: "Hata"
                                    errorMessage = error?.message ?: "Bilinmeyen hata"
                                    showError = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1976D2)
                        )
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
            onDismissRequest = { showError = false },
            title = { Text(errorTitle) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerScoreInput(
    player: Player,
    score: String,
    onScoreChange: (String) -> Unit
) {
    OutlinedTextField(
        value = score,
        onValueChange = onScoreChange,
        label = { Text(player.name) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        )
    )
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
            players = samplePlayers
        )
    }
}