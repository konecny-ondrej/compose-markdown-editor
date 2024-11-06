package me.okonecny.wysiwyg

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.constraintlayout.compose.ConstraintLayout
import me.okonecny.interactivetext.*
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition
import me.okonecny.wysiwyg.ast.data.HasText

/**
 * Flexible Wysiwyg editor for editing plaintext-based document formats, like HTML or Markdown.
 */
@Composable
fun <D : Any> WysiwygEditor(
    editorState: WysiwygEditorState<D>,
    modifier: Modifier = Modifier,
    selectionStyle: SelectionStyle,
    autocompletePlugins: List<AutocompletePlugin<D>>,
    onChange: (WysiwygEditorState<D>) -> Unit,
    components: @Composable WysiwygEditorScope.() -> Unit
) {
    val (
        sourceText,
        visualDocument,
        interactiveScope,
        undoManager,
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
            },
            onInput = inputQueue::add
        ) {
            editorScope.view()
        }

        val visualCursorRect = editorState.visualCursorRect
        if (visualCursorRect != null) {
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
//                    editorScope.toolbar(inputQueue::add)
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

    LaunchedEffect(inputQueue.firstOrNull(), inputQueue.size) {
        val textInputCommand = inputQueue.removeFirstOrNull() ?: return@LaunchedEffect
        if (editorState.visualCursor == null && textInputCommand.needsValidCursor) return@LaunchedEffect

        when (textInputCommand) {
            Copy -> {
                clipboardManager.setText(AnnotatedString("TODO"))
                TODO()
            }

            Cut -> {
                clipboardManager.setText(AnnotatedString("TODO"))
                TODO()
            }

            Paste -> TODO()
            is Delete -> {
                when (textInputCommand.size) {
                    Delete.Size.LETTER -> when (textInputCommand.direction) {
                        Delete.Direction.BEFORE_CURSOR -> TODO()
                        Delete.Direction.AFTER_CURSOR -> TODO()
                    }

                    Delete.Size.WORD -> when (textInputCommand.direction) {
                        Delete.Direction.BEFORE_CURSOR -> TODO()
                        Delete.Direction.AFTER_CURSOR -> TODO()
                    }
                }
            }

            NewLine -> TODO()
            is Type -> {
                val nodeCursor = editorState.nodeCursor ?: return@LaunchedEffect
                val editedNode = nodeCursor.node.findChildByDataType(HasText::class) ?: return@LaunchedEffect
                val editedText = editedNode.data.text
                val newState = editorState.copy(
                    visualDocument = editedNode.replaceWith(
                        editedNode.copy(
                            data = editedNode.data.replaceText(
                                editedText.substring(0, nodeCursor.visualOffset)
                                        + textInputCommand.text
                                        + editedText.substring(nodeCursor.visualOffset, editedText.length)
                            )
                        )
                    ).root
                )
                inputQueue.add(MoveCursorOnLine(textInputCommand.text.length))
                onChange(newState)
            }

            is Undo -> TODO()

            is Redo -> TODO()
            is ReplaceRange -> TODO("remove this")
            is MoveCursorOnLine -> {
                val oldCursorPosition = editorState.visualCursor ?: return@LaunchedEffect
                editorState.visualCursor = if (textInputCommand.steps > 0) {
                    interactiveScope.moveCursorRight(oldCursorPosition, textInputCommand.steps)
                } else {
                    interactiveScope.moveCursorLeft(oldCursorPosition, -textInputCommand.steps)
                }
                editorState.visualSelection = Selection.empty
            }
        }

        // TODO: register undo action
    }
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

data class WysiwygEditorState<D : Any>(
    val sourceText: String,
    val visualDocument: VisualNode<D, D>,
    val interactiveScope: InteractiveScope = InteractiveScope(),
    val undoManager: UndoManager = UndoManager(),
    val sourceCursor: Int? = null, // TODO: remove
    val sourceCursorRequest: Int? = null // TODO: remove
) {
    var visualCursor by interactiveScope::cursorPosition
    var visualSelection by interactiveScope::selection

    val nodeCursor: VisualNodeCursorPosition<D>?
        get() {
            val visualCursor = visualCursor ?: return null
            val interactiveId = visualCursor.componentId

            val nodes = mutableListOf<VisualNode<Any, D>>(visualDocument)
            while (nodes.isNotEmpty()) { // TODO: this is probably unnecessarily slow.
                val firstNode = nodes.removeFirst()
                if (firstNode.interactiveId == interactiveId) {
                    return VisualNodeCursorPosition(
                        firstNode,
                        visualCursor.visualOffset
                    )
                } else {
                    nodes.addAll(firstNode.children)
                }
            }
            return null
        }

    val visualCursorRect: Rect?
        get() {
            if (!interactiveScope.isPlaced) return null
            val cursor = visualCursor ?: return null
            return interactiveScope.cursorVisualRect(cursor)
        }
}

@Composable
fun <D : Any> rememberWysiwygEditorState(
    initialSourceText: String,
    visualDocument: VisualNode<D, D>,
    vararg keys: Any?
) = remember(keys) {
    mutableStateOf(
        WysiwygEditorState(
            sourceText = initialSourceText,
            visualDocument = visualDocument
        )
    )
}