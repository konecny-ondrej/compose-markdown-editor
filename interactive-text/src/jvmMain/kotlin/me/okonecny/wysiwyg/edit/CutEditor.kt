package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.Copy
import me.okonecny.interactivetext.Cut
import me.okonecny.wysiwyg.WysiwygEditorState

class CutEditor<D : Any>(
    val copyEditor: CopyEditor<D>
) : CommandEditor<Cut, D> {
    override fun edit(
        editorState: WysiwygEditorState<D>,
        command: Cut
    ): WysiwygEditorState<D>? {
        copyEditor.edit(editorState, Copy)
        val newState = deleteSelection(
            editorState,
            editorState.nodeSelection ?: return null
        ) ?: return null
        return editorState.edit(
            newVisualDocument = newState.visualDocument,
            newCursor = newState.nodeCursor,
            newSelection = newState.nodeSelection
        )
    }
}