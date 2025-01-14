package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.MoveCursorOnLine
import me.okonecny.interactivetext.Type
import me.okonecny.wysiwyg.WysiwygEditorState

class TypeEditor : CommandEditor<Type> {
    override fun <D : Any> edit(editorState: WysiwygEditorState<D>, command: Type): WysiwygEditorState<D>? {
        val editedTextNodeWithOffset = (editorState.nodeCursor ?: return null).textNodeUnderCursor
        val editedTextNode = editedTextNodeWithOffset.node
        val editedText = editedTextNode.data.text
        return editorState.copy(
            visualDocument = editedTextNode.replaceWith(
                editedTextNode.copy(
                    data = editedTextNode.data.replaceText(
                        editedText.substring(0, editedTextNodeWithOffset.charOffset)
                                + command.text
                                + editedText.substring(editedTextNodeWithOffset.charOffset, editedText.length)
                    )
                )
            ).root,
            visualCursorRequest = MoveCursorOnLine(command.text.length)
        )
    }
}