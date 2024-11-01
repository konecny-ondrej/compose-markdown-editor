package me.okonecny.wysiwyg.ast

data class VisualNodeCursorPosition<D : Any>(
    val node: VisualNode<*, D>,
    val visualOffset: Int
)
