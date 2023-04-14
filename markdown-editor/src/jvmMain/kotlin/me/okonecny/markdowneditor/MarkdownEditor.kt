package me.okonecny.markdowneditor

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import co.touchlab.kermit.Logger
import me.okonecny.interactivetext.*

var cursorRequest: (() -> Unit)? by mutableStateOf(null)

/**
 * Just like MarkdownView, but editable.
 */
@Composable
fun MarkdownEditor(
    sourceText: String,
    interactiveScope: InteractiveScope,
    documentTheme: DocumentTheme = DocumentTheme.default,
    scrollable: Boolean = true,
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList(),
    onChange: (String) -> Unit
) {
    var visualCursor by interactiveScope.cursorPosition
    val selection by interactiveScope.selection
    var sourceCursor by remember { mutableStateOf(TextRange(0)) }

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        onCursorMovement = { newVisualCursor ->
            visualCursor = newVisualCursor
            if (!newVisualCursor.isValid) return@InteractiveContainer
            val componentUnderCursor = interactiveScope.getComponent(newVisualCursor.componentId)
            sourceCursor = componentUnderCursor.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
        },
        onInput = { textInputCommand ->
            if (!visualCursor.isValid) return@InteractiveContainer
            Logger.d("$visualCursor", tag = "Cursor")
            Logger.d(
                "$textInputCommand@$sourceCursor '${sourceText[sourceCursor.start]}'",
                tag = "onInput"
            )
            when (textInputCommand) {
                Copy -> TODO()
                is Delete -> {
                    val size = when (textInputCommand.size) {
                        Delete.Size.LETTER -> 1
                        Delete.Size.WORD -> when (textInputCommand.direction) {
                            Delete.Direction.BEFORE_CURSOR -> ("\\s".toRegex()
                                .find(sourceText.substring(0, sourceCursor.start).reversed())?.range?.first
                                ?: sourceCursor.end) + 1

                            Delete.Direction.AFTER_CURSOR -> ("\\s".toRegex()
                                .find(sourceText, sourceCursor.end + 1)?.range?.first
                                ?: sourceText.length) - sourceCursor.start
                        }
                    }
                    val newSourceText: String = when (textInputCommand.direction) {
                        Delete.Direction.BEFORE_CURSOR -> sourceText.substring(
                            0,
                            (sourceCursor.start - size).coerceAtLeast(0)
                        ) + sourceText.substring(sourceCursor.end)

                        Delete.Direction.AFTER_CURSOR -> sourceText.substring(
                            0,
                            sourceCursor.start
                        ) + sourceText.substring(sourceCursor.start + size)
                    }

                    if (textInputCommand.direction == Delete.Direction.BEFORE_CURSOR) {
                        cursorRequest = {
                            sourceCursor = TextRange((sourceCursor.start - size).coerceAtLeast(0))
                        }
                    }
                    // TODO Delete selection if there is some.
                    onChange(newSourceText)
                }

                NewLine -> {
                    val newSourceText = sourceText.substring(0, sourceCursor.start) +
                            System.lineSeparator() + System.lineSeparator() +
                            sourceText.substring(sourceCursor.end)
                    onChange(newSourceText)
                    cursorRequest = {
                        sourceCursor = TextRange(sourceCursor.start + 2)
                    }
                }

                Paste -> TODO()
                is Type -> {
                    val newSourceText = sourceText.substring(0, sourceCursor.start) +
                            textInputCommand.text +
                            sourceText.substring(sourceCursor.start) // Typing intentionally replaces just one character.
                    onChange(newSourceText)
                    cursorRequest = {
                        sourceCursor = TextRange(sourceCursor.start + 1)
                    }
                }
            }
        }
    ) {
        MarkdownView(sourceText, documentTheme, scrollable, codeFenceRenderers)
        if (interactiveScope.isPlaced) {
            val layout = interactiveScope.requireComponentLayout()
            LaunchedEffect(layout) {
                cursorRequest?.invoke()
                cursorRequest = null
                visualCursor = layout.computeVisualCursor(sourceCursor)
            }
        }
    }
}

private fun InteractiveComponentLayout.computeVisualCursor(sourceCursor: TextRange): CursorPosition {
    val interactiveComponent = componentAtSource(sourceCursor.start)
    return CursorPosition(interactiveComponent.id, interactiveComponent.textMapping.toVisual(sourceCursor).start)
}