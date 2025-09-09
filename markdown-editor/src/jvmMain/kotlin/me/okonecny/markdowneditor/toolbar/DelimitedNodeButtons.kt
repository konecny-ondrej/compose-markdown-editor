package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.okonecny.markdowneditor.ast.data.CodeSpan
import me.okonecny.markdowneditor.ast.data.Emphasis
import me.okonecny.markdowneditor.ast.data.StrongEmphasis
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.*
import me.okonecny.wysiwyg.ast.data.Text


@Composable
internal fun <D : Any> EmphasisButton(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) =
    DelimitedNodeButton(
        "I",
        "Emphasis",
        TextStyle(fontStyle = FontStyle.Italic),
        Emphasis,
        editorState,
        onChange
    )

@Composable
internal fun <D : Any> StrongEmphasisButton(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) = DelimitedNodeButton(
    "B",
    "Strong Emphasis",
    TextStyle(fontWeight = FontWeight.Bold),
    StrongEmphasis,
    editorState,
    onChange
)

@Composable
internal fun <D : Any> CodeButton(editorState: WysiwygEditorState<D>, onChange: (WysiwygEditorState<D>) -> Unit) =
    DelimitedNodeButton(
        "\uf44f",
        "Inline Code",
        TextStyle.Default,
        CodeSpan,
        editorState,
        onChange,
        Modifier.offset((-2.5).dp)
    )

@Composable
private inline fun <reified T : Any, D : Any> DelimitedNodeButton(
    text: String,
    tooltip: String,
    textStyle: TextStyle,
    formattingParentData: T,
    editorState: WysiwygEditorState<D>,
    crossinline onChange: (WysiwygEditorState<D>) -> Unit,
    modifier: Modifier = Modifier
) {
    val touchedTextNodes = editorState.touchedNodesOfType<Text>()
    val formattingParentNodes = touchedTextNodes
        .flatMap { it.allParents }
        .mapNotNull { it typedAs T::class }
        .toSet()
    val formattingIsActive = formattingParentNodes.isNotEmpty()

    TextToolbarButton(
        text = text,
        tooltip = tooltip,
        disabledIf = touchedTextNodes.isEmpty() || formattingParentNodes.size > 1,
        activeIf = formattingIsActive,
        textStyle = textStyle,
        modifier = modifier,
    ) {
        editorState.interactiveScope.focusRequester.requestFocus()

        if (formattingIsActive) {
            val newDocument = formattingParentNodes.singleOrNull()?.replaceByChildren()?.compactTextNodes()
                ?: return@TextToolbarButton
            onChange(
                editorState.copy(
                    visualDocument = newDocument,
                    nodeCursor = editorState.nodeCursor.refresh(newDocument),
                    nodeSelection = editorState.nodeSelection.refresh(newDocument)
                )
            )
        } else {
            val selection = editorState.nodeSelectionOrWordUnderCursor ?: return@TextToolbarButton
            val newDocument = editorState.visualDocument.copyModified { node, proposedChildren ->
                if (node !in touchedTextNodes) return@copyModified listOf(node.copy(proposedChildren = proposedChildren))
                val (selectionStart, selectionEnd) = selection
                val formattedChildren = mutableListOf<VisualNode<Any, D>>()
                val unformattedChildrenBefore = mutableListOf<VisualNode<Any, D>>()
                val unformattedChildrenAfter = mutableListOf<VisualNode<Any, D>>()

                val nodeText = node.totalText

                if (node == selectionStart.textNodeUnderCursor.node && node != selectionEnd.textNodeUnderCursor.node) {
                    formattedChildren.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    0,
                                    selectionStart.textNodeUnderCursor.charOffset
                                )
                            )
                        )
                    )
                    unformattedChildrenAfter.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    selectionStart.textNodeUnderCursor.charOffset,
                                    nodeText.length
                                )
                            )
                        )
                    )
                }
                if (node != selectionStart.textNodeUnderCursor.node && node == selectionEnd.textNodeUnderCursor.node) {
                    unformattedChildrenBefore.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    0,
                                    selectionEnd.textNodeUnderCursor.charOffset
                                )
                            )
                        )
                    )
                    formattedChildren.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    selectionEnd.textNodeUnderCursor.charOffset,
                                    nodeText.length
                                )
                            )
                        )
                    )

                }
                if (node != selectionEnd.textNodeUnderCursor.node && node != selectionEnd.textNodeUnderCursor.node) {
                    formattedChildren.add(node)
                }
                if (node == selectionStart.textNodeUnderCursor.node && node == selectionEnd.textNodeUnderCursor.node) {
                    unformattedChildrenBefore.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    0,
                                    selectionStart.textNodeUnderCursor.charOffset
                                )
                            )
                        )
                    )
                    formattedChildren.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    selectionStart.textNodeUnderCursor.charOffset,
                                    selectionEnd.textNodeUnderCursor.charOffset
                                )
                            )
                        )
                    )
                    unformattedChildrenAfter.add(
                        VisualNode(
                            Text(
                                nodeText.substring(
                                    selectionEnd.textNodeUnderCursor.charOffset,
                                    nodeText.length
                                )
                            )
                        )
                    )
                }

                unformattedChildrenBefore + listOf(
                    VisualNode(
                        formattingParentData,
                        proposedChildren = formattedChildren
                    )
                ) + unformattedChildrenAfter
            }.singleOrNull()?.root ?: editorState.visualDocument
            onChange(
                editorState.copy(
                    visualDocument = newDocument,
                    nodeCursor = editorState.nodeCursor.refresh(newDocument),
                    nodeSelection = editorState.nodeSelection.refresh(newDocument)
                )
            )
        }
    }
}

val <D : Any> WysiwygEditorState<D>.nodeSelectionOrWordUnderCursor: VisualNodeSelection<D>?
    get() {
        val nodeUnderCursor = nodeCursor?.textNodeUnderCursor
        return nodeSelection ?: if (nodeUnderCursor == null) {
            null
        } else {
            val cursorNode = nodeUnderCursor.node
            val textRange = nodeUnderCursor.text.wordRangeAt(nodeUnderCursor.charOffset).textRange
            VisualNodeSelection(
                VisualNodeCursorPosition(cursorNode, textRange.start),
                VisualNodeCursorPosition(cursorNode, textRange.end),
            )
        }
    }

fun <T : Any, D : Any> VisualNode<T, D>.compactTextNodes(): VisualNode<T, D> {
    val compactedChildren = mutableListOf<VisualNode<Any, D>>()

    var compactedText = ""
    for (child in children) {
        val textChild = child typedAs Text::class
        if (textChild == null) {
            if (compactedText.isNotEmpty()) {
                compactedChildren.add(VisualNode(Text(compactedText)))
                compactedText = ""
            }
            compactedChildren.add(child.compactTextNodes())
        } else {
            compactedText += textChild.text
        }
    }
    if (compactedText.isNotEmpty()) {
        compactedChildren.add(VisualNode(Text(compactedText)))
    }

    return copy(proposedChildren = compactedChildren)
}

fun String.wordRangeAt(pos: Int): IntRange {
    if (isBlank()) return IntRange.EMPTY
    if (pos < 0 || pos > lastIndex) return IntRange.EMPTY

    val whitespacePadding = substring(0..pos)
        .takeLastWhile { !it.isLetterOrDigit() }
        .length
    val charsTillStart = substring(0, (pos - whitespacePadding).coerceAtLeast(0))
        .takeLastWhile { it.isLetterOrDigit() }
        .length
    val wordStart = (pos - whitespacePadding - charsTillStart).coerceAtLeast(0)
    val wordLength = substring(wordStart..lastIndex)
        .takeWhile { it.isLetterOrDigit() }
        .length
    val wordEnd = wordStart + wordLength

    return wordStart until wordEnd
}