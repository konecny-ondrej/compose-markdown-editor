package me.okonecny.markdowneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextRange
import co.touchlab.kermit.Logger
import me.okonecny.interactivetext.*

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
    onChange: (String, CursorPosition, Selection) -> Unit
) {
    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        onInput = { textInputCommand ->
            val cursor by interactiveScope.cursorPosition
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
                    val size = when(textInputCommand.size) {
                        Delete.Size.LETTER -> 1
                        Delete.Size.WORD -> when(textInputCommand.direction) {
                            Delete.Direction.BEFORE_CURSOR -> ("\\s".toRegex()
                                .find(sourceText.substring(0, sourceCursorPos.start).reversed())?.range?.first
                                ?: sourceCursorPos.end) + 1
                            Delete.Direction.AFTER_CURSOR -> ("\\s".toRegex()
                                .find(sourceText, sourceCursorPos.end + 1)?.range?.first
                                ?: sourceText.length) - sourceCursorPos.start
                        }
                    }
                    val newSourceText: String = when(textInputCommand.direction) {
                        Delete.Direction.BEFORE_CURSOR -> sourceText.substring(0, sourceCursorPos.start - size) + sourceText.substring(sourceCursorPos.end)
                        Delete.Direction.AFTER_CURSOR -> sourceText.substring(0, sourceCursorPos.start) + sourceText.substring(sourceCursorPos.start + size)
                    }

                    // TODO Delete selection if there is some.
                    // TODO Move cursor
                    onChange(newSourceText, cursor, selection /* TODO */)
                }

                NewLine -> {
                    val newSourceText = sourceText.substring(0, sourceCursorPos.start) +
                            System.lineSeparator() + System.lineSeparator() +
                            sourceText.substring(sourceCursorPos.end)
                    onChange(newSourceText, cursor, selection /* TODO */)
                }

                Paste -> TODO()
                is Type -> {
                    val newSourceText = sourceText.substring(0, sourceCursorPos.start) +
                            textInputCommand.text +
                            sourceText.substring(sourceCursorPos.end)
                    onChange(
                        newSourceText,
                        cursor.copy(visualOffset = cursor.visualOffset + 1),
//                        interactiveScope.moveCursorRight(cursor),
                        selection // TODO
                    )
                }
            }
        }
    ) {
        MarkdownView(sourceText, documentTheme, scrollable, codeFenceRenderers)
    }
}