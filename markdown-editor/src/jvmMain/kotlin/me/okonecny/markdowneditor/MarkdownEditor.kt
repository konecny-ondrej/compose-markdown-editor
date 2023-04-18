package me.okonecny.markdowneditor

import androidx.compose.runtime.*
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
    var visualCursor by interactiveScope.cursorPosition
    val selection by interactiveScope.selection
    var sourceCursor by remember { mutableStateOf(TextRange(0)) }
    var cursorRequest: (() -> Unit)? by remember { mutableStateOf(null) }

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        onCursorMovement = { newVisualCursor ->
            visualCursor = newVisualCursor
            if (!newVisualCursor.isValid) return@InteractiveContainer
            val componentUnderCursor = interactiveScope.getComponent(newVisualCursor.componentId)
            val newSourceCursor = componentUnderCursor.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
            sourceCursor = newSourceCursor ?: sourceCursor
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
        LaunchedEffect(sourceText) {
            if (interactiveScope.isPlaced) {
                cursorRequest?.apply {
                    invoke()
                    cursorRequest = null
                    visualCursor = interactiveScope.requireComponentLayout().computeVisualCursor(sourceCursor)
                }
            }
        }
    }
}

private fun InteractiveComponentLayout.computeVisualCursor(sourceCursor: TextRange): CursorPosition {
    val componentAtCursor = componentAtSource(sourceCursor.start)
    val cursorVisualRange = componentAtCursor.textMapping.toVisual(sourceCursor)
    if (cursorVisualRange != null) return CursorPosition(componentAtCursor.id, cursorVisualRange.start)

    val componentSourceRange = componentAtCursor.textMapping.coveredSourceRange ?: TextRange.Zero
    val bestComponent =
        if (abs(componentSourceRange.start - sourceCursor.start) <= abs(componentSourceRange.end - sourceCursor.end)) {
            componentPreviousOnLineFrom(componentAtCursor)
        } else {
            componentNextOnLineTo(componentAtCursor)
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