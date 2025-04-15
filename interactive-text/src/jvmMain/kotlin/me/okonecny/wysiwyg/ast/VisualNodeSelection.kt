package me.okonecny.wysiwyg.ast

import me.okonecny.wysiwyg.ast.data.HasText

data class VisualNodeSelection<D : Any>(
    val start: VisualNodeCursorPosition<D>,
    val end: VisualNodeCursorPosition<D>
) {
    /**
     * Node that contains the entire selection.
     */
    val containingNode: VisualNode<*, D> by lazy {
        commonParent(start.containerNode, end.containerNode)
    }
}

fun <D : Any> VisualNodeSelection<D>?.hitsNode(node: VisualNode<*, D>): Boolean {
    if (this == null) return false
    if (start.containerNode == node || end.containerNode == node) return true

    var currentNode: VisualNode<*, D>? = start.textNodeUnderCursor.node
    val endNode = end.textNodeUnderCursor.node
    if (currentNode == node || endNode == node) return true

    while (currentNode != null && currentNode != endNode) {
        if (currentNode == node) return true
        currentNode = currentNode.nextNodeInReadingOrder
    }
    return false
}

fun <D : Any> VisualNode<HasText, D>.selectedText(selection: VisualNodeSelection<D>?): String {
    if (selection == null) return data.text
    return if (selection.hitsNode(this)) {
            val textStart = selection.start.textNodeUnderCursor
            val textEnd = selection.end.textNodeUnderCursor

            if (textStart.node == this && textEnd.node == this) {
                data.text.substring(textStart.charOffset, textEnd.charOffset)
            } else if (textStart.node == this) {
                data.text.substring(textStart.charOffset)
            } else if (textEnd.node == this) {
                data.text.substring(0, textEnd.charOffset)
            } else {
                data.text
            }
        } else {
            ""
        }
}