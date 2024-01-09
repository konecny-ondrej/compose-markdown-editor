package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import me.okonecny.interactivetext.*
import me.okonecny.markdowneditor.inline.annotatedString
import me.okonecny.markdowneditor.inline.isMaybeEmojiStart
import me.okonecny.markdowneditor.inline.unicodeString
import me.okonecny.markdowneditor.interactive.computeSourceSelection
import me.okonecny.markdowneditor.toolbar.FloatingTextToolbar
import kotlin.math.abs


/**
 * Just like MarkdownView, but editable.
 */
@Composable
fun MarkdownEditor(
    editorState: MarkdownEditorState,
    modifier: Modifier = Modifier,
    documentTheme: DocumentTheme = DocumentTheme.default,
    onChange: (MarkdownEditorState) -> Unit,
    components: @Composable MarkdownEditorScope.() -> Unit
) {
    val (
        sourceText,
        interactiveScope,
        undoManager,
        sourceCursor,
        sourceCursorRequest
    ) = editorState
    val contextWord = editorState.contextWord

    val clipboardManager = LocalClipboardManager.current
    val inputQueue = remember { mutableStateListOf<TextInputCommand>() }

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

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = documentTheme.styles.selection,
        modifier = modifier,
        onCursorMovement = { newVisualCursor ->
            editorState.visualCursor = newVisualCursor
            if (!newVisualCursor.isValid) return@InteractiveContainer
            val component = interactiveScope.componentUnderCursor ?: return@InteractiveContainer
            val newSourceCursor = component.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
            onChange(editorState.copy(sourceCursor = newSourceCursor?.start))
        },
        onInput = inputQueue::add
    ) {
        val editorScope = MarkdownEditorScopeImpl()
        editorScope.components()
        editorScope.view!!()
        if (interactiveScope.isPlaced) {
            FloatingTextToolbar(
                editorState.visualSelection,
                interactiveScope.requireComponentLayout(),
                editorState.visualCursorRect,
                sourceText,
                editorState.sourceSelection,
                sourceCursor
            )
        }

        val handleInput = LocalInteractiveInputHandler.current
        AutocompletePopup(
            editorState.visualCursorRect,
            emojiSuggestions,
            onClick = { clickedItem ->
                val emojiTag = ":" + emojiSuggestions[clickedItem].shortcut + ":"
                handleInput(Type(emojiTag.remainingText(contextWord)))
                interactiveScope.focusRequester.requestFocus()
            }
        ) { emoji ->
            Text(emoji.annotatedString)
            Spacer(Modifier.width(3.dp))
            Text(":${emoji.shortcut}:")
        }
    }
    LaunchedEffect(sourceCursorRequest) {
        if (interactiveScope.isPlaced) {
            sourceCursorRequest?.apply {
                editorState.visualCursor = computeVisualCursor(
                    this,
                    interactiveScope.requireComponentLayout()
                )
                onChange(
                    editorState.copy(
                        sourceCursor = sourceCursorRequest,
                        sourceCursorRequest = null
                    )
                )
            }
        }
    }
    LaunchedEffect(inputQueue.size) {
        for (i in inputQueue.lastIndex downTo 0) {
            val textInputCommand = inputQueue.removeAt(i)
            if (!editorState.visualCursor.isValid && textInputCommand.needsValidCursor) break
            val sourceEditor = SourceEditor(sourceText, sourceCursor ?: break, editorState.sourceSelection)

            var editedUndoManager = undoManager
            val editedSourceEditor = when (textInputCommand) {
                Copy -> {
                    clipboardManager.setText(AnnotatedString(sourceEditor.selectedText))
                    sourceEditor
                }

                Cut -> {
                    clipboardManager.setText(AnnotatedString(sourceEditor.selectedText))
                    sourceEditor.deleteSelection()
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
                is ReplaceRange -> sourceEditor.replaceRange(
                    textInputCommand.sourceRange,
                    textInputCommand.newSource,
                    textInputCommand.sourceCursorOffset
                )

                is Undo -> if (undoManager.hasHistory) {
                    editedUndoManager = undoManager.undo()
                    editedUndoManager.currentHistory
                } else sourceEditor

                is Redo -> if (undoManager.hasHistory) {
                    editedUndoManager = undoManager.redo()
                    editedUndoManager.currentHistory
                } else sourceEditor
            }

            if (undoManager == editedUndoManager) { // TODO: maybe don't add every letter types, but bigger chunks.
                if (!editedUndoManager.hasHistory) {
                    editedUndoManager = editedUndoManager.add(sourceEditor)
                }
                editedUndoManager = editedUndoManager.add(editedSourceEditor)
            }
            if (editedSourceEditor.hasChangedWrt(sourceEditor)) {
                editorState.visualSelection = Selection.empty
                onChange(
                    editorState.copy(
                        sourceText = editedSourceEditor.sourceText,
                        undoManager = editedUndoManager,
                        sourceCursor = null,
                        sourceCursorRequest = editedSourceEditor.sourceCursor
                    )
                )
            }
        }
    }
}

private fun computeVisualCursor(sourceCursor: Int, layout: InteractiveComponentLayout): CursorPosition {
    val componentAtCursor = layout.componentAtSource(sourceCursor)
    val cursorVisualRange = componentAtCursor.textMapping.toVisual(TextRange(sourceCursor))
    if (cursorVisualRange != null && cursorVisualRange.collapsed) return CursorPosition(
        componentAtCursor.id,
        cursorVisualRange.start
    )

    // Decide if start or end is closer to the source cursor pos.
    val componentSourceRange = componentAtCursor.textMapping.coveredSourceRange
    val visualOffset = if (componentSourceRange == null) {
        componentAtCursor.visualTextRange.start
    } else {
        val visualRange = cursorVisualRange ?: componentAtCursor.visualTextRange
        if (abs(componentSourceRange.start - sourceCursor) <= abs(componentSourceRange.end - sourceCursor)) {
            visualRange.start
        } else {
            visualRange.end
        }
    }
    return CursorPosition(componentAtCursor.id, visualOffset)
}

// region dsl

interface MarkdownEditorScope {
    @Composable
    fun WysiwygView(view: @Composable () -> Unit)

    // TODO: add Toolbar
    // TODO: add SourceView?
}

private class MarkdownEditorScopeImpl : MarkdownEditorScope {
    var view: @Composable (() -> Unit)? = null
        get() = if (field == null) throw IllegalStateException("You must set the View for the editor.") else field

    @Composable
    override fun WysiwygView(view: @Composable () -> Unit) {
        this.view = view
    }
}

// endregion dsl

data class MarkdownEditorState(
    val sourceText: String,
    val interactiveScope: InteractiveScope = InteractiveScope(),
    val undoManager: UndoManager = UndoManager(),
    val sourceCursor: Int? = null,
    val sourceCursorRequest: Int? = null
) {
    var visualCursor by interactiveScope.cursorPosition // TODO: replace the whole interactive scope instead of mutating?
    var visualSelection by interactiveScope.selection
    val visualCursorRect: Rect?
        get() =
            if (interactiveScope.isPlaced && visualCursor.isValid) {
                visualCursor.visualRect(interactiveScope.requireComponentLayout())
            } else {
                null
            }
    val sourceSelection: TextRange
        get() =
            if (interactiveScope.isPlaced) {
                visualSelection.computeSourceSelection(interactiveScope.requireComponentLayout())
            } else {
                TextRange.Zero
            }
    val contextWord: String by lazy {
        sourceCursor?.let { sourceText.wordBefore(it) } ?: ""
    }
}

@Composable
fun rememberMarkdownEditorState(initialSourceText: String, vararg keys: Any?) = remember(keys) {
    mutableStateOf(
        MarkdownEditorState(
            sourceText = initialSourceText
        )
    )
}