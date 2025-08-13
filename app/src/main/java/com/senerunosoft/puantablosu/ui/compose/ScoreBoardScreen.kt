package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.config.OkeyConfig
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScoreBoardScreen(
    game: Game,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        val isOkeyOrYuzbir = game.gameType == GameType.Okey || game.gameType == GameType.YuzBirOkey
        val topWeight = 0.4f
        val bottomWeight = 0.6f
        val isTeamState: Boolean = when (game.gameType) {
            GameType.Okey -> (game.config as OkeyConfig).isPartnered
            GameType.YuzBirOkey -> (game.config as YuzBirOkeyConfig).isPartnered
            else -> false
        }
        if (isOkeyOrYuzbir) {
            OkeyYuzbirScoreHeader(
                players = game.playerList,
                scores = game.score,
                modifier = Modifier.weight(topWeight)
            )
        } else {
            GeneralScoreHeader(
                players = game.playerList,
                scores = game.score.first().scoreMap,
                modifier = Modifier.weight(topWeight)
            )
        }
        // Placeholder for animation and total scores
        Box(
            modifier = Modifier
                .weight(bottomWeight)
                .fillMaxWidth()
        ) {
            // TODO: Add round-by-round score animation here
            ScoreResultTable(
                players = game.playerList,
                scores = game.score.first().scoreMap,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun OkeyYuzbirScoreHeader(
    players: List<Player>,
    scores: List<Score>,
    modifier: Modifier = Modifier
) {
    var currentRound by remember { mutableStateOf(0) }
    val animatedTotals by animateIntAsState(
        targetValue = if (currentRound == scores.size) {
            scores.sumOf { it.scoreMap[players[currentRound % players.size].id] ?: 0 }
        } else {
            scores.take(currentRound).sumOf { it.scoreMap[players[currentRound % players.size].id] ?: 0 }
        },
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(scores) {
        scores.indices.forEach { round ->
            currentRound = round + 1
            delay(600)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Okey masası dairesel görünüm
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2.5f
            drawCircle(color = Color(0xFF8D5524), radius = radius)

            // Oyuncu pozisyonları (4 köşe)
            players.forEachIndexed { index, player ->
                val angle = (index * 90f - 45f)
                val x: Double = (radius * cos(angle.toRadians()) + center.x).toDouble()
                val y = radius * sin(angle.toRadians()) + center.y

                drawCircle(
                    color = Color(0xFF8D5524),
                    radius = 40f,
                    center = Offset(x.toFloat(), y.toFloat())
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        player.name.take(3),
                        x.toFloat(),
                        (y - 50f).toFloat(),
                        android.graphics.Paint().apply {
                            textSize = 30f
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )

                    drawText(
                        (scores.take(currentRound).sumOf { it.scoreMap[player.id] ?: 0 }).toString(),
                        x.toFloat(),
                        (y + 20f).toFloat(),
                        android.graphics.Paint().apply {
                            textSize = 36f
                            color = android.graphics.Color.WHITE
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

private fun Any.toRadians(): Double {
    return (this as Float) * Math.PI / 180.0
}

@Composable
fun IstakaView(
    playerName: String,
    score: Int,
    isTeam: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 24.dp)
                .background(
                    color = Color(0xFF8D5524), // Brown
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = playerName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Text(
            text = score.toString(),
            color = colorScheme.primary,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun GeneralScoreHeader(
    players: List<Player>,
    scores: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // Simpler header for general games
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        players.forEach { player ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = player.name,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = scores[player.id]?.toString() ?: "0",
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ScoreResultTable(
    players: List<Player>,
    scores: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // Simple table: Player | Total Score
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Oyuncu", fontWeight = FontWeight.Bold)
            Text("Toplam Puan", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        players.forEach { player ->
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(player.name)
                Text(scores[player.id]?.toString() ?: "0")
            }
        }
    }
}
