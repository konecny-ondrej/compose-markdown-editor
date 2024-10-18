package me.okonecny.wysiwyg.ast

import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.InteractiveId
import me.okonecny.interactivetext.LinearInteractiveIdGenerator.Companion.firstInteractiveId
import me.okonecny.lang.only
import me.okonecny.lang.onlyOrNull
import me.okonecny.wysiwyg.ast.data.Text

/**
 * Syntax tree for the editor to work with. The editor will add/remove/replace nodes based on the user actions.
 */
data class VisualNode<out T>(
    val data: T,
    val parentIndex: Int? = null,
    val parent: VisualNode<*>? = null,
    val sourceRange: TextRange, // TODO: remove. Won't be needed.
    private val proposedChildren: List<VisualNode<Any>> = emptyList()
) {
    val isRoot: Boolean = parent == null
    val children: List<VisualNode<Any>> = proposedChildren
        .ifEmpty { if (isRoot) listOf(nil(this)) else emptyList() }
        .mapIndexed { index, childNode ->
            childNode.copy(parent = this, parentIndex = index)
        }

    val root: VisualNode<*> by lazy {
        parent?.root ?: this
    }

    val allSiblings: List<VisualNode<*>> by lazy {
        parent?.children ?: listOf(this)
    }

    val interactiveId: InteractiveId by lazy {
        // Generates interactive ids in reading order.
        // This is just a depth-first pre-order walk of the VisualNode tree.
        // We just want to initialize the interactiveId on each node that we visit, so we don't have to compute them again.
        if (parent == null) return@lazy firstInteractiveId
        val previousNodeByInteractiveId = if (siblingsBefore.isEmpty()) {
            parent
        } else {
            val previousSibling = siblingsBefore.last()
            if (previousSibling.children.isEmpty()) {
                previousSibling
            } else {
                var previousSiblingDeepestRightChild: VisualNode<*> = previousSibling.children.last()
                while (previousSiblingDeepestRightChild.children.isNotEmpty()) {
                    previousSiblingDeepestRightChild = previousSiblingDeepestRightChild.children.last()
                }
                previousSiblingDeepestRightChild
            }
        }
        previousNodeByInteractiveId.interactiveId + 1
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

    val allParents: List<VisualNode<*>> by lazy {
        if (parent == null) {
            emptyList()
        } else {
            listOf(parent) + parent.allParents
        }
    }

    fun isBetweenIncluding(node1: VisualNode<*>, node2: VisualNode<*>): Boolean {
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

    companion object {
        private fun nil(parent: VisualNode<*>) = VisualNode(
            parent = parent,
            data = Text("\uFEFF"), // Zero-width space
            sourceRange = TextRange.Zero
        )
    }
}

fun commonParent(node1: VisualNode<*>, node2: VisualNode<*>): VisualNode<*> {
    if (node1 == node2) return node1
    val startParents = node1.allParents
    val endParents = node2.allParents

    val commonParent = endParents.intersect(startParents.toSet()).only(
        "Both nodes in must be a part of the same tree => there must be one common parent for every pair of nodes."
    )
    return commonParent
}

