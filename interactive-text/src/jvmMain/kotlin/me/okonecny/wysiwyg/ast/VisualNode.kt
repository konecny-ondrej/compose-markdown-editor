package me.okonecny.wysiwyg.ast

import androidx.compose.ui.text.TextRange
import me.okonecny.lang.only
import me.okonecny.lang.onlyOrNull
import kotlin.reflect.KClass

/**
 * Syntax tree for the editor to work with. The editor will add/remove/replace nodes based on the user actions.
 */
data class VisualNode<T : Any>(
    val parent: VisualNode<*>? = null,
    val parentIndex: Int? = null,
    val textRange: TextRange,
    private val proposedChildren: List<VisualNode<*>> = emptyList(),

    val dataType: KClass<T>,
    val data: T
) {
    val isRoot: Boolean = parent == null
    val children: List<VisualNode<*>> = proposedChildren
        .ifEmpty { if (isRoot) listOf(nil) else emptyList() }
        .mapIndexed { index, childNode ->
            childNode.copy(parent = this, parentIndex = index)
        }
    val siblings: List<VisualNode<*>> by lazy {
        parent?.children ?: emptyList()
    }
    val siblingsBefore by lazy {
        if (parentIndex == null) {
            emptyList()
        } else {
            siblings.subList(0, parentIndex)
        }
    }
    val siblingsAfter by lazy {
        if (parentIndex == null) {
            emptyList()
        } else {
            siblings.subList(
                (parentIndex + 1).coerceAtMost(siblings.lastIndex),
                siblings.size
            )
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
        private val nil = VisualNode(
            textRange = TextRange(0, 1),
            dataType = NilNode::class,
            data = NilNode
        )

        // We have to start somewhere.
        val emptyDocument = VisualNode(
            parent = null,
            parentIndex = null,
            textRange = TextRange.Zero,
            proposedChildren = listOf(nil),
            dataType = EmptyDocument::class,
            data = EmptyDocument
        )
    }
}

object EmptyDocument
object NilNode

fun commonParent(node1: VisualNode<*>, node2: VisualNode<*>): VisualNode<*> {
    if (node1 == node2) return node1
    val startParents = node1.allParents
    val endParents = node2.allParents

    val commonParent = endParents.intersect(startParents.toSet()).only(
        "Both nodes in must be a part of the same tree => there must be one common parent for every pair of nodes."
    )
    return commonParent
}

