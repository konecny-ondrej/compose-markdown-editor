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
        val strokeWidthPx = if (strokeWidth == Dp.Hairline) 1f else density.run { strokeWidth.toPx() }
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

fun Modifier.topBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = if (strokeWidth == Dp.Hairline) 1f else density.run { strokeWidth.toPx() }
        val yOffset = strokeWidthPx / 2f

        Modifier.drawBehind {
            val width = size.width

            drawLine(
                color = color,
                start = Offset(x = 0f, y = yOffset),
                end = Offset(x = width, y = yOffset),
                strokeWidth = strokeWidthPx
            )
        }
    }
)