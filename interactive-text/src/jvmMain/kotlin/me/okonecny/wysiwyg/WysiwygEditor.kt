package me.okonecny.wysiwyg

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import me.okonecny.interactivetext.*
import kotlin.math.abs


/**
 * Flexible Wysiwyg editor for editing plaintext-based document formats, like HTML or Markdown.
 */
@Composable
fun WysiwygEditor(
    editorState: WysiwygEditorState,
    modifier: Modifier = Modifier,
    selectionStyle: TextStyle,
    autocompletePlugins: List<AutocompletePlugin>,
    onChange: (WysiwygEditorState) -> Unit,
    components: @Composable WysiwygEditorScope.() -> Unit
) {
    val (
        sourceText,
        interactiveScope,
        undoManager,
        sourceCursor,
        sourceCursorRequest
    ) = editorState

    val clipboardManager = LocalClipboardManager.current
    val inputQueue = remember { mutableStateListOf<TextInputCommand>() }

    InteractiveContainer(
        scope = interactiveScope,
        selectionStyle = selectionStyle,
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
        val editorScope = WysiwygEditorScopeImpl()
        editorScope.components()
        editorScope.view!!()
        editorScope.floatingToolbar()

        AutocompletePopup(
            editorState,
            autocompletePlugins
        )
    }
    LaunchedEffect(sourceCursorRequest) {
        if (interactiveScope.isPlaced) {
            sourceCursorRequest?.apply {
                editorState.visualCursor = computeVisualCursor(
                    this,
                    interactiveScope
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

private fun computeVisualCursor(sourceCursor: Int, scope: InteractiveScope): CursorPosition {
    val componentAtCursor = scope.componentAtSource(sourceCursor)
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

interface WysiwygEditorScope {
    @Composable
    fun WysiwygView(view: @Composable () -> Unit)

    @Composable
    fun FloatingToolbar(toolbar: @Composable () -> Unit)

    // TODO: add SourceView?
    // TODO: implement proper WYSIWYG / source / both modes.
}

private class WysiwygEditorScopeImpl : WysiwygEditorScope {
    var view: @Composable (() -> Unit)? = null
        get() = if (field == null) throw IllegalStateException("You must set the View for the editor.") else field
    var floatingToolbar: @Composable () -> Unit = {}

    @Composable
    override fun WysiwygView(view: @Composable () -> Unit) {
        this.view = view
    }

    @Composable
    override fun FloatingToolbar(toolbar: @Composable () -> Unit) {
        this.floatingToolbar = toolbar
    }
}

// endregion dsl

data class WysiwygEditorState(
    val sourceText: String,
    val interactiveScope: InteractiveScope = InteractiveScope(),
    val undoManager: UndoManager = UndoManager(),
    val sourceCursor: Int? = null,
    val sourceCursorRequest: Int? = null
) {
    var visualCursor
        get() = interactiveScope.cursorPosition // TODO: replace the whole interactive scope instead of mutating?
        set(value) {
            interactiveScope.cursorPosition = value
        }
    var visualSelection
        get() = interactiveScope.selection
        set(value) {
            interactiveScope.selection = value
        }

    val visualCursorRect: Rect?
        get() =
            if (interactiveScope.isPlaced && visualCursor.isValid) {
                visualCursor.visualRect(interactiveScope)
            } else {
                null
            }
    val sourceSelection: TextRange
        get() =
            if (interactiveScope.isPlaced) {
                visualSelection.computeSourceSelection(interactiveScope)
            } else {
                TextRange.Zero
            }
}

@Composable
fun rememberWysiwygEditorState(initialSourceText: String, vararg keys: Any?) = remember(keys) {
    mutableStateOf(
        WysiwygEditorState(
            sourceText = initialSourceText
        )
    )
}