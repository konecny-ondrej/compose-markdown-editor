package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*


/**
 * Moves the cursor based on keyboard input.
 */
@OptIn(ExperimentalComposeUiApi::class) // Because of key codes.
internal fun Modifier.keyboardCursorMovement(
    scope: InteractiveScope,
    onCursorPositionChanged: (CursorPosition) -> Unit
): Modifier = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
    val oldPosition by scope.cursorPosition

    when (keyEvent.key) {
        Key.DirectionLeft -> {
            onCursorPositionChanged(scope.moveCursorLeft(oldPosition))
            true
        }

        Key.DirectionRight -> {
            onCursorPositionChanged(scope.moveCursorRight(oldPosition))
            true
        }

        Key.DirectionDown -> {
            onCursorPositionChanged(scope.moveCursorDown(oldPosition))
            true
        }

        Key.DirectionUp -> {
            onCursorPositionChanged(scope.moveCursorUp(oldPosition))
            true
        }

        // TODO: handle page up and page down, too.
        else -> false
    }
}

private fun InteractiveScope.moveCursorByChars(cursorPosition: CursorPosition, charOffset: Int): CursorPosition {
    val component = getComponent(cursorPosition.componentId)
    val newOffset = (cursorPosition.offset + charOffset)
        .coerceAtMost(component.textRange.end)
        .coerceAtLeast(component.textRange.start)
    return CursorPosition(component.id, newOffset)
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
    val oldComponent = getComponent(oldPosition.componentId)
    val oldTextLayout = oldComponent.textLayoutResult

    if (oldTextLayout == null || !oldComponent.hasText) {
        // TODO: find component below the old one. Will be useful for tables.
        return CursorPosition(requireComponentLayout().componentNextOnLineTo(oldComponent).id, 0)
    }

    val oldLine = oldTextLayout.getLineForOffset(oldPosition.offset)
    val oldLineOffset = oldPosition.offset - oldTextLayout.getLineStart(oldLine)
    val oldWasLastLine = oldLine == oldTextLayout.lineCount - 1

    val newComponent: InteractiveComponent = if (oldWasLastLine) {
        requireComponentLayout().componentNextOnLineTo(oldComponent) // TODO: find component below the old one. Will be useful for tables.
    } else {
        oldComponent
    }
    // TODO: Move to next line

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
    return CursorPosition(newComponent.id, newOffset)
}

private fun InteractiveScope.moveCursorUp(oldPosition: CursorPosition): CursorPosition {
    val oldComponent = getComponent(oldPosition.componentId)

    val oldTextLayout = oldComponent.textLayoutResult
    if (oldTextLayout == null) {
        // TODO: find component above the old one. Will be useful for tables.
        return CursorPosition(requireComponentLayout().componentPreviousOnLineFrom(oldComponent).id, 0)
    } else {
        val oldLine = oldTextLayout.getLineForOffset(oldPosition.offset)
        val oldLineOffset = oldPosition.offset - oldTextLayout.getLineStart(oldLine)
        val oldWasFirstLine = oldLine == 0
        val newComponent: InteractiveComponent = if (oldWasFirstLine) {
            requireComponentLayout().componentPreviousOnLineFrom(oldComponent) // TODO: find component above the old one. Will be useful for tables.
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
        return CursorPosition(newComponent.id, newOffset)
    }
}