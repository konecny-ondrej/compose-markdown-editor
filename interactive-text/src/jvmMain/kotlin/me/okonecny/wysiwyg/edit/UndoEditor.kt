package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.Undo
import me.okonecny.wysiwyg.UndoManager
import me.okonecny.wysiwyg.WysiwygEditorState

class UndoEditor<D : Any> : CommandEditor<Undo, D> {
    override fun edit(
        editorState: WysiwygEditorState<D>,
        command: Undo
    ): WysiwygEditorState<D>? {
        val originalUndoManager = editorState.undoManager
        if (!originalUndoManager.hasHistory) return null
        val undoManagerWithCompleteHistory = if (originalUndoManager.undoSteps == 0) {
            originalUndoManager.add(
                UndoManager.HistoryEntry(
                    document = editorState.visualDocument,
                    cursor = editorState.nodeCursor
                )
            )
        } else originalUndoManager
        val undoneManager = undoManagerWithCompleteHistory.undo()
        val mostRecentHistory = undoneManager.mostRecentHistory
        return editorState.copy(
            visualDocument = mostRecentHistory.document,
            nodeCursor = mostRecentHistory.cursor,
            undoManager = undoneManager
        )
    }
}
