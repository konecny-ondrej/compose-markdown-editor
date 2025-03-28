package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.wysiwyg.WysiwygEditorState

/**
 * Applies the text input commands to the editor / the AST.
 */
interface CommandEditor<in C : TextInputCommand, D : Any> {
    /**
     * Applies the changes specified by the command to the editor.
     * @param editorState Current state of the editor.
     * @param command Input command to apply.
     * @return New editor state with the changes applied. Null if no changes were made.
     */
    fun edit(editorState: WysiwygEditorState<D>, command: C): WysiwygEditorState<D>?
}