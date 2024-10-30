package me.okonecny.wysiwyg.ast

data class VisualEditorState<D>(
    val cursorPosition: VisualNodeCursorPosition<D>,
    val selection: VisualNodeSelection<D>?
)