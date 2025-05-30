package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.Type
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition

class TypeEditor<D : Any> : CommandEditor<Type, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: Type): WysiwygEditorState<D>? {
        val editorStateWithoutSelection = editorState.withSelectionDeleted

        val editedTextNodeWithOffset = (editorStateWithoutSelection.nodeCursor ?: return null).textNodeUnderCursor
        val editedTextNode = editedTextNodeWithOffset.node
        val editedText = editedTextNode.data.text

        val nodeAfterEdit = editedTextNode.replaceWith(
            editedTextNode.copy(
                data = editedTextNode.data.replaceText(
                    editedText.substring(0, editedTextNodeWithOffset.charOffset)
                            + command.text
                            + editedText.substring(editedTextNodeWithOffset.charOffset, editedText.length)
                )
            )
        )
        return editorStateWithoutSelection.copy(
            visualDocument = nodeAfterEdit.root,
            nodeCursor = VisualNodeCursorPosition(
                nodeAfterEdit,
                editedTextNodeWithOffset.charOffset + command.text.length
            )
        )
    }
}