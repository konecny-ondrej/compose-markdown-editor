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
    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        onInput = { textInputCommand ->
            var cursor by interactiveScope.cursorPosition
            if (!cursor.isValid) return@InteractiveContainer

            val selection by interactiveScope.selection
            val layout = interactiveScope.requireComponentLayout()
            val component = layout.getComponent(cursor.componentId)
            val mapping = component.textMapping
            val sourceCursorPos = mapping.toSource(TextRange(cursor.visualOffset))
            Logger.d("$cursor", tag = "Cursor")
            Logger.d(
                "$textInputCommand@$sourceCursorPos '${sourceText[sourceCursorPos.start]}'",
                tag = "onInput"
            )
            when (textInputCommand) {
                Copy -> TODO()
                is Delete -> {
                    val size = when (textInputCommand.size) {
                        Delete.Size.LETTER -> 1
                        Delete.Size.WORD -> when (textInputCommand.direction) {
                            Delete.Direction.BEFORE_CURSOR -> ("\\s".toRegex()
                                .find(sourceText.substring(0, sourceCursorPos.start).reversed())?.range?.first
                                ?: sourceCursorPos.end) + 1

                            Delete.Direction.AFTER_CURSOR -> ("\\s".toRegex()
                                .find(sourceText, sourceCursorPos.end + 1)?.range?.first
                                ?: sourceText.length) - sourceCursorPos.start
                        }
                    }
                    val newSourceText: String = when (textInputCommand.direction) {
                        Delete.Direction.BEFORE_CURSOR -> sourceText.substring(
                            0,
                            (sourceCursorPos.start - size).coerceAtLeast(0)
                        ) + sourceText.substring(sourceCursorPos.end)

                        Delete.Direction.AFTER_CURSOR -> sourceText.substring(
                            0,
                            sourceCursorPos.start
                        ) + sourceText.substring(sourceCursorPos.start + size)
                    }

                    if (textInputCommand.direction == Delete.Direction.BEFORE_CURSOR) {
                        cursorRequest = {
                            for (i in 1..size) cursor = interactiveScope.moveCursorLeft(cursor)
                        }
                    }
                    // TODO Delete selection if there is some.
                    onChange(newSourceText)
                }

                NewLine -> {
                    val newSourceText = sourceText.substring(0, sourceCursorPos.start) +
                            System.lineSeparator() + System.lineSeparator() +
                            sourceText.substring(sourceCursorPos.end)
                    onChange(newSourceText)
                    cursorRequest = {
                        cursor = interactiveScope.moveCursorRight(cursor)
                    }
                }

                Paste -> TODO()
                is Type -> {
                    val newSourceText = sourceText.substring(0, sourceCursorPos.start) +
                            textInputCommand.text +
                            sourceText.substring(sourceCursorPos.start) // Typing intentionally replaces just one character.
                    onChange(newSourceText)
                    cursorRequest = {
                        cursor = interactiveScope.moveCursorRight(cursor)
                    }
                }
            }
        }
    ) {
        MarkdownView(sourceText, documentTheme, scrollable, codeFenceRenderers)
        if (interactiveScope.isPlaced) {
            LaunchedEffect(interactiveScope.requireComponentLayout()) {
                cursorRequest?.invoke()
                cursorRequest = null
            }
        }
    }
}