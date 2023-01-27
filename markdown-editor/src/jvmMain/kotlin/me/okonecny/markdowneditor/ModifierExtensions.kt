package me.okonecny.markdowneditor

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

fun Modifier.leftBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val xOffset = strokeWidthPx / 2f

        Modifier.drawBehind {
            val height = size.height

            drawLine(
                color = color,
                start = Offset(x = xOffset, y = 0f),
                end = Offset(x = xOffset, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)