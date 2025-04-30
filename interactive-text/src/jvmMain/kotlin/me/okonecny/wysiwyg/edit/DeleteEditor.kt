package me.okonecny.wysiwyg.edit

import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.Delete
import me.okonecny.interactivetext.MoveCursorOnLine
import me.okonecny.interactivetext.SetCursor
import me.okonecny.lang.removeRange
import me.okonecny.lang.wordRangeAfter
import me.okonecny.lang.wordRangeBefore
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.ast.typedAs

/**
 * Deletes a character or a word before or after cursor.
 * Assumes that a word is not spanning multiple nodes (if it does, it is considered as multiple words).
 */
class DeleteEditor<D : Any> : CommandEditor<Delete, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: Delete): WysiwygEditorState<D>? {
        val nodeSelection = editorState.nodeSelection
        if (nodeSelection != null) return deleteSelection(editorState, nodeSelection)

        val editedTextNodeWithOffset = (editorState.nodeCursor ?: return null).textNodeUnderCursor
        val visibleOrTextNode = { node: VisualNode<Any, D> ->
            node.asTextNode != null || (editorState.interactiveScope.hasComponent(node.interactiveId) && node !in editedTextNodeWithOffset.node.allParents)
        }
        val editedNode = editedTextNodeWithOffset.node.let {
            when (command.direction) {
                Delete.Direction.BEFORE_CURSOR -> if (editedTextNodeWithOffset.isAtStart && !editedTextNodeWithOffset.node.totalTextIsEmpty) it.findPrev(
                    visibleOrTextNode
                ) else it

                Delete.Direction.AFTER_CURSOR -> if (editedTextNodeWithOffset.isAtEnd) it.findNext(visibleOrTextNode) else it
            }
        } ?: return null

        return if (editedNode.totalTextIsEmpty) {
            removeNode(editedNode, editorState)
        } else {
            if (editedNode.asTextNode == null) {
                // There is some text, but not in this node => We must edit the first text child node.
                editTextNode(
                    editedNode.findNext<HasText>() ?: return null,
                    editedTextNodeWithOffset,
                    command,
                    editorState
                )
            } else {
                editTextNode(editedNode.asTextNode, editedTextNodeWithOffset, command, editorState)
            }
        }
    }

    private fun deleteSelection(
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

    private fun <D : Any> removeNode(
        editedNode: VisualNode<Any, D>,
        editorState: WysiwygEditorState<D>
    ): WysiwygEditorState<D>? {
        val removedNode = editedNode.findParentWhile { it.totalTextIsEmpty } ?: editedNode
        val newRootNode = removedNode.removeNode() ?: return null // Cannot remove the document itself.

        val newTextNodeUnderCursor = newRootNode.findTextChildAtOffset(editedNode.textLengthBefore)
        return editorState.copy(
            visualDocument = newRootNode,
            visualCursorRequest = SetCursor(
                CursorPosition(
                    newTextNodeUnderCursor.node.interactiveId,
                    newTextNodeUnderCursor.charOffset
                )
            )
        )
    }

    private fun <D : Any> editTextNode(
        editedTextNode: VisualNode<HasText, D>,
        editedTextNodeWithOffset: VisualNode.TextWithCharOffset<D>,
        command: Delete,
        editorState: WysiwygEditorState<D>
    ): WysiwygEditorState<D>? {
        if (editedTextNode.data.text.isEmpty()) return removeNode(editedTextNode, editorState)

        val editOffset = if (editedTextNode == editedTextNodeWithOffset.node) {
            editedTextNodeWithOffset.charOffset
        } else {
            when (command.direction) {
                Delete.Direction.BEFORE_CURSOR -> editedTextNode.data.text.length
                Delete.Direction.AFTER_CURSOR -> 0
            }
        }

        val deleteRange: TextRange = when (command.direction) {
            Delete.Direction.BEFORE_CURSOR -> {
                when (command.size) {
                    Delete.Size.LETTER -> TextRange((editOffset - 1).coerceAtLeast(0), editOffset)
                    Delete.Size.WORD -> editedTextNode.data.text.wordRangeBefore(editOffset)
                }
            }

            Delete.Direction.AFTER_CURSOR -> {
                when (command.size) {
                    Delete.Size.LETTER -> TextRange(editOffset, editOffset + 1)
                    Delete.Size.WORD -> editedTextNode.data.text.wordRangeAfter(editOffset)
                }
            }
        }

        val newText = editedTextNode.data.text.removeRange(deleteRange)

        val newRootNode = editedTextNode.replaceWith(
            editedTextNode.copy(data = editedTextNode.data.replaceText(newText))
        ).root


        return editorState.copy(
            visualDocument = newRootNode,
            visualCursorRequest = when (command.direction) {
                Delete.Direction.BEFORE_CURSOR -> MoveCursorOnLine(-deleteRange.length)
                Delete.Direction.AFTER_CURSOR -> null
            }
        )
    }
}