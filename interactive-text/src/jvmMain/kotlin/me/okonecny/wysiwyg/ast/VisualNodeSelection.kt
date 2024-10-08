package me.okonecny.wysiwyg.ast

data class VisualNodeSelection(
    val start: VisualNodeCursorPosition,
    val end: VisualNodeCursorPosition
) {
    /**
     * Node that contains the entire selection.
     */
    val containingNode: VisualNode<*> by lazy {
        commonParent(start.node, end.node)
    }
}

enum class VisualNodeSelectionMode {
    CONTAINS_START,
    CONTAINS_END,
    CONTAINS_SELECTION,
    IS_ENGULFED,
    OUTSIDE
}

fun VisualNode<*>.selectionMode(selection: VisualNodeSelection?): VisualNodeSelectionMode {
    // Check myself first.
    if (selection == null) return VisualNodeSelectionMode.OUTSIDE
    if (selection.start.node == selection.end.node && selection.start.node == this) return VisualNodeSelectionMode.CONTAINS_SELECTION
    if (selection.start.node == this) return VisualNodeSelectionMode.CONTAINS_START
    if (selection.end.node == this) return VisualNodeSelectionMode.CONTAINS_END

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

fun VisualNodeSelection?.hitsNode(node: VisualNode<*>): Boolean {
    if (this == null) return false
    if (start.node == node || end.node == node) return true
    return node.isBetweenIncluding(start.node, end.node)
}