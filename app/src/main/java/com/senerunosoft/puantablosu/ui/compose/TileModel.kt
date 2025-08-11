package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class TileColor { RED, BLACK, BLUE, YELLOW }

fun tileColorFromCode(code: Char): TileColor = when (code.lowercaseChar()) {
    'r' -> TileColor.RED
    's' -> TileColor.BLACK
    'm' -> TileColor.BLUE
    'y' -> TileColor.YELLOW
    else -> TileColor.RED
}

fun tileColorToPaint(c: TileColor): Color = when (c) {
    TileColor.RED   -> Color(0xFFD63031)
    TileColor.BLACK -> Color(0xFF2D3436)
    TileColor.BLUE  -> Color(0xFF0984E3)
    TileColor.YELLOW-> Color(0xFFF2C200)
}

@Immutable
data class OkeyTileModel(
    val value: Int,                 // 1..13
    val color: TileColor,
    val isFakeJoker: Boolean = false,
    val isSelected: Boolean = false
)
