package me.okonecny.wysiwyg.ast

import me.okonecny.interactivetext.InteractiveId
import me.okonecny.interactivetext.LinearInteractiveIdGenerator.Companion.firstInteractiveId
import me.okonecny.lang.map
import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.ast.data.Text
import kotlin.reflect.KClass

/**
 * Syntax tree for the editor to work with. The editor will add/remove/replace nodes based on the user actions.
 * @param T Type of the data carried by this node.
 * @param D Type of the data of the root node, the "document type" for short.
 */
data class VisualNode<out T : Any, D : Any>(
    val data: T,
    val parentIndex: Int? = null,
    val parent: VisualNode<*, D>? = null,
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

    val asTextNode: VisualNode<HasText, D>? = this typedAs HasText::class

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

    /**
     * Finds the index of this node (or its parent) in the parent's children list.
     * @return Index of this node in the parent's children list or null if this node is the root.
     */
    fun indexIn(parentNode: VisualNode<*, D>): Int? {
        if (parent == null) return null
        if (parent == parentNode) return parentIndex
        return parent.indexIn(parentNode)
    }

    /**
     * Replaces the node with the new node specified.
     * More specifically this copies the entire tree and places the new node instead of this one.
     * @param newNode New node to use instead of this one.
     * @return The new node as a part of a copy of the entire tree.
     */
    fun <T : Any> replaceWith(newNode: VisualNode<T, D>): VisualNode<T, D> {
        val parentNode =
            parent ?: return newNode // When replacing the root node, use the new node as the new root.

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
     * More specifically, this copies the entire tree without the subtree to which this node is the root.
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

    /**
     * Replaces this node with its children.
     * More specifically, this copies the entire tree without the node, but appending the node's children to the node's parent.
     * @return A copy of the entire tree without the specified node. Null if you try to replace the root itself.
     */
    fun replaceByChildren(): VisualNode<D, D>? {
        val parentNode =
            parent
                ?: return null // When removing the root node, just return null as there is nothing to append children to.
        val replacedParent = parentNode.replaceWith(
            parentNode.copy(
                proposedChildren = siblingsBefore + children + siblingsAfter
            )
        )
        return replacedParent.root
    }

    /**
     * Splits the subtree of this node so that each subtree represents a portion of the node's totalText.
     * One subtree will contain all text up to the offset, the other will contain the rest of the text.
     * @return A list of the two subtrees.
     */
    fun split(splitAtTextOffset: Int): Pair<VisualNode<T, D>, VisualNode<T, D>> {
        if (children.isEmpty()) {
            val thisAsText = this typedAs HasText::class
            if (thisAsText == null) {
                return Pair(this, this.copy()) // If this leaf node is non-text, then we cannot really split it.
            } else {
                // If this node is a text node, we split the text in the node.
                val leftText = thisAsText.text.take(splitAtTextOffset)
                val rightText = thisAsText.text.drop(splitAtTextOffset)
                return Pair(leftText, rightText)
                    .map {
                        VisualNode<HasText, D>(thisAsText.data.replaceText(it))
                    }
                    .map {
                        (it typedAs this)!! // We have checked that T is HasText.
                    }
            }
        } else {
            val textChild = findTextChildAtOffset(splitAtTextOffset)
            val splitChildIndex = textChild.node.indexIn(this)
            // This should not happen, except maybe in concurrent execution scenarios, because the textChild
            // was searched for in this subtree, so the indexIn should always return something meaningful.
                ?: throw IllegalStateException("The child node was not found in its subtree. Was it removed?")
            val leftChildren = children.take(splitChildIndex)
            val rightChildren = children.drop(splitChildIndex + 1)
            val (splitLeftChild, splitRightChild) = children[splitChildIndex]
                .split(
                    splitAtTextOffset - leftChildren.sumOf(
                        VisualNode<Any, D>::totalTextLength
                    )
                )
            return Pair(
                this.copy(proposedChildren = leftChildren + splitLeftChild),
                this.copy(proposedChildren = listOf(splitRightChild) + rightChildren)
            )
        }
    }

    /**
     * Copies the subtree specified by this node applying the modifications by the map function to each node.
     * @param modify Function to modify each node before copying it. The node can change the data type. The function can return zero, one, or more nodes to be used instead of the current node.
     * @return A copy of the subtree specified by this node with the modifications applied. Empty list if the node itself is removed.
     */
    fun copyModified(
        modify: (VisualNode<Any, D>, List<VisualNode<Any, D>>) -> List<VisualNode<Any, D>>
    ): List<VisualNode<Any, D>> {
        val newChildren = children.flatMap { child ->
            child.copyModified(modify)
        }
        return modify(this, newChildren)
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
        findFirst(VisualNode<Any, D>::nextNodeInReadingOrder, predicate)

    inline fun <reified T : Any> findPrev(predicate: (VisualNode<T, D>) -> Boolean = { true }): VisualNode<T, D>? =
        findFirst(VisualNode<Any, D>::previousNodeInReadingOrder, predicate)

    fun isBetweenInReadingOrder(start: VisualNode<Any, D>?, end: VisualNode<Any, D>?): Boolean =
        if (start == null || end == null) {
            false
        } else {
            interactiveId >= start.interactiveId && interactiveId <= end.interactiveId
        }

    inline fun <reified T : Any> findFirst(
        successor: VisualNode<Any, D>.() -> VisualNode<Any, D>?,
        predicate: (VisualNode<T, D>) -> Boolean = { true }
    ): VisualNode<T, D>? {
        var currentNode: VisualNode<Any, D> = this.successor() ?: return null
        while (currentNode.data !is T || !predicate(currentNode as VisualNode<T, D>)) {
            currentNode = currentNode.successor() ?: return null
        }
        return currentNode
    }

    inline fun <reified T : Any> findAllSuccessorsWhile(
        successor: VisualNode<Any, D>.() -> VisualNode<Any, D>?,
        predicate: (VisualNode<T, D>) -> Boolean = { true }
    ): List<VisualNode<T, D>> {
        val result = mutableListOf<VisualNode<T, D>>()
        var currentNode: VisualNode<Any, D>? = this.successor()
        while (currentNode != null) {
            val wantedNode = currentNode typedAs T::class
            if (wantedNode != null) {
                if (predicate(wantedNode)) {
                    result.add(wantedNode)
                } else {
                    break
                }
            }
            currentNode = currentNode.successor()
        }
        return result
    }

    /**
     * Walks the parent node chain while the predicate matches.
     * @return The farthest parent node matching the predicate continuously.
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

    fun findClosestParentMatching(predicate: (VisualNode<Any, D>) -> Boolean): VisualNode<Any, D>? =
        findFirst(VisualNode<Any, D>::parent, predicate)

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
        val text = node.text
        val isAtStart: Boolean = charOffset == 0
        val isAtEnd: Boolean = node.data.text.length == charOffset

        fun <T : Any> insertNode(insertedNode: VisualNode<T, D>): VisualNode<T, D> {
            val parentNode = node.parent ?: throw IllegalStateException("The target node must have a parent.")
            val textBefore = text.take(charOffset)
            val textAfter = text.drop(charOffset)
            val children = mutableListOf<VisualNode<*, D>>()

            var insertedNodeIndex = node.siblingsBefore.size
            children.addAll(node.siblingsBefore)
            if (textBefore.isNotEmpty()) {
                children.add(VisualNode(Text(textBefore)))
                insertedNodeIndex++
            }
            children.add(insertedNode)
            if (textAfter.isNotEmpty()) children.add(VisualNode(Text(textAfter)))
            children.addAll(node.siblingsAfter)
            val newParent = parentNode.replaceWith(
                parentNode.copy(
                    proposedChildren = children
                )
            )

            return newParent.children[insertedNodeIndex].typedAs(insertedNode)!!
        }
    }

    /**
     * Assume this node to be a container of text. Then find a child node (or self), which contains the character
     * at the specific offset from the start of the text in this container.
     */
    fun findTextChildAtOffset(charOffset: Int): TextWithCharOffset<D> {
        var textLengthSoFar = 0
        var currentNode: VisualNode<*, D> = this

        while (textLengthSoFar <= charOffset) {
            val currentTextNode = currentNode.asTextNode
            if (currentTextNode != null) {
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

    fun findOffsetByTextChild(textWithCharOffset: TextWithCharOffset<D>): Int =
        findOffsetByTextChild(textWithCharOffset.node, textWithCharOffset.charOffset)

    val totalTextLength: Int by lazy {
        if (data is HasText) {
            data.text.length
        } else {
            children.sumOf(VisualNode<Any, D>::totalTextLength)
        }
    }

    val totalTextIsEmpty: Boolean by lazy {
        if (data is HasText) {
            data.text.isEmpty()
        } else {
            children.all(VisualNode<Any, D>::totalTextIsEmpty)
        }
    }

    val totalText: String by lazy {
        if (data is HasText) {
            data.text
        } else {
            children
                .map(VisualNode<Any, D>::totalText)
                .filter(String::isNotBlank)
                .joinToString(" ")
        }
    }

    val textLengthBefore: Int by lazy {
        val prevTextNode = findPrev<HasText>() ?: return@lazy 0
        prevTextNode.data.text.length + prevTextNode.textLengthBefore
    }

    override fun toString(): String {
        return "VisualNode(${parent?.data?.let { "parent=" + it::class.simpleName } ?: "<ROOT>"}, data=$data)"
    }

    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = System.identityHashCode(this)

    companion object {
        private fun <D : Any> nil(parent: VisualNode<*, D>) = VisualNode(
            parent = parent,
            data = Text("\uFEFF") // Zero-width space
        )
    }
}

fun <D : Any> commonParent(node1: VisualNode<*, D>, node2: VisualNode<*, D>): VisualNode<*, D> {
    if (node1 == node2) return node1
    val startParents = node1.allParents
    val endParents = node2.allParents

    val commonParent = endParents
        .intersect(startParents.toSet())
        .reduce { a, b -> if (a in b.allParents) a else b }
    return commonParent
}

fun <D : Any> commonParent(vararg nodes: VisualNode<*, D>): VisualNode<*, D> {
    val firstNode = nodes.first()
    return nodes
        .drop(1)
        .fold(firstNode) { commonParent, node ->
            commonParent(commonParent, node)
        }
}

val VisualNode<HasText, *>.text: String get() = data.text

infix fun <T : Any, D : Any> VisualNode<*, D>?.typedAs(dataClass: KClass<T>): VisualNode<T, D>? = if (this == null) {
    null
} else {
    if (dataClass.isInstance(data)) {
        this as VisualNode<T, D>
    } else {
        null
    }
}

infix fun <T : Any, D : Any> VisualNode<*, D>?.typedAs(otherNode: VisualNode<T, D>): VisualNode<T, D>? =
    this typedAs otherNode.data::class