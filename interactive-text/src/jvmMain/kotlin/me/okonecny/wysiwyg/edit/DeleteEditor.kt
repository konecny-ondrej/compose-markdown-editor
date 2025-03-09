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
import me.okonecny.wysiwyg.ast.data.HasText

/**
 * Deletes a character or a word before or after cursor.
 * Assumes that a word is not spanning multiple nodes (if it does, it is considered as multiple words).
 */
class DeleteEditor : CommandEditor<Delete> {
    override fun <D : Any> edit(editorState: WysiwygEditorState<D>, command: Delete): WysiwygEditorState<D>? {
        val editedTextNodeWithOffset = (editorState.nodeCursor ?: return null).textNodeUnderCursor
// TODO: take selection into account.
        val visibleNode = { node: VisualNode<Any, D> ->
            editorState.interactiveScope.hasComponent(
                node.interactiveId
            )
        }
        val editedNode = editedTextNodeWithOffset.node.let {
            when (command.direction) {
                Delete.Direction.BEFORE_CURSOR -> if (editedTextNodeWithOffset.isAtStart && !editedTextNodeWithOffset.node.totalTextIsEmpty) it.findPrev(visibleNode) else it
                Delete.Direction.AFTER_CURSOR -> if (editedTextNodeWithOffset.isAtEnd) it.findNext(visibleNode) else it
            }
        } ?: return null

        return if (editedNode.totalTextIsEmpty) {
            removeNode(editedNode, editorState)
        } else {
            if(editedNode.asTextNode == null) {
                return null
            } else {
                editTextNode(editedNode.asTextNode, editedTextNodeWithOffset, command, editorState)
            }
        }
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
                    Delete.Size.LETTER -> TextRange(editOffset - 1, editOffset)
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