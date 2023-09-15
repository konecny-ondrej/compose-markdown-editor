package me.okonecny.markdowneditor.flexmark

import com.vladsch.flexmark.util.ast.Node

val List<Node>.includingParents: List<Node>
    get() {
        val leafNodes = this
        val parentNodes = mutableSetOf<Node>()
        for (leafNode in leafNodes) {
            var currentNode: Node? = leafNode.parent
            while (currentNode != null) {
                parentNodes.add(currentNode)
                currentNode = currentNode.parent
            }
        }
        return leafNodes + parentNodes
    }