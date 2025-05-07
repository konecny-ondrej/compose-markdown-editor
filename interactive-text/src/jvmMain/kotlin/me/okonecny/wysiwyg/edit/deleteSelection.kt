package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.SetCursor
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
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
            starTextNode.data.text.removeRange(nodeSelection.start.visualOffset, nodeSelection.end.visualOffset)
        return if (newText.isEmpty()) {
            removeNode(starTextNode, editorState)
        } else {
            editorState.copy(
                visualDocument = starTextNode.replaceWith(
                    starTextNode.copy(data = starTextNode.data.replaceText(newText))
                ).root,
                visualCursorRequest = SetCursor(
                    CursorPosition(
                        starTextNode.interactiveId,
                        nodeSelection.start.visualOffset
                    )
                )
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
        .copyModified { node ->
            when (node) {
                in nodesToRemove -> null
                starTextNode -> starTextNode.copy(data = starTextNode.data.replaceText(newStartText))
                endTextNode -> endTextNode.copy(data = endTextNode.data.replaceText(newEndText))
                else -> node
            }
        } typedAs originalDocument) ?: return null // Cannot remove the document itself

    val oldCursorTextOffset = originalDocument.findOffsetByTextChild(
        nodeSelection.start.textNodeUnderCursor.node,
        nodeSelection.start.textNodeUnderCursor.charOffset
    )
    val newTextNodeUnderCursor = newDocument.findTextChildAtOffset(oldCursorTextOffset)

    val newCursor = SetCursor(
        CursorPosition(
            newTextNodeUnderCursor.node.interactiveId,
            newTextNodeUnderCursor.charOffset
        )
    )

    return editorState.copy(
        visualDocument = newDocument,
        visualCursorRequest = newCursor
    )
}