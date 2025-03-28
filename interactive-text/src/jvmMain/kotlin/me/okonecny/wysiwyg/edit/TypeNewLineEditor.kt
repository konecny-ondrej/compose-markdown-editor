package me.okonecny.wysiwyg.edit

import me.okonecny.interactivetext.NewLine
import me.okonecny.wysiwyg.WysiwygEditorState

class TypeNewLineEditor<D : Any> : CommandEditor<NewLine, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: NewLine): WysiwygEditorState<D>? =
        TODO("Create a new empty paragraph or a new bullet point in a list.")
}