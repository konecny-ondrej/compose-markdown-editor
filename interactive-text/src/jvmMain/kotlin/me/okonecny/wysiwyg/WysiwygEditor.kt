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
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.ast.serializers.NodeToEmptyAnnotatedString
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers
import me.okonecny.wysiwyg.edit.CommandEditors

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
    commandEditors: CommandEditors<D> = CommandEditors.basic(
        LocalClipboardManager.current,
        VisualNodeSerializers<D, AnnotatedString>()
            .withUnknownNodeSerializer(NodeToEmptyAnnotatedString())
    ),
    components: @Composable WysiwygEditorScope.() -> Unit
) {
    val interactiveScope = editorState.interactiveScope

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

    fun moveCursor() {
        val request = editorState.visualCursorRequest ?: return
        if (request is SetCursor) {
            val requestedCursor = request.newPosition
            if (interactiveScope.hasComponent(requestedCursor.componentId)) {
                editorState.visualCursor = request.newPosition
            } else {
                val requestedNode = editorState.visualDocument
                    .findChildById(requestedCursor.componentId)
                val renderedNode = requestedNode
                    ?.findClosestParentMatching { interactiveScope.hasComponent(it.interactiveId) }
                    ?: return

                editorState.visualCursor = CursorPosition(
                    renderedNode.interactiveId,
                    renderedNode.findOffsetByTextChild(requestedNode, requestedCursor.visualOffset)
                )
            }

            editorState.visualSelection = Selection.empty
            onChange(editorState.copy(visualCursorRequest = null))
        }
        if (request !is MoveCursorOnLine) return
        val oldCursorPosition = editorState.nodeCursor ?: return

        var currentNode = oldCursorPosition.textNodeUnderCursor.node
        var currentCharOffset = oldCursorPosition.textNodeUnderCursor.charOffset
        var renderedContainerNode: VisualNode<*, *> = oldCursorPosition.containerNode
        var currentVisualOffset = oldCursorPosition.visualOffset
        if (request.steps > 0) {
            for (i in 1..request.steps) {
                if (currentCharOffset == currentNode.data.text.length) {
                    currentNode = currentNode.findNext<HasText>() ?: return
                    currentCharOffset = 0
                }
                currentCharOffset++
                currentVisualOffset++
            }
        } else if (request.steps < 0) {
            for (i in 1..-request.steps) {
                if (currentCharOffset == 0) {
                    currentNode = currentNode.findPrev<HasText>() ?: return
                    currentCharOffset = currentNode.data.text.length
                    renderedContainerNode = currentNode
                    while (!interactiveScope.hasComponent(renderedContainerNode.interactiveId)) {
                        renderedContainerNode = renderedContainerNode.parent ?: return
                    }
                    currentVisualOffset = renderedContainerNode.totalTextLength
                } else {
                    currentCharOffset--
                    currentVisualOffset--
                }
            }
        }

        editorState.visualCursor = CursorPosition(
            renderedContainerNode.interactiveId,
            currentVisualOffset
        )
        editorState.visualSelection = Selection.empty
        onChange(editorState.copy(visualCursorRequest = null))
    }
    moveCursor()

    LaunchedEffect(inputQueue.firstOrNull(), inputQueue.size, editorState.visualCursorRequest) {
        if (editorState.visualCursorRequest != null) return@LaunchedEffect
        val textInputCommand = inputQueue.removeFirstOrNull() ?: return@LaunchedEffect
        if (editorState.visualCursor == null && textInputCommand.needsValidCursor) return@LaunchedEffect

        when (textInputCommand) {
//
//            Paste -> TODO()
            is MoveCursorOnLine -> {
                onChange(editorState.copy(visualCursorRequest = textInputCommand))
            }

            is SetCursor -> {
                onChange(editorState.copy(visualCursorRequest = textInputCommand))
            }

            else -> onChange(
                commandEditors.forCommand(textInputCommand).edit(editorState, textInputCommand) ?: return@LaunchedEffect
            )
        }
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
    val undoManager: UndoManager<D> = UndoManager(),
    val sourceCursor: Int? = null, // TODO: remove
    val sourceCursorRequest: Int? = null, // TODO: remove
    val visualCursorRequest: CursorMoveCommand? = null
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

    val nodeSelection: VisualNodeSelection<D>?
        get() {
            val selection = visualSelection
            if (selection.isEmpty) return null
            return VisualNodeSelection(
                VisualNodeCursorPosition(
                    containerNode = visualDocument.findChildById(selection.start.componentId) ?: return null,
                    visualOffset = selection.start.visualOffset
                ),
                VisualNodeCursorPosition(
                    containerNode = visualDocument.findChildById(selection.end.componentId) ?: return null,
                    visualOffset = selection.end.visualOffset
                )
            )
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