package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/**
 * Used as mutable state. Must stay a value object.
 */
data class CursorPosition(
    val componentId: InteractiveId,
    val offset: Int
) {
    companion object {
        val home = CursorPosition(firstInteractiveId, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CursorPosition

        if (componentId != other.componentId) return false
        if (offset != other.offset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentId.hashCode()
        result = 31 * result + offset
        return result
    }


}

/**
 * Draws cursor line inside a text component.
 * @param textLayoutResult Text Layout Result of the component. Used to determine where to draw the cursor line.
 * @param offset Text offset inside one component.
 * @param enabled If to show the cursor at all. Useful if you have multiple components.
 */
internal fun Modifier.cursorLine(
    textLayoutResult: TextLayoutResult?,
    offset: Int,
    enabled: Boolean
): Modifier {
    if (!enabled || textLayoutResult == null || offset < 0) return Modifier

    return composed {
        val cursorAlpha = remember { Animatable(1f) }
        LaunchedEffect(textLayoutResult, offset) {
            // ensure that the value is always 1f _this_ frame by calling snapTo
            cursorAlpha.snapTo(1f)
            // then start the cursor blinking on animation clock (500ms on to start)
            cursorAlpha.animateTo(0f, cursorAnimationSpec)
        }

        drawWithContent {
            drawContent()
            val cursorAlphaValue = cursorAlpha.value.coerceIn(0f, 1f)
            val cursorRect = textLayoutResult.getCursorRect(offset)
            val cursorWidth = floor(1.dp.toPx()).coerceAtLeast(1f) // TODO: style cursor width
            val cursorX = (cursorRect.left + cursorWidth / 2)
                .coerceAtMost(size.width - cursorWidth / 2)
            drawIntoCanvas {
                it.drawLine(
                    Offset(cursorX, cursorRect.top),
                    Offset(cursorX, cursorRect.bottom),
                    Paint().apply {
                        SolidColor(Color.Black) // TODO: style cursor brush
                            .applyTo(size, this, cursorAlphaValue)
                        strokeWidth = cursorWidth
                        isAntiAlias = false
                    }
                )
            }
        }
    }
}

private val cursorAnimationSpec: AnimationSpec<Float> = infiniteRepeatable(
    animation = keyframes {
        durationMillis = 1000
        1f at 0
        1f at 499
        0f at 500
        0f at 999
    }
)

