package me.okonecny.interactivetext

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput


/**
 * Moves the cursor based on keyboard input.
 */

internal fun Modifier.keyboardCursorMovement(
    scope: InteractiveScope,
    onCursorPositionChanged: (CursorPosition, Selection) -> Unit
): Modifier = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
    val oldPosition = scope.cursorPosition
    if (!oldPosition.isValid) return@onKeyEvent false
    val newPosition = when (keyEvent.key) {
        Key.DirectionLeft -> {
            if (keyEvent.isCtrlPressed) {
                scope.moveCursorLeftByWord(oldPosition)
            } else {
                scope.moveCursorLeft(oldPosition)
            }
        }

        Key.DirectionRight -> {
            if (keyEvent.isCtrlPressed) {
                scope.moveCursorRightByWord(oldPosition)
            } else {
                scope.moveCursorRight(oldPosition)
            }
        }

        Key.DirectionDown -> {
            scope.moveCursorDown(oldPosition)
        }

        Key.DirectionUp -> {
            scope.moveCursorUp(oldPosition)
        }

        Key.MoveHome -> {
            scope.moveCursorHome(oldPosition)
        }

        Key.MoveEnd -> {
            scope.moveCursorToEnd(oldPosition)
        }
        // TODO: handle page up and page down, too.
        else -> oldPosition
    }
    if (newPosition != oldPosition) {
        val newSelection = if (keyEvent.isShiftPressed) updateSelection(
            scope.selection,
            oldPosition,
            newPosition,
            scope.componentLayout
        ) else Selection.empty
        onCursorPositionChanged(newPosition, newSelection)
    }
    false
}

private fun moveCursor(
    scope: InteractiveScope,
    position: Offset,
): CursorPosition {
    val layout = scope.componentLayout
    if (!layout.hasAnyComponents) return CursorPosition.invalid

    val tappedComponent = layout.componentAt(position)
    val textLayout = tappedComponent.textLayoutResult

    if (!tappedComponent.hasText || textLayout == null) {
        return CursorPosition(tappedComponent.id, 0)
    }

    val localOffset = tappedComponent.layoutCoordinates.localPositionOf(
        layout.containerCoordinates,
        position
    )
    val textOffset = textLayout.getOffsetForPosition(localOffset)
    return CursorPosition(tappedComponent.id, textOffset)
}

internal fun Modifier.pointerCursorMovement(
    scope: InteractiveScope,
    onCursorPositionChanged: (CursorPosition, Selection) -> Unit
): Modifier = composed {
    val onCursorChangeCallback by rememberUpdatedState(onCursorPositionChanged)
    pointerInput(scope) {
        detectTapGestures(
            onPress = { position ->
                val newCursorPosition = moveCursor(scope, position)
                onCursorChangeCallback(newCursorPosition, Selection.empty)
            },
            onDoubleTap = { position ->
                val clickedCursorPosition = moveCursor(scope, position)
                if (!clickedCursorPosition.isValid) return@detectTapGestures

                val wordStartCursorPosition = scope.moveCursorLeftByWord(clickedCursorPosition)
                val wordEndCursorPosition = scope.moveCursorRightByWord(wordStartCursorPosition)
                onCursorChangeCallback(
                    wordEndCursorPosition,
                    Selection(wordStartCursorPosition, wordEndCursorPosition)
                )
            }
        )
    }.pointerInput(scope) {
        detectDragGestures { change, _ ->
            val newCursorPosition = moveCursor(scope, change.position)
            val newSelection = updateSelection(
                scope.selection,
                scope.cursorPosition,
                newCursorPosition,
                scope.componentLayout
            )
            onCursorChangeCallback(newCursorPosition, newSelection)
        }
    }.pointerHoverIcon(PointerIcon.Text)
}

private fun InteractiveScope.moveCursorByCharsInComponent(
    cursorPosition: CursorPosition,
    charOffset: Int
): CursorPosition {
    require(charOffset == 1 || charOffset == -1) { "Direction of cursor movement can only ever be 1 or -1." }
    val component = getComponent(cursorPosition.componentId)
    val originalPosition =
        component.textLayoutResult?.getHorizontalPosition(cursorPosition.visualOffset, usePrimaryDirection = true)
    var newOffset: Int
    var retries = 1
    do {
        newOffset = (cursorPosition.visualOffset + (charOffset * retries))
            .coerceAtMost(component.visualTextRange.end)
            .coerceAtLeast(component.visualTextRange.start)
        retries++
        val newPosition = component.textLayoutResult?.getHorizontalPosition(
            newOffset,
            usePrimaryDirection = true
        )
    } while (
        originalPosition == newPosition
        && newOffset != component.visualTextRange.start
        && newOffset != component.visualTextRange.end
    )

    return CursorPosition(component.id, newOffset)
}

private fun InteractiveScope.moveCursorByLine(cursorPosition: CursorPosition, lineOffset: Int): CursorPosition {
    val component = getComponent(cursorPosition.componentId)
    val textLayout = component.textLayoutResult
    if (!component.hasText || textLayout == null) return cursorPosition

    val currentLine = textLayout.getLineForOffset(cursorPosition.visualOffset)
    val newLine = currentLine + lineOffset
    if (newLine < 0 || newLine >= textLayout.lineCount) return cursorPosition

    val positionOnLine = getOffsetFromLineStart(cursorPosition)
    val newLineStart = textLayout.getLineStart(newLine)
    val newCursorOffset = (newLineStart + positionOnLine)
        .coerceAtMost(textLayout.getLineEnd(newLine, true))
        .coerceAtLeast(newLineStart)
    return CursorPosition(component.id, newCursorOffset)
}

fun InteractiveScope.moveCursorLeft(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByCharsInComponent(oldPosition, -1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val oldComponent = getComponent(oldPosition.componentId)
    val newComponent: InteractiveComponent = componentLayout.componentPreviousOnLineFrom(oldComponent)
    if (oldComponent == newComponent) return oldPosition // TODO: onOverscroll callback to move the window further?

    return CursorPosition(newComponent.id, newComponent.visualTextRange.end)
}

fun InteractiveScope.moveCursorLeftByWord(oldPosition: CursorPosition): CursorPosition =
    moveCursorByWord(oldPosition, ::moveCursorLeft)

fun InteractiveScope.moveCursorRight(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByCharsInComponent(oldPosition, 1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val oldComponent = getComponent(oldPosition.componentId)
    val newComponent: InteractiveComponent = componentLayout.componentNextOnLineTo(oldComponent)
    if (oldComponent == newComponent) return oldPosition // TODO: onOverscroll callback to move the window further?

    return CursorPosition(newComponent.id, newComponent.visualTextRange.start)
}

fun InteractiveScope.moveCursorRightByWord(oldPosition: CursorPosition): CursorPosition =
    moveCursorByWord(oldPosition, ::moveCursorRight)

fun InteractiveScope.moveCursorByWord(
    oldPosition: CursorPosition,
    moveStep: (CursorPosition) -> CursorPosition
): CursorPosition {
    var prevPosition = oldPosition
    var newPosition = moveStep(prevPosition)
    if (prevPosition.componentId != newPosition.componentId) return newPosition // Skipping components is as good as stopping.
    val stopChar = ' '

    while (prevPosition != newPosition) {
        val component = componentLayout.getComponent(newPosition.componentId)
        val text = component.textLayoutResult?.layoutInput?.text ?: stopChar.toString()
        val currentChar = text[newPosition.visualOffset.coerceAtMost(text.lastIndex)]
        if (currentChar == stopChar) break
        prevPosition = newPosition
        newPosition = moveStep(newPosition)
        if (prevPosition.componentId != newPosition.componentId) return prevPosition
    }
    return newPosition
}

fun InteractiveScope.moveCursorDown(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByLine(oldPosition, 1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val componentLayout = componentLayout
    val cursorVisualOffset = oldPosition.visualRect(componentLayout).center
    val componentBelow = componentLayout.componentBelow(cursorVisualOffset)

    val newTextLayout = componentBelow.textLayoutResult
    if (newTextLayout == null || !componentBelow.hasText) return CursorPosition(componentBelow.id, 0)

    return CursorPosition(
        componentBelow.id, getOffsetFromLineStart(oldPosition)
            .coerceAtMost(newTextLayout.getLineEnd(0))
    )
}

fun InteractiveScope.moveCursorUp(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByLine(oldPosition, -1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val componentLayout = componentLayout
    val cursorVisualOffset = oldPosition.visualRect(componentLayout).center
    val componentAbove = componentLayout.componentAbove(cursorVisualOffset)

    val newTextLayout = componentAbove.textLayoutResult
    if (newTextLayout == null || !componentAbove.hasText) return CursorPosition(componentAbove.id, 0)

    val newLastLine = (newTextLayout.lineCount - 1).coerceAtLeast(0)
    val newLastLineStart = newTextLayout.getLineStart(newLastLine)
    val newTextOffset = (newLastLineStart + getOffsetFromLineStart(oldPosition))
        .coerceAtMost(newTextLayout.getLineEnd(newLastLine))
    return CursorPosition(componentAbove.id, newTextOffset)
}

fun InteractiveScope.moveCursorHome(oldPosition: CursorPosition): CursorPosition {
    val offsetFromLineStart = getOffsetFromLineStart(oldPosition)
    if (offsetFromLineStart > 0) return CursorPosition(
        oldPosition.componentId,
        oldPosition.visualOffset - offsetFromLineStart
    )
    // Try to move to the leftmost component if we are already at the line start?
    return oldPosition
}

fun InteractiveScope.moveCursorToEnd(oldPosition: CursorPosition): CursorPosition {
    val component = getComponent(oldPosition.componentId)
    val textLayout = component.textLayoutResult
    if (!component.hasText || textLayout == null) return oldPosition
    // Try to move to the rightmost component if we are already at the line end?
    return CursorPosition(
        oldPosition.componentId,
        (textLayout.getLineEnd(textLayout.getLineForOffset(oldPosition.visualOffset), true))
    )
}

private fun InteractiveScope.getOffsetFromLineStart(cursorPosition: CursorPosition): Int {
    val component = getComponent(cursorPosition.componentId)
    val textLayoutResult = component.textLayoutResult
    if (!component.hasText || textLayoutResult == null) return 0
    val offset = cursorPosition.visualOffset
    val line = textLayoutResult.getLineForOffset(offset)
    val start = textLayoutResult.getLineStart(line)
    return offset - start
}