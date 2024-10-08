package me.okonecny.wysiwyg.ast

data class VisualNodeCursorPosition(
    val node: VisualNode<*>,
    val visualOffset: Int
)
