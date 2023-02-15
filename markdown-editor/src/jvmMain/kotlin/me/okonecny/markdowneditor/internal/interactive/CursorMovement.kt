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


private fun InteractiveScope.moveCursorLeft(oldPosition: CursorPosition): CursorPosition {
    val oldComponent = getComponent(oldPosition.componentId)

    val newComponent: InteractiveComponent = if (oldPosition.offset == oldComponent.textRange.start) {
        prevTo(oldComponent)
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
    return CursorPosition(newComponent.id, newOffset)
}

private fun InteractiveScope.moveCursorRight(oldPosition: CursorPosition): CursorPosition {
    val oldComponent = getComponent(oldPosition.componentId)

    val newComponent: InteractiveComponent = if (oldPosition.offset == oldComponent.textRange.end) {
        nextTo(oldComponent)
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
    return CursorPosition(newComponent.id, newOffset)
}

private fun InteractiveScope.moveCursorDown(oldPosition: CursorPosition): CursorPosition {
    val oldComponent = getComponent(oldPosition.componentId)
    val oldTextLayout = oldComponent.textLayoutResult

    if (oldTextLayout == null || !oldComponent.hasText) {
        // TODO: find component below the old one. Will be useful for tables.
        return CursorPosition(nextTo(oldComponent).id, 0)
    }

    val oldLine = oldTextLayout.getLineForOffset(oldPosition.offset)
    val oldLineOffset = oldPosition.offset - oldTextLayout.getLineStart(oldLine)
    val oldWasLastLine = oldLine == oldTextLayout.lineCount - 1

    val newComponent: InteractiveComponent = if (oldWasLastLine) {
        nextTo(oldComponent) // TODO: find component below the old one. Will be useful for tables.
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
        return CursorPosition(prevTo(oldComponent).id, 0)
    } else {
        val oldLine = oldTextLayout.getLineForOffset(oldPosition.offset)
        val oldLineOffset = oldPosition.offset - oldTextLayout.getLineStart(oldLine)
        val oldWasFirstLine = oldLine == 0
        val newComponent: InteractiveComponent = if (oldWasFirstLine) {
            prevTo(oldComponent) // TODO: find component above the old one. Will be useful for tables.
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