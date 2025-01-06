package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.wysiwyg.WysiwygEditorState

interface CommandEditor<C : TextInputCommand> {
    fun <D : Any> edit(editorState: WysiwygEditorState<D>, command: C): WysiwygEditorState<D>?
}