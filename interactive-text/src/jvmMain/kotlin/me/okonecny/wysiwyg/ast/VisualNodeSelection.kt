package me.okonecny.wysiwyg.ast

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