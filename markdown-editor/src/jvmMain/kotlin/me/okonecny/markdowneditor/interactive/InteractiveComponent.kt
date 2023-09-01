package me.okonecny.markdowneditor.interactive

import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveComponent

val InteractiveComponent.rootNode: Node? get() = if (hasData<Node>()) get(Node::class) else null

inline fun <reified T : Node> InteractiveComponent.nodeAtSource(sourceCursor: Int): T? {
    val componentRootNode = rootNode ?: return null

    return componentRootNode
        .descendants
        .plus(componentRootNode)
        .filterIsInstance<T>()
        .filter { node ->
            node.sourceRange.contains(sourceCursor)
        }
        .minByOrNull { node ->
            node.sourceRange.span
        }
}

fun InteractiveComponent.nodesBeforeOrAt(sourceCursor: Int): List<Node> {
    val componentRootNode = rootNode ?: return emptyList()

    return componentRootNode
        .descendants
        .filter { node ->
            node.sourceRange.startOffset <= sourceCursor
        }
}

fun InteractiveComponent.nodesAfterOrAt(sourceCursor: Int): List<Node> {
    val componentRootNode = rootNode ?: return emptyList()

    return componentRootNode
        .descendants
        .filter { node ->
            node.sourceRange.endOffset > sourceCursor
        }
}