package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OkeyTile(
    tile: OkeyTileModel,
    modifier: Modifier = Modifier.size(44.dp)
) {
    val measurer = rememberTextMeasurer()
    val highlight = animateFloatAsState(if (tile.isSelected) 1f else 0f, label = "sel")
    val numberText = if (tile.isFakeJoker) "★" else tile.value.coerceIn(1, 13).toString()
    val numberColor = tileColorToPaint(tile.color)

    val style = TextStyle(
        color = numberColor,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Default
    )

    Canvas(
        modifier = modifier.clip(RoundedCornerShape(10))
    ) {
        val r = 10f
        val body = Rect(0f, 0f, size.width, size.height)
        val cr = CornerRadius(r, r)

        val plastic = Brush.verticalGradient(
            0f to Color(0xFFF9F7F4),
            0.55f to Color(0xFFEFEAE2),
            1f to Color(0xFFE6DFD6),
            startY = 0f, endY = size.height
        )
        drawRoundRect(brush = plastic, cornerRadius = cr)
        drawRectShadow(body, radius = 6f, dy = 2f, alpha = 0.35f, corner = cr)
        drawBevel(body, cr)
        drawTopGloss(body, cr, alpha = 0.14f)

        if (highlight.value > 0f) {
            inset(2.dp.toPx()) {
                val sel = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f * highlight.value),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.35f),
                    radius = size.minDimension * 0.6f
                )
                drawRoundRect(brush = sel, cornerRadius = CornerRadius(8f, 8f), blendMode = BlendMode.Softlight)
            }
        }

        val layout = measurer.measure(numberText, style)
        val textX = size.width / 2 - layout.size.width / 2
        val textY = size.height * 0.42f - layout.size.height / 2
        drawText(layout, topLeft = Offset(textX, textY))

        if (!tile.isFakeJoker) {
            val y = size.height * 0.70f
            drawCircle(color = numberColor, radius = size.minDimension * 0.06f, center = Offset(size.width * 0.40f, y))
            drawCircle(color = numberColor, radius = size.minDimension * 0.06f, center = Offset(size.width * 0.60f, y))
        }

        drawRoundRect(
            color = Color(0xFFD1C8BD).copy(alpha = 0.7f),
            topLeft = Offset(size.width * 0.33f, size.height * 0.80f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.34f, size.height * 0.06f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawRectShadow(
    rect: Rect,
    radius: Float,
    dy: Float,
    alpha: Float,
    corner: CornerRadius
) {
    drawRoundRect(
        color = Color.Black.copy(alpha = alpha),
        topLeft = Offset(rect.left, rect.top + dy),
        size = rect.size,
        cornerRadius = corner,
        blendMode = BlendMode.SrcOver
    )
}

private fun DrawScope.drawBevel(rect: Rect, corner: CornerRadius) {
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
            start = Offset(rect.left, rect.top),
            end = Offset(rect.left, rect.top + rect.height * 0.35f)
        ),
        cornerRadius = corner
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.10f)),
            start = Offset(rect.left, rect.bottom - rect.height * 0.35f),
            end = Offset(rect.left, rect.bottom)
        ),
        cornerRadius = corner
    )
}

private fun DrawScope.drawTopGloss(rect: Rect, corner: CornerRadius, alpha: Float) {
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = alpha),
            1f to Color.Transparent
        ),
        size = androidx.compose.ui.geometry.Size(rect.width, rect.height * 0.28f),
        cornerRadius = corner
    )
}

@Preview(showBackground = true)
@Composable
fun OkeyTilePreview() {
    OkeyTile(
        tile = OkeyTileModel(
            value = 5,
            color = TileColor.RED,
            isFakeJoker = false,
            isSelected = true
        ),
        modifier = Modifier.size(90.dp)
    )
}
