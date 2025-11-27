package me.okonecny.wysiwyg.edit

import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.Delete
import me.okonecny.lang.removeRange
import me.okonecny.lang.wordRangeAfter
import me.okonecny.lang.wordRangeBefore
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition
import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.ast.refresh

/**
 * Deletes a character or a word before or after cursor.
 * Assumes that a word is not spanning multiple nodes (if it does, it is considered as multiple words).
 */
class DeleteEditor<D : Any> : CommandEditor<Delete, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: Delete): WysiwygEditorState<D>? {
        val nodeSelection = editorState.nodeSelection
        if (nodeSelection != null) {
            val newState = deleteSelection(editorState, nodeSelection) ?: return null
            return editorState.edit(
                newVisualDocument = newState.visualDocument,
                newCursor = newState.nodeCursor,
                newSelection = newState.nodeSelection
            )
        }

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

        val nodeAfterEdit = editedTextNode.replaceWith(
            editedTextNode.copy(data = editedTextNode.data.replaceText(newText))
        )

        return editorState.edit(
            newVisualDocument = nodeAfterEdit.root,
            newCursor = when (command.direction) {
                Delete.Direction.BEFORE_CURSOR ->
                    VisualNodeCursorPosition(
                        nodeAfterEdit,
                        deleteRange.start
                    )

                Delete.Direction.AFTER_CURSOR -> editorState.nodeCursor.refresh(nodeAfterEdit.root)
            },
            newSelection = null
        )
    }
}