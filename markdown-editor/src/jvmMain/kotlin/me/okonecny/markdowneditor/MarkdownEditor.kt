package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.*
import me.okonecny.markdowneditor.inline.annotatedString
import me.okonecny.markdowneditor.inline.isMaybeEmojiStart
import me.okonecny.markdowneditor.inline.unicodeString
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
    val clipboardManager = LocalClipboardManager.current
    var sourceCursorRequest: (() -> Unit)? by remember { mutableStateOf(null) }
    var visualCursor by interactiveScope.cursorPosition
    var visualSelection by interactiveScope.selection
    var sourceCursor by remember { mutableStateOf<Int?>(null) }
    val componentUnderCursor: InteractiveComponent? by remember {
        derivedStateOf {
            if (!interactiveScope.isPlaced || !visualCursor.isValid) null else {
                interactiveScope.getComponent(visualCursor.componentId)
            }
        }
    }
    val nodeUnderCursor: Node? by remember {
        derivedStateOf {
            val cursor = sourceCursor ?: return@derivedStateOf null
            val component = componentUnderCursor ?: return@derivedStateOf null
            if (!component.hasData<Node>()) return@derivedStateOf null
            val componentNode = component[Node::class]
            return@derivedStateOf componentNode.descendants.minByOrNull { child ->
                val range = child.sourceRange
                if (range.contains(cursor)) range.span else Int.MAX_VALUE
            }
        }
    }
    val contextWord: String = remember(sourceCursor) {
        sourceCursor?.let { sourceText.wordBefore(it) } ?: ""
    }
    val emojiSuggestions by remember(contextWord) {
        derivedStateOf {
            if (!contextWord.isMaybeEmojiStart()) return@derivedStateOf emptyList()
            val emojiNamePrefix = contextWord.substring(1)
            if (emojiNamePrefix.isEmpty()) return@derivedStateOf emptyList()
            EmojiReference.getEmojiList()
                .filter { it.shortcut?.startsWith(emojiNamePrefix) ?: false }
                .filter { it.unicodeString.isNotEmpty() }
                .take(5)
        }
    }

    var autocompleteState by remember(emojiSuggestions) {
        mutableStateOf(AutocompleteState(emojiSuggestions) { emoji -> ":" + emoji.shortcut + ":" })
    }

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        modifier = Modifier.autocompleteNavigation(autocompleteState) { newState ->
            autocompleteState = newState
        },
        onCursorMovement = { newVisualCursor ->
            visualCursor = newVisualCursor
            if (!newVisualCursor.isValid) return@InteractiveContainer
            val component = componentUnderCursor ?: return@InteractiveContainer
            val newSourceCursor = component.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
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

                NewLine -> {
                    if (autocompleteState.isEmpty()) {
                        sourceEditor.typeNewLine()
                    } else {
                        sourceEditor.type(autocompleteState.remainingText(contextWord))
                    }
                }

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
        WithOptionalSourceView(showSource, sourceText, sourceCursor, modifier) { contentModifier ->
            Box(contentModifier) {
                MarkdownView(
                    sourceText,
                    basePath,
                    Modifier.fillMaxSize(1f),
                    documentTheme,
                    scrollable,
                    codeFenceRenderers,
                    linkHandlers
                )

                AutocompletePopup(
                    visualCursor,
                    interactiveScope,
                    autocompleteState
                ) { emoji ->
                    Text(emoji.annotatedString)
                    Spacer(Modifier.width(3.dp))
                    Text(":${emoji.shortcut}:")
                }

            }

        }
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

@Composable
private fun WithOptionalSourceView(
    showSource: Boolean,
    sourceText: String,
    sourceCursor: Int?,
    modifier: Modifier,
    content: @Composable (Modifier) -> Unit
) {
    if (showSource) {
        Row(modifier) {
            content(Modifier.weight(0.5f))
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
        content(modifier)
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