package me.okonecny.markdowneditor

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import co.touchlab.kermit.Logger
import me.okonecny.interactivetext.*
import kotlin.math.abs


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
    var cursorRequest: (() -> Unit)? by remember { mutableStateOf(null) }
    var visualCursor by interactiveScope.cursorPosition
    var visualSelection by interactiveScope.selection
    var sourceCursor by remember { mutableStateOf(TextRange(0)) }

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        modifier = Modifier.onKeyEvent { keyEvent: KeyEvent -> Logger.d(keyEvent.toString()); false },
        onCursorMovement = { newVisualCursor ->
            visualCursor = newVisualCursor
            if (!newVisualCursor.isValid) return@InteractiveContainer
            val componentUnderCursor = interactiveScope.getComponent(newVisualCursor.componentId)
            val newSourceCursor = componentUnderCursor.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
            sourceCursor = newSourceCursor ?: sourceCursor
        },
        onInput = { textInputCommand ->
            if (!visualCursor.isValid) return@InteractiveContainer
            val sourceSelection = computeSourceSelection(visualSelection, interactiveScope.requireComponentLayout())
            val sourceEditor = SourceEditor(sourceText, sourceCursor, sourceSelection)

            val editedSourceEditor = when (textInputCommand) {
                Copy -> TODO()
                Paste -> TODO()
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
            }
            if (editedSourceEditor.hasChangedWrt(sourceEditor)) {
                cursorRequest = {
                    sourceCursor = editedSourceEditor.sourceCursor
                    visualSelection = Selection.empty
                }
                onChange(editedSourceEditor.sourceText)
            }
        }
    ) {
        MarkdownView(sourceText, documentTheme, scrollable, codeFenceRenderers)
        LaunchedEffect(sourceText) {
            if (interactiveScope.isPlaced) {
                cursorRequest?.apply {
                    invoke()
                    cursorRequest = null
                    visualCursor = computeVisualCursor(sourceCursor, interactiveScope.requireComponentLayout())
                }
            }
        }
    }
}

private fun computeSourceSelection(selection: Selection, interactiveComponentLayout: InteractiveComponentLayout): TextRange {
    if (selection.isEmpty) return TextRange.Zero
    val (startMapping, endMapping) = listOf(selection.start.componentId, selection.end.componentId)
        .map(interactiveComponentLayout::getComponent)
        .map(InteractiveComponent::textMapping)
    return TextRange(
        startMapping.toSource(TextRange(selection.start.visualOffset))?.start ?: 0,
        endMapping.toSource(TextRange(selection.end.visualOffset))?.end ?: 0
    )
}

private fun computeVisualCursor(sourceCursor: TextRange, layout: InteractiveComponentLayout): CursorPosition {
    val componentAtCursor = layout.componentAtSource(sourceCursor.start)
    val cursorVisualRange = componentAtCursor.textMapping.toVisual(sourceCursor)
    if (cursorVisualRange != null) return CursorPosition(componentAtCursor.id, cursorVisualRange.start)

    val componentSourceRange = componentAtCursor.textMapping.coveredSourceRange ?: TextRange.Zero
    val bestComponent =
        if (abs(componentSourceRange.start - sourceCursor.start) <= abs(componentSourceRange.end - sourceCursor.end)) {
            layout.componentPreviousOnLineFrom(componentAtCursor)
        } else {
            layout.componentNextOnLineTo(componentAtCursor)
        }

    // Decide if start or end is closer to the source cursor pos.
    val bestComponentSourceRange = bestComponent.textMapping.coveredSourceRange
    val visualOffset = if (bestComponentSourceRange == null) {
        bestComponent.visualTextRange.start
    } else {
        if (abs(bestComponentSourceRange.start - sourceCursor.start) <= abs(bestComponentSourceRange.end - sourceCursor.end)) {
            bestComponent.visualTextRange.start
        } else {
            bestComponent.visualTextRange.end
        }
    }
    return CursorPosition(bestComponent.id, visualOffset)
}