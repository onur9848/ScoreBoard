package com.senerunosoft.puantablosu.ui.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Custom shapes for ScoreBoard app
val ScoreBoardShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Custom shape definitions for specific components
object ScoreBoardComponentShapes {
    val playerCard = RoundedCornerShape(12.dp)
    val scoreDialog = RoundedCornerShape(16.dp)
    val gameCard = RoundedCornerShape(8.dp)
    val inputField = RoundedCornerShape(8.dp)
    val button = RoundedCornerShape(8.dp)
    val floatingActionButton = RoundedCornerShape(16.dp)
}