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
    ): WysiwygEditorState<D>? = if (editorState.undoManager.hasHistory) {
        val mostRecentHistory = editorState.undoManager.mostRecentHistory
        editorState.copy(
            visualDocument = mostRecentHistory.document,
            visualCursorRequest = mostRecentHistory.visualCursor?.let { SetCursor(it) },
            undoManager = editorState.undoManager.undo()
        )
    } else null
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