package me.okonecny.wysiwyg.ast

data class VisualNodeCursorPosition<D : Any>(
    /**
     * Node under the cursor. Not necessarily a leaf node.
     */
    val containerNode: VisualNode<*, D>,
    /**
     * Text offset in the text encompassed by the containerNode.
     */
    val visualOffset: Int
) {
    /**
     * Leaf text node under the cursor and the corresponding offset in that text.
     */
    val textNodeUnderCursor: VisualNode.TextWithCharOffset<D> by lazy {
        containerNode.findTextChildAtOffset(visualOffset)
    }
}

inline fun <reified T : Any, D : Any> VisualNodeCursorPosition<D>?.touchedNodesOfType(): List<VisualNode<T, D>> {
    val leafNode = this?.textNodeUnderCursor?.node ?: return emptyList()
    return (leafNode.allParents + leafNode)
        .mapNotNull { it typedAs T::class }
}
