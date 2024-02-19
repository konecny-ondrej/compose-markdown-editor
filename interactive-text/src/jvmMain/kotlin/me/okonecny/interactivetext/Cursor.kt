package me.okonecny.interactivetext

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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/**
 * Used as mutable state. Must stay a value object.
 */
data class CursorPosition(
    val componentId: InteractiveId,
    val visualOffset: Int
) {
    companion object {
        val invalid: CursorPosition? = null
    }

    internal fun isBefore(other: CursorPosition, scope: InteractiveScope): Boolean {
        if (componentId == other.componentId) return visualOffset < other.visualOffset
        return scope.isComponentBefore(componentId, other.componentId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CursorPosition

        if (componentId != other.componentId) return false
        if (visualOffset != other.visualOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = componentId.hashCode()
        result = 31 * result + visualOffset
        return result
    }
}

/**
 * Draws cursor line inside a text component.
 * @param textLayoutResult Text Layout Result of the component. Used to determine where to draw the cursor line.
 * @param offset Text offset inside one component.
 */
internal fun Modifier.cursorLine(
    textLayoutResult: TextLayoutResult?,
    offset: Int
): Modifier {
    if (textLayoutResult == null || offset < 0) return Modifier

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
            val cursorRect = try {
                textLayoutResult.getCursorRect(offset)
            } catch (e: NullPointerException) { // Workaround for the Paragraph bugging out sometimes.
                return@drawWithContent
            }

            val cursorWidth = floor(2.dp.toPx()).coerceAtLeast(1f) // TODO: style cursor width
            val cursorX = (cursorRect.left + cursorWidth / 2)
                .coerceAtMost(size.width - cursorWidth / 2)
            drawLine(
                color = Color.Black.copy(alpha = cursorAlphaValue), // TODO: style cursor color
                start = Offset(cursorX, cursorRect.top),
                end = Offset(cursorX, cursorRect.bottom),
                strokeWidth = cursorWidth

            )
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

/**
 * Find the visual position of the cursor in the layout.
 */
fun InteractiveScope.cursorVisualRect(cursorPosition: CursorPosition): Rect {
    val component = getComponent(cursorPosition.componentId)
    val componentTextLayout = component.textLayoutResult
    val componentLayoutCoordinates = component.attachedLayoutCoordinates ?: return Rect.Zero
    if (!component.hasText || componentTextLayout == null) {
        return Rect(containerCoordinates.localCenterPointOf(component) ?: Offset.Zero, 0f)
    }
    val componentCursorRect = componentTextLayout.getCursorRect(cursorPosition.visualOffset)
    return Rect(
        containerCoordinates.localPositionOf(componentLayoutCoordinates, componentCursorRect.topLeft),
        containerCoordinates.localPositionOf(componentLayoutCoordinates, componentCursorRect.bottomRight)
    )
}
