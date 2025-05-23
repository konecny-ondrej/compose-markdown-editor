package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.SetCursor
import me.okonecny.interactivetext.TextInputCommand
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
            originalUndoManager.add(UndoManager.HistoryEntry(
                document = editorState.visualDocument,
                visualCursor =  editorState.visualCursor
            ))
        } else originalUndoManager
        val undoneManager = undoManagerWithCompleteHistory.undo()
        val mostRecentHistory = undoneManager.mostRecentHistory
        return editorState.copy(
            visualDocument = mostRecentHistory.document,
            visualCursorRequest = mostRecentHistory.visualCursor?.let { SetCursor(it) },
            undoManager = undoneManager
        )
    }
}

class UndoableEditor<C : TextInputCommand, D : Any>(
    val originalEditor: CommandEditor<C, D>
) : CommandEditor<C, D> {
    override fun edit(
        editorState: WysiwygEditorState<D>,
        command: C
    ): WysiwygEditorState<D>? {
        return originalEditor.edit(
            editorState = editorState.copy(
                undoManager = editorState.undoManager.add(
                    UndoManager.HistoryEntry(
                        document = editorState.visualDocument,
                        visualCursor = editorState.visualCursor
                    )
                )
            ),
            command = command
        )
    }
}

fun <C : TextInputCommand, D : Any> CommandEditor<C, D>.withUndo(): CommandEditor<C, D> = UndoableEditor(this)