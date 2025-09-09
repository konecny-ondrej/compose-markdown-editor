package me.okonecny.wysiwyg

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.constraintlayout.compose.ConstraintLayout
import me.okonecny.interactivetext.*
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.NodeToEmptyAnnotatedString
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers
import me.okonecny.wysiwyg.ast.touchedNodesOfType
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
        clipboard = LocalClipboard.current,
        clipboardSerializers = VisualNodeSerializers<D, AnnotatedString>()
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
            onCursorMovement = { newVisualCursor, newSelection ->
                interactiveScope.cursorPosition = newVisualCursor
                interactiveScope.selection = newSelection
                val newNode =
                    editorState.visualDocument.findChildById(newVisualCursor.componentId) ?: return@InteractiveContainer
                val newNodeSelection = if (newSelection.isEmpty) null else {
                    val startNode = editorState.visualDocument.findChildById(newSelection.start.componentId)
                    val endNode = editorState.visualDocument.findChildById(newSelection.end.componentId)
                    if (startNode == null || endNode == null) null else {
                        VisualNodeSelection(
                            VisualNodeCursorPosition(
                                containerNode = startNode,
                                visualOffset = newSelection.start.visualOffset
                            ),
                            VisualNodeCursorPosition(
                                containerNode = endNode,
                                visualOffset = newSelection.end.visualOffset
                            )
                        )
                    }
                }
                onChange(
                    editorState.copy(
                        nodeCursor = VisualNodeCursorPosition(newNode, newVisualCursor.visualOffset),
                        nodeSelection = newNodeSelection
                    )
                )
            },
            onInput = inputQueue::add
        ) {
            editorScope.view()
        }

        val visualCursorRect = interactiveScope.cursorVisualRect
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
                    editorScope.toolbar()
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
        val request = if (editorState.nodeCursor?.matchesVisualCursor(interactiveScope) == true) {
            return
        } else {
            val newCursor = editorState.nodeCursor?.textNodeUnderCursor ?: return
            SetCursor(CursorPosition(newCursor.node.interactiveId, newCursor.charOffset))
        }
        val requestedCursor = request.newPosition
        if (interactiveScope.hasComponent(requestedCursor.componentId)) {
            interactiveScope.cursorPosition = request.newPosition
        } else {
            val requestedNode = editorState.visualDocument
                .findChildById(requestedCursor.componentId)
            val renderedNode = requestedNode
                ?.findClosestParentMatching { interactiveScope.hasComponent(it.interactiveId) }
                ?: return

            interactiveScope.cursorPosition = CursorPosition(
                renderedNode.interactiveId,
                renderedNode.findOffsetByTextChild(requestedNode, requestedCursor.visualOffset)
            )
        }
    }
    moveCursor()

    LaunchedEffect(inputQueue.firstOrNull(), inputQueue.size, editorState.nodeCursor) {
        val textInputCommand = inputQueue.firstOrNull() ?: return@LaunchedEffect
        if (editorState.nodeCursor == null && textInputCommand.needsValidCursor) return@LaunchedEffect
        inputQueue.removeFirst()
        onChange(
            commandEditors.forCommand(textInputCommand).edit(editorState, textInputCommand) ?: return@LaunchedEffect
        )
    }
}

// region dsl

interface WysiwygEditorScope {
    @Composable
    fun View(view: @Composable () -> Unit)

    @Composable
    fun Toolbar(toolbar: @Composable () -> Unit)
}

private class WysiwygEditorScopeImpl : WysiwygEditorScope {
    lateinit var view: @Composable (() -> Unit)
    var toolbar: @Composable () -> Unit = {}

    @Composable
    override fun View(view: @Composable () -> Unit) {
        this.view = view
    }

    @Composable
    override fun Toolbar(toolbar: @Composable () -> Unit) {
        this.toolbar = toolbar
    }
}

// endregion dsl

data class WysiwygEditorState<D : Any>(
    val visualDocument: VisualNode<D, D>,
    val interactiveScope: InteractiveScope = InteractiveScope(),
    val undoManager: UndoManager<D> = UndoManager(),
    val nodeCursor: VisualNodeCursorPosition<D>?,
    val nodeSelection: VisualNodeSelection<D>?
) {
    init {
        require(nodeCursor == null || nodeCursor.containerNode.root == visualDocument) {
            "Node cursor must point to the current visualDocument."
        }
        require(nodeSelection == null || nodeSelection.containingNode.root == visualDocument) {
            "Node selection must point to the current visualDocument."
        }
    }

    inline fun <reified T : Any> touchedNodesOfType(): List<VisualNode<T, D>> =
        nodeSelection.touchedNodesOfType<T, D>() + nodeCursor.touchedNodesOfType<T, D>()
}

@Composable
fun <D : Any> rememberWysiwygEditorState(
    visualDocument: VisualNode<D, D>,
    vararg keys: Any?
) = remember(keys) {
    mutableStateOf(
        WysiwygEditorState(
            visualDocument = visualDocument,
            nodeCursor = null,
            nodeSelection = null
        )
    )
}

fun <D : Any> VisualNodeCursorPosition<D>.matchesVisualCursor(interactiveScope: InteractiveScope): Boolean {
    val visualCursor = interactiveScope.cursorPosition ?: return false
    val renderedNode = textNodeUnderCursor.node.findClosestParentMatching {
        visualCursor.componentId == it.interactiveId
    } ?: return false

    return visualCursor.visualOffset == renderedNode.findOffsetByTextChild(
        textNodeUnderCursor.node,
        textNodeUnderCursor.charOffset
    )
}