package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.key.*
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

/**
 * Moves the cursor based on keyboard input.
 */
@OptIn(ExperimentalComposeUiApi::class) // Because of key codes.
internal fun Modifier.cursorKeyboardInteraction(
    scope: InteractiveScope,
    onCursorPositionChanged: (CursorPosition) -> Unit
): Modifier = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
    val oldPosition = scope.cursorPosition.value
    val oldComponent = scope.getComponent(oldPosition.componentId)

    // TODO: refactor
    when (keyEvent.key) {
        Key.DirectionLeft -> {
            val newComponent: InteractiveComponent = if (oldPosition.offset == oldComponent.textRange.start) {
                scope.prevTo(oldComponent)
            } else {
                oldComponent
            }
            val newOffset = if (oldPosition.offset == oldComponent.textRange.start) {
                if (oldComponent == newComponent) {
                    oldPosition.offset // TODO: onOverscroll callback to move the window further?
                } else {
                    newComponent.textRange.end
                }
            } else {
                oldPosition.offset - 1
            }
            onCursorPositionChanged(CursorPosition(newComponent.id, newOffset))
            true
        }

        Key.DirectionRight -> {
            val newComponent: InteractiveComponent = if (oldPosition.offset == oldComponent.textRange.end) {
                scope.nextTo(oldComponent)
            } else {
                oldComponent
            }
            val newOffset = if (oldPosition.offset == oldComponent.textRange.end) {
                if (oldComponent == newComponent) {
                    oldPosition.offset // TODO: onOverscroll callback to move the window further?
                } else {
                    newComponent.textRange.start
                }
            } else {
                oldPosition.offset + 1
            }
            onCursorPositionChanged(CursorPosition(newComponent.id, newOffset))
            true
        }

        Key.DirectionDown -> {
            val oldTextLayout = oldComponent.textLayoutResult
            if (oldTextLayout == null) {
                onCursorPositionChanged(
                    CursorPosition(
                        scope.nextTo(oldComponent).id,
                        0
                    )
                ) // TODO: find component below the old one. Will be useful for tables.
            } else {
                val oldLine = oldTextLayout.getLineForOffset(oldPosition.offset)
                val oldLineOffset = oldPosition.offset - oldTextLayout.getLineStart(oldLine)
                val oldWasLastLine = oldLine == oldTextLayout.lineCount - 1
                val newComponent: InteractiveComponent = if (oldWasLastLine) {
                    scope.nextTo(oldComponent) // TODO: find component below the old one. Will be useful for tables.
                } else {
                    oldComponent
                }
                val newTextLayoutResult = newComponent.textLayoutResult
                val newOffset = if (newTextLayoutResult == null || newTextLayoutResult.lineCount < 1) {
                    0
                } else {
                    val lineEnd = newTextLayoutResult.getLineEnd(0, true)
                    if (lineEnd < oldLineOffset) {
                        lineEnd
                    } else {
                        oldLineOffset
                    }
                }
                onCursorPositionChanged(CursorPosition(newComponent.id, newOffset))
            }
            true
        }

        Key.DirectionUp -> {
            val oldTextLayout = oldComponent.textLayoutResult
            if (oldTextLayout == null) {
                onCursorPositionChanged(
                    CursorPosition(
                        scope.prevTo(oldComponent).id,
                        0
                    )
                ) // TODO: find component above the old one. Will be useful for tables.
            } else {
                val oldLine = oldTextLayout.getLineForOffset(oldPosition.offset)
                val oldLineOffset = oldPosition.offset - oldTextLayout.getLineStart(oldLine)
                val oldWasFirstLine = oldLine == 0
                val newComponent: InteractiveComponent = if (oldWasFirstLine) {
                    scope.prevTo(oldComponent) // TODO: find component above the old one. Will be useful for tables.
                } else {
                    oldComponent
                }
                val newTextLayoutResult = newComponent.textLayoutResult
                val newOffset = if (newTextLayoutResult == null || newTextLayoutResult.lineCount < 1) {
                    0
                } else {
                    val lineEnd = newTextLayoutResult.getLineEnd(0, true)
                    if (lineEnd < oldLineOffset) {
                        lineEnd
                    } else {
                        oldLineOffset
                    }
                }
                onCursorPositionChanged(CursorPosition(newComponent.id, newOffset))
            }
            true
        }

        // TODO: handle up and down keys, page up and page down, too.
        else -> false
    }
}