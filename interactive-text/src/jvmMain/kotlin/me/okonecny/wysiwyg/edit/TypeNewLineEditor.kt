package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.NewLine
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition

class TypeNewLineEditor<D : Any> : CommandEditor<NewLine, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: NewLine): WysiwygEditorState<D>? {
        val editorStateWithoutSelection = editorState.withSelectionDeleted
        val cursor = editorStateWithoutSelection.nodeCursor ?: return null
        val textNodeUnderCursor = cursor.textNodeUnderCursor

        val editedNode = textNodeUnderCursor.node.replaceWith(
            VisualNode(textNodeUnderCursor.node.data.replaceText(
                textNodeUnderCursor.text.take(textNodeUnderCursor.charOffset) + "\n" + textNodeUnderCursor.text.drop(textNodeUnderCursor.charOffset)
            ))
        )
        return editorStateWithoutSelection.edit(
            newVisualDocument = editedNode.root,
            newCursor = VisualNodeCursorPosition(
                editedNode,
                textNodeUnderCursor.charOffset + 1
            ),
            newSelection = null
        )
    }
}