package me.okonecny.wysiwyg.ast

data class VisualEditorState(
    val cursorPosition: VisualNodeCursorPosition,
    val selection: VisualNodeSelection?
)