package me.okonecny.wysiwyg

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.DpOffset
import androidx.constraintlayout.compose.ConstraintLayout
import me.okonecny.interactivetext.*
import kotlin.math.abs

/**
 * Flexible Wysiwyg editor for editing plaintext-based document formats, like HTML or Markdown.
 */
@Composable
fun WysiwygEditor(
    editorState: WysiwygEditorState,
    modifier: Modifier = Modifier,
    selectionStyle: SelectionStyle,
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

    val editorScope = WysiwygEditorScopeImpl()
    editorScope.components()

    Box {
        InteractiveContainer(
            scope = interactiveScope,
            selectionStyle = selectionStyle,
            modifier = modifier,
            onCursorMovement = { newVisualCursor ->
                editorState.visualCursor = newVisualCursor
                val component = interactiveScope.componentUnderCursor ?: return@InteractiveContainer
                val newSourceCursor = component.textMapping.toSource(TextRange(newVisualCursor.visualOffset))
                onChange(editorState.copy(sourceCursor = newSourceCursor?.start))
            },
            onInput = inputQueue::add
        ) {
            editorScope.view()
        }

        val visualCursorRect = editorState.visualCursorRect
        if (sourceCursor != null && visualCursorRect != null) {
            ConstraintLayout {
                val (toolbar, autocompletePopup) = createRefs()
                val toolbarOffset = with(LocalDensity.current) {
                    DpOffset(visualCursorRect.left.toDp(), visualCursorRect.top.toDp())
                }
                val autocompletePopupOffset = with(LocalDensity.current) {
                    DpOffset(visualCursorRect.left.toDp(), visualCursorRect.bottom.toDp())
                }

                Box(Modifier.constrainAs(toolbar) {
                    bottom.linkTo(parent.top) // So the base position is computed using bottom, not top.
                    translationX = toolbarOffset.x
                    translationY = toolbarOffset.y
                }) {
                    editorScope.toolbar(inputQueue::add)
                }

                Box(Modifier.constrainAs(autocompletePopup) {
                    translationX = autocompletePopupOffset.x
                    translationY = autocompletePopupOffset.y
                }) {
                    AutocompletePopup(
                        editorState,
                        autocompletePlugins,
                        inputQueue::add
                    )
                }
            }

        } else {
            LaunchedEffect(Unit) {
                editorState.interactiveScope.focusRequester.requestFocus()
            }
        }
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
            if (editorState.visualCursor == null && textInputCommand.needsValidCursor) break
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
                editorState.visualCursor = CursorPosition.invalid
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
    fun View(view: @Composable () -> Unit)

    @Composable
    fun Toolbar(toolbar: @Composable (handleInput: (TextInputCommand) -> Unit) -> Unit)
}

private class WysiwygEditorScopeImpl : WysiwygEditorScope {
    lateinit var view: @Composable (() -> Unit)
    var toolbar: @Composable (handleInput: (TextInputCommand) -> Unit) -> Unit = {}

    @Composable
    override fun View(view: @Composable () -> Unit) {
        this.view = view
    }

    @Composable
    override fun Toolbar(toolbar: @Composable (handleInput: (TextInputCommand) -> Unit) -> Unit) {
        this.toolbar = toolbar
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
    var visualCursor by interactiveScope::cursorPosition
    var visualSelection by interactiveScope::selection

    val visualCursorRect: Rect?
        get() {
            if (!interactiveScope.isPlaced) return null
            val cursor = visualCursor ?: return null
            return interactiveScope.cursorVisualRect(cursor)
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