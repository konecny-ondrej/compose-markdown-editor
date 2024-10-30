package me.okonecny.wysiwyg.ast

data class VisualNodeCursorPosition<D>(
    val node: VisualNode<*, D>,
    val visualOffset: Int
)
