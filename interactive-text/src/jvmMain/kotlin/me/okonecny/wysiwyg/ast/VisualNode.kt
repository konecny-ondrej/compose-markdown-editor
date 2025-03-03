package me.okonecny.wysiwyg.ast

import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.InteractiveId
import me.okonecny.interactivetext.LinearInteractiveIdGenerator.Companion.firstInteractiveId
import me.okonecny.lang.only
import me.okonecny.lang.onlyOrNull
import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.ast.data.Text

/**
 * Syntax tree for the editor to work with. The editor will add/remove/replace nodes based on the user actions.
 * @param T Type of the data carried by this node.
 * @param D Type of the data of the root node, the "document type" for short.
 */
data class VisualNode<out T : Any, D : Any>(
    val data: T,
    val parentIndex: Int? = null,
    val parent: VisualNode<*, D>? = null,
    val sourceRange: TextRange, // TODO: remove. Won't be needed.
    private val proposedChildren: List<VisualNode<Any, D>> = emptyList()
) {
    val isRoot: Boolean = parent == null
    val children: List<VisualNode<Any, D>> = proposedChildren
        .ifEmpty { if (isRoot) listOf(nil(this)) else emptyList() }
        .mapIndexed { index, childNode ->
            childNode.copy(parent = this, parentIndex = index)
        }

    val root: VisualNode<D, D> by lazy {
        parent?.root
            ?: this as VisualNode<D, D> // If this is root, then the data type must be the same as the document type.
    }

    val allSiblings: List<VisualNode<Any, D>> by lazy {
        parent?.children ?: listOf(this)
    }

    val interactiveId: InteractiveId by lazy {
        // Generates interactive ids in reading order.
        previousNodeInReadingOrder?.interactiveId?.plus(1) ?: firstInteractiveId
    }

    val previousNodeInReadingOrder: VisualNode<Any, D>? by lazy {
        // This is just a reverse depth-first pre-order walk of the VisualNode tree.
        // We just want to initialize the "previous node" on each node that we visit, so we don't have to compute them again.
        if (parent == null) return@lazy null
        if (siblingsBefore.isEmpty()) {
            parent
        } else {
            val previousSibling = siblingsBefore.last()
            if (previousSibling.children.isEmpty()) {
                previousSibling
            } else {
                var previousSiblingDeepestRightChild: VisualNode<*, D> = previousSibling.children.last()
                while (previousSiblingDeepestRightChild.children.isNotEmpty()) {
                    previousSiblingDeepestRightChild = previousSiblingDeepestRightChild.children.last()
                }
                previousSiblingDeepestRightChild
            }
        }
    }

    val nextNodeInReadingOrder: VisualNode<Any, D>? by lazy {
        // This is just a depth-first pre-order walk of the Visual Node tree.
        if (children.isNotEmpty()) return@lazy children.first()

        if (siblingsAfter.isNotEmpty()) {
            return@lazy siblingsAfter.first()
        } else {
            var parentWithSiblings: VisualNode<Any, D> = parent ?: return@lazy null
            while (parentWithSiblings.siblingsAfter.isEmpty()) {
                parentWithSiblings = parentWithSiblings.parent ?: return@lazy null
            }
            return@lazy parentWithSiblings.siblingsAfter.first()
        }
    }

    val siblingsBefore by lazy {
        if (parentIndex == null) {
            emptyList()
        } else {
            allSiblings.subList(0, parentIndex)
        }
    }

    val siblingsAfter by lazy {
        if (parentIndex == null || parentIndex == parent?.children?.lastIndex) {
            emptyList()
        } else {
            allSiblings.subList(parentIndex + 1, allSiblings.size)
        }
    }

    val allParents: List<VisualNode<*, D>> by lazy {
        if (parent == null) {
            emptyList()
        } else {
            listOf(parent) + parent.allParents
        }
    }

    fun isBetweenIncluding(node1: VisualNode<*, D>, node2: VisualNode<*, D>): Boolean {
        val commonParent = commonParent(node1, node2)

        val myNodeInCommonParent = commonParent.children
            .intersect(allParents.toSet())
            .onlyOrNull("The node graph must be a tree.")
            ?: return false
        val myIndexInParent = myNodeInCommonParent.parentIndex ?: return false // We have reached the root.
        val n1f1 = commonParent.children.intersect(node1.allParents.toSet()).first().parentIndex ?: return false
        val n2f1 = commonParent.children.intersect(node2.allParents.toSet()).first().parentIndex ?: return false

        return myIndexInParent in n1f1..n2f1
    }

    /**
     * Replaces the node with the new node specified.
     * More specifically this copies the entire tree and places the new node instead of this one.
     * @param newNode New node to use instead of this one.
     * @return The new node as a part of a copy of the entire tree.
     */
    fun <T : Any> replaceWith(newNode: VisualNode<T, D>): VisualNode<T, D> {
        val parentNode =
            parent ?: return newNode // When replacing the root node, just use the new node as the new root.

        val expectedParentIndex = siblingsBefore.size
        val replacedParent = parentNode.replaceWith(
            parentNode.copy(
                proposedChildren = siblingsBefore + newNode + siblingsAfter
            )
        )
        return replacedParent.children[expectedParentIndex] as VisualNode<T, D>
    }

    /**
     * Removes this node.
     * More specifically this copies the entire tree without the subtree to which this node is the root.
     * @return A copy of the entire tree without the subtree specified by this node. Null if you remove the root itself.
     */
    fun removeNode(): VisualNode<D, D>? {
        val parentNode =
            parent ?: return null // When removing the root node, just return null as there is nothing left.
        val replacedParent = parentNode.replaceWith(
            parentNode.copy(
                proposedChildren = siblingsBefore + siblingsAfter
            )
        )
        return replacedParent.root
    }

    fun findChildById(id: InteractiveId): VisualNode<*, D>? =
        if (interactiveId == id) {
            this
        } else {
            children.firstNotNullOfOrNull { child ->
                child.findChildById(id)
            }
        }

    inline fun <reified T : Any> findNext(predicate: (VisualNode<T, D>) -> Boolean = { true }): VisualNode<T, D>? =
        find(VisualNode<Any, D>::nextNodeInReadingOrder, predicate)

    inline fun <reified T : Any> findPrev(predicate: (VisualNode<T, D>) -> Boolean = { true }): VisualNode<T, D>? =
        find(VisualNode<Any, D>::previousNodeInReadingOrder, predicate)

    inline fun <reified T : Any> find(
        successor: VisualNode<Any, D>.() -> VisualNode<Any, D>?,
        predicate: (VisualNode<T, D>) -> Boolean = { true }
    ): VisualNode<T, D>? {
        var currentNode: VisualNode<Any, D> = this.successor() ?: return null
        while (currentNode.data !is T || !predicate(currentNode as VisualNode<T, D>)) {
            currentNode = currentNode.successor() ?: return null
        }
        return currentNode
    }

    /**
     * Walks the parent node chain while the predicate matches.
     * @return Farthest parent node matching the predicate continuously.
     */
    fun findParentWhile(predicate: (VisualNode<Any, D>) -> Boolean): VisualNode<Any, D>? {
        var currentNode: VisualNode<Any, D>? = this.parent
        var lastMatchingNode: VisualNode<Any, D>? = null
        while (currentNode != null) {
            if (predicate(currentNode)) {
                lastMatchingNode = currentNode
            } else break
            currentNode = currentNode.parent
        }
        return lastMatchingNode
    }

    fun findClosestParentMatching(predicate: (VisualNode<Any, D>) -> Boolean): VisualNode<Any, D>? {
        var currentNode: VisualNode<Any, D>? = this.parent
        while (currentNode != null) {
            if (predicate(currentNode)) {
                return currentNode
            }
            currentNode = currentNode.parent
        }
        return null
    }

    fun findFarthestParentMatching(predicate: (VisualNode<Any, D>) -> Boolean): VisualNode<Any, D>? {
        var currentNode: VisualNode<Any, D>? = this.parent
        var lastMatchingNode: VisualNode<Any, D>? = null
        while (currentNode != null) {
            if (predicate(currentNode)) {
                lastMatchingNode = currentNode
            }
            currentNode = currentNode.parent
        }
        return lastMatchingNode
    }

    data class TextWithCharOffset<D : Any>(
        val node: VisualNode<HasText, D>,
        val charOffset: Int
    ) {
        val isAtStart: Boolean = charOffset == 0
        val isAtEnd: Boolean = node.data.text.length == charOffset
    }

    /**
     * Assume this node to be a container of text. Then find a child node (or self), which contains the character
     * at the specific offset from the start of the text in this container.
     */
    fun findTextChildAtOffset(charOffset: Int): TextWithCharOffset<D> {
        var textLengthSoFar = 0
        var currentNode: VisualNode<*, D> = this

        while (textLengthSoFar <= charOffset) {
            if (currentNode.data is HasText) {
                val currentTextNode = currentNode as VisualNode<HasText, D>
                val currentTextLength = currentTextNode.data.text.length
                val totalTextLength = textLengthSoFar + currentTextLength
                if (totalTextLength >= charOffset) return TextWithCharOffset(
                    node = currentTextNode,
                    charOffset = charOffset - textLengthSoFar
                )
                textLengthSoFar = totalTextLength
            }
            currentNode = currentNode.nextNodeInReadingOrder ?: throw IndexOutOfBoundsException(
                "Offset %d is larger than the text length %d".format(charOffset, textLengthSoFar)
            )
        }
        throw IndexOutOfBoundsException(
            "Offset %d is larger than the text length %d".format(charOffset, textLengthSoFar)
        )
    }

    /**
     * Assume this node to be a container of text. Also assume a child node that also contains text and an offset within
     * that text.
     * Then find text offset inside this container node corresponding to the offset in the child node.
     * Basically this an inverse function to findTextChildAtOffset().
     */
    fun findOffsetByTextChild(childNode: VisualNode<*, D>, offset: Int): Int {
        if (childNode == this) return offset

        var textLengthSoFar = offset
        var currentNode: VisualNode<Any, D>? = childNode.previousNodeInReadingOrder
        while (currentNode != null) {
            val currentData = currentNode.data
            if (currentData is HasText) {
                textLengthSoFar += currentData.text.length
            }
            if (currentNode == this) return textLengthSoFar
            currentNode = currentNode.previousNodeInReadingOrder
        }
        throw IllegalArgumentException("The passed node must be a child of this node.")
    }

    val totalTextLength: Int by lazy {
        if (data is HasText) {
            data.text.length
        } else {
            children.sumOf(VisualNode<Any, D>::totalTextLength)
        }
    }

    val textLengthBefore: Int by lazy {
        val prevTextNode = findPrev<HasText>() ?: return@lazy 0
        prevTextNode.data.text.length + prevTextNode.textLengthBefore
    }

    override fun toString(): String {
        return "VisualNode(${parent?.data?.let { "parent=" + it::class.simpleName } ?: "<ROOT>"}, data=$data)"
    }

    companion object {
        private fun <D : Any> nil(parent: VisualNode<*, D>) = VisualNode(
            parent = parent,
            data = Text("\uFEFF"), // Zero-width space
            sourceRange = TextRange.Zero
        )
    }
}

fun <D : Any> commonParent(node1: VisualNode<*, D>, node2: VisualNode<*, D>): VisualNode<*, D> {
    if (node1 == node2) return node1
    val startParents = node1.allParents
    val endParents = node2.allParents

    val commonParent = endParents.intersect(startParents.toSet()).only(
        "Both nodes in must be a part of the same tree => there must be one common parent for every pair of nodes."
    )
    return commonParent
}

