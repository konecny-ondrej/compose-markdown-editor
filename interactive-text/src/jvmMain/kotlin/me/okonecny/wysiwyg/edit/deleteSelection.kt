package me.okonecny.wysiwyg.edit

import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.typedAs

internal fun <D : Any> deleteSelection(
    editorState: WysiwygEditorState<D>,
    nodeSelection: VisualNodeSelection<D>
): WysiwygEditorState<D>? {
    val starTextNode = nodeSelection.start.textNodeUnderCursor.node
    val endTextNode = nodeSelection.end.textNodeUnderCursor.node

    if (starTextNode == endTextNode) {
        val newText =
            starTextNode.data.text.removeRange(
                nodeSelection.start.textNodeUnderCursor.charOffset,
                nodeSelection.end.textNodeUnderCursor.charOffset
            )
        return if (newText.isEmpty()) {
            removeNode(starTextNode, editorState)
        } else {
            val nodeAfterEdit = starTextNode.replaceWith(
                starTextNode.copy(data = starTextNode.data.replaceText(newText))
            )
            editorState.copy(
                visualDocument = nodeAfterEdit.root,
                nodeCursor = VisualNodeCursorPosition(
                    nodeAfterEdit,
                    nodeSelection.start.textNodeUnderCursor.charOffset
                ),
                nodeSelection = null
            )
        }
    }

    val newStartText = starTextNode.data.text.removeRange(
        nodeSelection.start.textNodeUnderCursor.charOffset,
        starTextNode.data.text.length
    )
    val newEndText = endTextNode.data.text.removeRange(0, nodeSelection.end.textNodeUnderCursor.charOffset)

    val nodesToRemove = mutableSetOf<VisualNode<Any, D>>()
    nodesToRemove.addAll(starTextNode.findAllSuccessorsWhile<Any>(VisualNode<Any, D>::nextNodeInReadingOrder) {
        it != endTextNode
    })
    if (newStartText.isEmpty()) {
        nodesToRemove.add(starTextNode)
    } else {
        nodesToRemove.removeAll(starTextNode.allParents)
    }
    if (newEndText.isEmpty()) {
        nodesToRemove.add(endTextNode)
    } else {
        nodesToRemove.removeAll(endTextNode.allParents)
    }
    // FIXME: join start and end nodes. Use the type of the start node for the result.

    val originalDocument = editorState.visualDocument
    val newDocument = (originalDocument
        .copyModified { node, proposedChildren ->
            when (node) {
                in nodesToRemove -> emptyList()
                starTextNode -> listOf(
                    starTextNode.copy(
                        data = starTextNode.data.replaceText(newStartText),
                        proposedChildren = proposedChildren
                    )
                )

                endTextNode -> listOf(
                    endTextNode.copy(
                        data = endTextNode.data.replaceText(newEndText),
                        proposedChildren = proposedChildren
                    )
                )

                else -> listOf(node.copy(proposedChildren = proposedChildren))
            }
        }.singleOrNull() typedAs originalDocument) ?: return null // Cannot remove the document itself

    val oldCursorTextOffset = originalDocument.findOffsetByTextChild(
        nodeSelection.start.textNodeUnderCursor.node,
        nodeSelection.start.textNodeUnderCursor.charOffset
    )
    val newTextNodeUnderCursor = newDocument.findTextChildAtOffset(oldCursorTextOffset)

    return editorState.copy(
        visualDocument = newDocument,
        nodeCursor = VisualNodeCursorPosition(
            newTextNodeUnderCursor.node,
            newTextNodeUnderCursor.charOffset
        ),
        nodeSelection = null
    )
}

val <D : Any> WysiwygEditorState<D>.withSelectionDeleted: WysiwygEditorState<D>
    get() {
        return deleteSelection(this, nodeSelection ?: return this) ?: this
    }