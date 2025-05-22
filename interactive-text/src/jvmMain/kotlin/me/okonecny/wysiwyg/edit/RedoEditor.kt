package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.Redo
import me.okonecny.interactivetext.SetCursor
import me.okonecny.wysiwyg.WysiwygEditorState

class RedoEditor<D : Any> : CommandEditor<Redo, D> {
    override fun edit(
        editorState: WysiwygEditorState<D>,
        command: Redo
    ): WysiwygEditorState<D>? {
        val redone = editorState.undoManager.redo()
        if (redone === editorState.undoManager) return null
        return editorState.copy(
            visualDocument = redone.mostRecentHistory.document,
            visualCursorRequest = redone.mostRecentHistory.visualCursor?.let { SetCursor(it) },
            undoManager = redone
        )
    }
}