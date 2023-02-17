package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*


/**
 * Moves the cursor based on keyboard input.
 */

internal fun Modifier.keyboardCursorMovement(
    scope: InteractiveScope,
    onCursorPositionChanged: (CursorPosition) -> Unit
): Modifier = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
    val oldPosition by scope.cursorPosition
    when (keyEvent.key) {
        @OptIn(ExperimentalComposeUiApi::class)
        Key.DirectionLeft -> {
            onCursorPositionChanged(scope.moveCursorLeft(oldPosition))
            true
        }

        @OptIn(ExperimentalComposeUiApi::class)
        Key.DirectionRight -> {
            onCursorPositionChanged(scope.moveCursorRight(oldPosition))
            true
        }

        @OptIn(ExperimentalComposeUiApi::class)
        Key.DirectionDown -> {
            onCursorPositionChanged(scope.moveCursorDown(oldPosition))
            true
        }

        @OptIn(ExperimentalComposeUiApi::class)
        Key.DirectionUp -> {
            onCursorPositionChanged(scope.moveCursorUp(oldPosition))
            true
        }

        @OptIn(ExperimentalComposeUiApi::class)
        Key.MoveHome -> {
            onCursorPositionChanged(scope.moveCursorHome(oldPosition))
            true
        }

        @OptIn(ExperimentalComposeUiApi::class)
        Key.MoveEnd -> {
            onCursorPositionChanged(scope.moveCursorToEnd(oldPosition))
            true
        }

        // TODO: handle page up and page down, too.
        else -> false
    }
}


internal fun Modifier.pointerCursorMovement(
    scope: InteractiveScope,
    onCursorPositionChanged: (CursorPosition) -> Unit
): Modifier = pointerInput(scope) {
    detectTapGestures(onPress = { position ->
        val layout = scope.requireComponentLayout()
        val tappedComponent = layout.componentAt(position)
        val textLayout = tappedComponent.textLayoutResult
        if (!tappedComponent.hasText || textLayout == null) {
            CursorPosition(tappedComponent.id, 0)
            return@detectTapGestures
        }

        val localOffset = tappedComponent.layoutCoordinates.localPositionOf(
            layout.containerCoordinates,
            position
        )
        val textOffset = textLayout.getOffsetForPosition(localOffset)


        onCursorPositionChanged(CursorPosition(tappedComponent.id, textOffset))
    })
}.pointerHoverIcon(@OptIn(ExperimentalComposeUiApi::class) PointerIconDefaults.Text)

private fun InteractiveScope.moveCursorByChars(cursorPosition: CursorPosition, charOffset: Int): CursorPosition {
    val component = getComponent(cursorPosition.componentId)
    val newOffset = (cursorPosition.offset + charOffset)
        .coerceAtMost(component.textRange.end)
        .coerceAtLeast(component.textRange.start)
    return CursorPosition(component.id, newOffset)
}

private fun InteractiveScope.moveCursorByLine(cursorPosition: CursorPosition, lineOffset: Int): CursorPosition {
    val component = getComponent(cursorPosition.componentId)
    val textLayout = component.textLayoutResult
    if (!component.hasText || textLayout == null) return cursorPosition

    val currentLine = textLayout.getLineForOffset(cursorPosition.offset)
    val newLine = currentLine + lineOffset
    if (newLine < 0 || newLine >= textLayout.lineCount) return cursorPosition

    val positionOnLine = getOffsetFromLineStart(cursorPosition)
    val newLineStart = textLayout.getLineStart(newLine)
    val newCursorOffset = (newLineStart + positionOnLine)
        .coerceAtMost(textLayout.getLineEnd(newLine, true))
        .coerceAtLeast(newLineStart)
    return CursorPosition(component.id, newCursorOffset)
}

private fun InteractiveScope.moveCursorLeft(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByChars(oldPosition, -1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val oldComponent = getComponent(oldPosition.componentId)
    val newComponent: InteractiveComponent = requireComponentLayout().componentPreviousOnLineFrom(oldComponent)
    if (oldComponent == newComponent) return oldPosition // TODO: onOverscroll callback to move the window further?

    return CursorPosition(newComponent.id, newComponent.textRange.end)
}

private fun InteractiveScope.moveCursorRight(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByChars(oldPosition, 1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val oldComponent = getComponent(oldPosition.componentId)
    val newComponent: InteractiveComponent = requireComponentLayout().componentNextOnLineTo(oldComponent)
    if (oldComponent == newComponent) return oldPosition // TODO: onOverscroll callback to move the window further?

    return CursorPosition(newComponent.id, newComponent.textRange.start)
}

private fun InteractiveScope.moveCursorDown(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByLine(oldPosition, 1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val componentLayout = requireComponentLayout()
    val cursorVisualOffset = componentLayout.cursorVisualOffset(oldPosition)
    val componentBelow = componentLayout.componentBelow(cursorVisualOffset)

    val newTextLayout = componentBelow.textLayoutResult
    if (newTextLayout == null || !componentBelow.hasText) return CursorPosition(componentBelow.id, 0)

    return CursorPosition(
        componentBelow.id, getOffsetFromLineStart(oldPosition)
            .coerceAtMost(newTextLayout.getLineEnd(0))
    )
}

private fun InteractiveScope.moveCursorUp(oldPosition: CursorPosition): CursorPosition {
    val lineCursorPosition = moveCursorByLine(oldPosition, -1)
    if (lineCursorPosition != oldPosition) return lineCursorPosition

    val componentLayout = requireComponentLayout()
    val cursorVisualOffset = componentLayout.cursorVisualOffset(oldPosition)
    val componentAbove = componentLayout.componentAbove(cursorVisualOffset)

    val newTextLayout = componentAbove.textLayoutResult
    if (newTextLayout == null || !componentAbove.hasText) return CursorPosition(componentAbove.id, 0)

    val newLastLine = (newTextLayout.lineCount - 1).coerceAtLeast(0)
    val newLastLineStart = newTextLayout.getLineStart(newLastLine)
    val newTextOffset = (newLastLineStart + getOffsetFromLineStart(oldPosition))
        .coerceAtMost(newTextLayout.getLineEnd(newLastLine))
    return CursorPosition(componentAbove.id, newTextOffset)
}

private fun InteractiveScope.moveCursorHome(oldPosition: CursorPosition): CursorPosition {
    val offsetFromLineStart = getOffsetFromLineStart(oldPosition)
    if (offsetFromLineStart > 0) return CursorPosition(
        oldPosition.componentId,
        oldPosition.offset - offsetFromLineStart
    )
    // Try to move to the leftmost component if we are already at the line start?
    return oldPosition
}

private fun InteractiveScope.moveCursorToEnd(oldPosition: CursorPosition): CursorPosition {
    val component = getComponent(oldPosition.componentId)
    val textLayout = component.textLayoutResult
    if (!component.hasText || textLayout == null) return oldPosition
    // Try to move to the rightmost component if we are already at the line end?
    return CursorPosition(
        oldPosition.componentId,
        (textLayout.getLineEnd(textLayout.getLineForOffset(oldPosition.offset), true))
    )
}

private fun InteractiveScope.getOffsetFromLineStart(cursorPosition: CursorPosition): Int {
    val component = getComponent(cursorPosition.componentId)
    val textLayoutResult = component.textLayoutResult
    if (!component.hasText || textLayoutResult == null) return 0
    val offset = cursorPosition.offset
    val line = textLayoutResult.getLineForOffset(offset)
    val start = textLayoutResult.getLineStart(line)
    return offset - start
}