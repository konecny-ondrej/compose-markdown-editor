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
import me.okonecny.wysiwyg.ast.data.HasText

/**
 * Deletes a character or a word before or after cursor.
 * Assumes that a word is not spanning multiple nodes (if it does, it is considered as multiple words).
 */
class DeleteEditor : CommandEditor<Delete> {
    override fun <D : Any> edit(editorState: WysiwygEditorState<D>, command: Delete): WysiwygEditorState<D>? {
        val editedTextNodeWithOffset = (editorState.nodeCursor ?: return null).textNodeUnderCursor
// TODO: take selection into account.
        val editedTextNode = editedTextNodeWithOffset.node.let {
            when (command.direction) {
                Delete.Direction.BEFORE_CURSOR -> if (editedTextNodeWithOffset.isAtStart) it.findPrevByDataType<HasText>() else it
                Delete.Direction.AFTER_CURSOR -> if (editedTextNodeWithOffset.isAtEnd) it.findNextByDataType<HasText>() else it
            }
        } ?: return null

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
        val removeNode = newText.isEmpty()
        val removedNode = editedTextNode.findParentWhile { it.children.size == 1 } ?: editedTextNode
        val newRootNode = if (removeNode) {
            removedNode.removeNode() ?: return null // Cannot remove the document itself.
        } else {
            editedTextNode.replaceWith(
                editedTextNode.copy(data = editedTextNode.data.replaceText(newText))
            ).root
        }

        return editorState.copy(
            visualDocument = newRootNode,
            visualCursorRequest = if (removeNode) {
                val newTextNodeUnderCursor = newRootNode.findTextChildAtOffset(editedTextNode.textLengthBefore)
                SetCursor(
                    CursorPosition(
                        newTextNodeUnderCursor.node.interactiveId,
                        newTextNodeUnderCursor.charOffset
                    )
                )
            } else {
                when (command.direction) {
                    Delete.Direction.BEFORE_CURSOR -> MoveCursorOnLine(-deleteRange.length)
                    Delete.Direction.AFTER_CURSOR -> null
                }
            }
        )
    }
}