package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import co.touchlab.kermit.Logger
import me.okonecny.interactivetext.*
import java.nio.file.Path
import kotlin.math.abs


/**
 * Just like MarkdownView, but editable.
 */
@Composable
fun MarkdownEditor(
    sourceText: String,
    basePath: Path,
    interactiveScope: InteractiveScope,
    modifier: Modifier = Modifier,
    showSource: Boolean = false,
    documentTheme: DocumentTheme = DocumentTheme.default,
    scrollable: Boolean = true,
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList(),
    linkHandlers: List<LinkHandler> = emptyList(),
    onChange: (String) -> Unit
) {
    var sourceCursorRequest: (() -> Unit)? by remember { mutableStateOf(null) }
    var visualCursor by interactiveScope.cursorPosition
    var visualSelection by interactiveScope.selection
    var sourceCursor by remember { mutableStateOf<Int?>(null) }
    val clipboardManager = LocalClipboardManager.current

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        modifier = Modifier.onKeyEvent { keyEvent: KeyEvent -> Logger.d(keyEvent.toString()); false },
        onCursorMovement = { newVisualCursor ->
            visualCursor = newVisualCursor
            if (!newVisualCursor.isValid) return@InteractiveContainer
            val componentUnderCursor = interactiveScope.getComponent(newVisualCursor.componentId)
            val newSourceCursor = componentUnderCursor.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
            sourceCursor = newSourceCursor?.start
        },
        onInput = { textInputCommand ->
            if (!visualCursor.isValid && textInputCommand.needsValidCursor) return@InteractiveContainer
            val sourceSelection = computeSourceSelection(visualSelection, interactiveScope.requireComponentLayout())
            val sourceEditor = SourceEditor(sourceText, sourceCursor ?: return@InteractiveContainer, sourceSelection)

            val editedSourceEditor = when (textInputCommand) {
                Copy -> {
                    clipboardManager.setText(AnnotatedString(sourceEditor.selectedText))
                    sourceEditor
                }

                Paste -> sourceEditor.type(clipboardManager.getText()?.text ?: "")
                is Delete -> {
                    when (textInputCommand.size) {
                        Delete.Size.LETTER -> when (textInputCommand.direction) {
                            Delete.Direction.BEFORE_CURSOR -> sourceEditor.deleteLetterBeforeCursor()
                            Delete.Direction.AFTER_CURSOR -> sourceEditor.deleteLetterAfterCursor()
                        }

                        Delete.Size.WORD -> when (textInputCommand.direction) {
                            Delete.Direction.BEFORE_CURSOR -> sourceEditor.deleteWordBeforeCursor()
                            Delete.Direction.AFTER_CURSOR -> sourceEditor.deleteWordAfterCursor()
                        }
                    }
                }

                NewLine -> sourceEditor.typeNewLine()
                is Type -> sourceEditor.type(textInputCommand.text)
                is ReplaceRange -> sourceEditor.replaceRange(textInputCommand.sourceRange, textInputCommand.newSource)
            }
            if (editedSourceEditor.hasChangedWrt(sourceEditor)) {
                visualSelection = Selection.empty
                sourceCursorRequest = {
                    sourceCursor = editedSourceEditor.sourceCursor
                }
                onChange(editedSourceEditor.sourceText)
            }
        }
    ) {
        if (showSource) {
            Row(modifier) {
                MarkdownView(
                    sourceText,
                    basePath,
                    modifier = Modifier.weight(0.5f),
                    documentTheme,
                    scrollable,
                    codeFenceRenderers,
                    linkHandlers
                )
                val debuggingCursor = sourceCursor ?: 0
                BasicTextField(
                    value = TextFieldValue(
                        text = sourceText,
                        selection = TextRange(debuggingCursor, debuggingCursor + 1)
                    ),
                    onValueChange = {},
                    modifier = Modifier.weight(0.5f)
                )
            }
        } else {
            MarkdownView(sourceText, basePath, modifier, documentTheme, scrollable, codeFenceRenderers)
        }
        LaunchedEffect(sourceText) {
            if (interactiveScope.isPlaced) {
                sourceCursorRequest?.apply {
                    invoke()
                    sourceCursorRequest = null
                    visualCursor =
                        computeVisualCursor(sourceCursor ?: return@apply, interactiveScope.requireComponentLayout())
                }
            }
        }
    }
}

private fun computeSourceSelection(
    selection: Selection,
    interactiveComponentLayout: InteractiveComponentLayout
): TextRange {
    if (selection.isEmpty) return TextRange.Zero
    val (startMapping, endMapping) = listOf(selection.start.componentId, selection.end.componentId)
        .map(interactiveComponentLayout::getComponent)
        .map(InteractiveComponent::textMapping)
    return TextRange(
        startMapping.toSource(TextRange(selection.start.visualOffset))?.start ?: 0,
        endMapping.toSource(TextRange(selection.end.visualOffset))?.end ?: 0
    )
}

private fun computeVisualCursor(sourceCursor: Int, layout: InteractiveComponentLayout): CursorPosition {
    val componentAtCursor = layout.componentAtSource(sourceCursor)
    val cursorVisualRange = componentAtCursor.textMapping.toVisual(TextRange(sourceCursor))
    if (cursorVisualRange != null) return CursorPosition(componentAtCursor.id, cursorVisualRange.start)

    // Decide if start or end is closer to the source cursor pos.
    val componentSourceRange = componentAtCursor.textMapping.coveredSourceRange
    val visualOffset = if (componentSourceRange == null) {
        componentAtCursor.visualTextRange.start
    } else {
        if (abs(componentSourceRange.start - sourceCursor) <= abs(componentSourceRange.end - sourceCursor)) {
            componentAtCursor.visualTextRange.start
        } else {
            componentAtCursor.visualTextRange.end
        }
    }
    return CursorPosition(componentAtCursor.id, visualOffset)
}