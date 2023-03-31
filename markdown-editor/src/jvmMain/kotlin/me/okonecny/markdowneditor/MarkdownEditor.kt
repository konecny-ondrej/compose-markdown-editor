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
            val selection by interactiveScope.selection
            val layout = interactiveScope.requireComponentLayout()

            val mapping = layout.getComponent(cursor.componentId).textMapping
            val sourceCursorPos = mapping.toSource(TextRange(cursor.visualOffset))
            Logger.d("$cursor", tag = "Cursor")
            Logger.d(
                "$textInputCommand@$sourceCursorPos '${sourceText[sourceCursorPos.start]}'",
                tag = "onInput"
            )
            when (textInputCommand) {
                Copy -> TODO()
                is Delete -> TODO()
                NewLine -> TODO()
                Paste -> TODO()
                is Type -> {
                    val newSourceText = sourceText.substring(0, sourceCursorPos.start) +
                            textInputCommand.text +
                            sourceText.substring(sourceCursorPos.end)
                    onChange(
                        newSourceText,
                        interactiveScope.moveCursorRight(cursor),
                        selection // TODO
                    )
                }
            }
        }
    ) {
        MarkdownView(sourceText, documentTheme, scrollable, codeFenceRenderers)
    }
}