package me.okonecny.wysiwyg.ast

data class VisualNodeCursorPosition<D : Any>(
    val containerNode: VisualNode<*, D>,
    val visualOffset: Int
) {
    val textNodeUnderCursor: VisualNode.TextWithCharOffset<D> by lazy {
        containerNode.findTextChildAtOffset(visualOffset)
    }
}
