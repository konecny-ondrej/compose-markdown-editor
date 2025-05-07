package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.SetCursor
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode

internal fun <D : Any> removeNode(
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