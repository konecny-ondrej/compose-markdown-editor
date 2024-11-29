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

enum class VisualNodeSelectionMode {
    CONTAINS_START,
    CONTAINS_END,
    CONTAINS_SELECTION,
    IS_ENGULFED,
    OUTSIDE
}

fun <D : Any> VisualNode<*, D>.selectionMode(selection: VisualNodeSelection<D>?): VisualNodeSelectionMode {
    // Check myself first.
    if (selection == null) return VisualNodeSelectionMode.OUTSIDE
    if (selection.start.containerNode == selection.end.containerNode && selection.start.containerNode == this) return VisualNodeSelectionMode.CONTAINS_SELECTION
    if (selection.start.containerNode == this) return VisualNodeSelectionMode.CONTAINS_START
    if (selection.end.containerNode == this) return VisualNodeSelectionMode.CONTAINS_END

    // Search DOWN
    val containsStart = children.any { it.selectionMode(selection) == VisualNodeSelectionMode.CONTAINS_START }
    val containsEnd = children.any { it.selectionMode(selection) == VisualNodeSelectionMode.CONTAINS_END }
    if (containsStart && containsEnd) return VisualNodeSelectionMode.CONTAINS_SELECTION
    if (containsStart) return VisualNodeSelectionMode.CONTAINS_START
    if (containsEnd) return VisualNodeSelectionMode.CONTAINS_END

    // Search UP
    val selectionIsBefore = siblingsBefore.any {
        it.selectionMode(selection) == VisualNodeSelectionMode.CONTAINS_START
    }
    val selectionIsAfter = siblingsAfter.any {
        it.selectionMode(selection) == VisualNodeSelectionMode.CONTAINS_END
    }
    if (selectionIsBefore && selectionIsAfter) return VisualNodeSelectionMode.IS_ENGULFED
    return VisualNodeSelectionMode.OUTSIDE
}

fun <D : Any> VisualNodeSelection<D>?.hitsNode(node: VisualNode<*, D>): Boolean {
    if (this == null) return false
    if (start.containerNode == node || end.containerNode == node) return true
    return node.isBetweenIncluding(start.containerNode, end.containerNode)
}