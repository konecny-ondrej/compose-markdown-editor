package me.okonecny.interactivetext

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalInteractiveScope = compositionLocalOf<InteractiveScope?> { null }
val LocalInteractiveInputHandler = compositionLocalOf<(TextInputCommand) -> Unit> { {} }

/**
 * Delimits region where all the interactive components will be considered parts of the same document.
 * @param scope State of this interactive region. Pass null to disable interaction.
 * @param interactiveContent Child components.
 */
@Composable
fun InteractiveContainer(
    scope: InteractiveScope? = rememberInteractiveScope(),
    selectionStyle: SelectionStyle = SelectionStyle(),
    modifier: Modifier = Modifier,
    onInput: (TextInputCommand) -> Unit = {},
    onCursorMovement: (CursorPosition) -> Unit = { scope?.cursorPosition = it },
    interactiveContent: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalInteractiveScope provides scope,
        LocalInteractiveInputHandler provides onInput
    ) {
        val interactiveModifier = if (scope == null) {
            Modifier
        } else {
            val requester = scope.focusRequester
            Modifier
                .focusRequester(requester)
                .focusable()
                .onGloballyPositioned { layoutCoordinates ->
                    scope.place(layoutCoordinates)
                }
                .keyboardCursorMovement(scope) { newCursorPosition, newSelection ->
                    scope.selection = newSelection
                    onCursorMovement(newCursorPosition)
                }
                .pointerCursorMovement(scope) { newCursorPosition, newSelection ->
                    requester.requestFocus()
                    scope.selection = newSelection
                    onCursorMovement(newCursorPosition)
                }
                .onKeyEvent { keyEvent: KeyEvent ->
                    if (keyEvent.key == Key.Escape) {
                        scope.selection = Selection.empty
                    }
                    false
                }
                .textInput(onInput = onInput)
                .paintSelection(scope, selectionStyle)
        }
        Box(modifier = interactiveModifier.then(modifier)) {
            interactiveContent()
        }
    }
}

@Composable
fun rememberInteractiveScope(vararg keys: Any?) = remember(keys) { InteractiveScope() }

/**
 * Delimits a region where interaction is disabled.
 */
@Composable
fun DisabledInteractiveContainer(interactiveContent: @Composable () -> Unit) =
    InteractiveContainer(scope = null, interactiveContent = interactiveContent)

data class SelectionStyle(
    val fillColor: Color = Color.Cyan.copy(alpha = 0.5f),
    val stroke: Stroke = Stroke()
) {
    data class Stroke(
        val width: Dp = 2.dp,
        val color: Color = Color.Cyan
    )
}

private fun Modifier.paintSelection(
    interactiveScope: InteractiveScope,
    selectionStyle: SelectionStyle
) = clip(RectangleShape)
    .drawWithContent {
        val selection = interactiveScope.selection
        if (selection.isEmpty
            || !interactiveScope.isPlaced
            || !interactiveScope.hasComponent(selection.start.componentId)
            || !interactiveScope.hasComponent(selection.end.componentId)
        ) {
            drawContent()
            return@drawWithContent
        }

        var combinedSelectionPath = Path()
        for (component in interactiveScope.componentsBetween(
            interactiveScope.getComponent(selection.start.componentId),
            interactiveScope.getComponent(selection.end.componentId)
        )) {
            val textLayout = component.textLayoutResult ?: continue
            val selectionStart = if (selection.start.componentId == component.id) {
                selection.start.visualOffset
            } else {
                0
            }
            val text = textLayout.layoutInput.text
            val selectionEnd = if (selection.end.componentId == component.id) {
                selection.end.visualOffset.coerceAtMost(text.length)
            } else {
                text.length
            }

            val componentSelectionPath = textLayout.getFilledPathForRange(selectionStart, selectionEnd, (selectionStyle.stroke.width + 1.dp).toPx())
            val positionInContainer =
                interactiveScope.containerCoordinates.localPositionOf(component.layoutCoordinates, Offset.Zero)
            componentSelectionPath.translate(positionInContainer)
            combinedSelectionPath = Path.combine(
                PathOperation.Union,
                combinedSelectionPath,
                componentSelectionPath
            )
        }

        drawContent()
        drawPath(combinedSelectionPath, selectionStyle.fillColor)
        drawPath(
            combinedSelectionPath,
            selectionStyle.stroke.color,
            style = Stroke(
                width = selectionStyle.stroke.width.toPx(),
                join = StrokeJoin.Round
            )
        )
    }

private fun TextLayoutResult.getFilledPathForRange(start: Int, end: Int, growBy: Float = 1f): Path {
    require(start in 0..end && end <= layoutInput.text.length) {
        "Start($start) or End($end) is out of range [0..${layoutInput.text.length})," +
                " or start > end!"
    }
    if (start == end) return Path()

    var closedPath = Path()
    for (characterPos in start..<end) {
        val characterBox = Path()
        val lineNo = getLineForOffset(characterPos)
        val lineTop = getLineTop(lineNo)
        val lineBottom = getLineBottom(lineNo)
        val charBounds = getBoundingBox(characterPos)
        characterBox.addRect(
            Rect(
                left = charBounds.left - growBy,
                top = lineTop - growBy,
                right = charBounds.right + growBy,
                bottom = lineBottom + growBy
            )
        )
        closedPath = Path.combine(
            PathOperation.Union,
            closedPath,
            characterBox
        )
    }

    return closedPath
}