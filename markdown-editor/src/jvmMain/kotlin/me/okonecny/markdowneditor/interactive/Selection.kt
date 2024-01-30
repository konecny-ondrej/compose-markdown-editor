package me.okonecny.markdowneditor.interactive

import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveScope
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.compose.onlyIncludedIndexes
import me.okonecny.markdowneditor.flexmark.includingParents

fun Selection.spansMultipleLeafNodes(scope: InteractiveScope): Boolean {
    return touchedNodes(scope)
        .filter { node ->
            !node.hasChildren()
        }
        .size > 1
}

fun Selection.touchedNodes(scope: InteractiveScope): List<Node> {
    if (isEmpty) return emptyList()

    val borderComponents = listOf(start, end)
        .map(CursorPosition::componentId)
        .map(scope::getComponent)
    val (startComponent, endComponent) = borderComponents

    val selectedComponents = scope.componentsBetween(
        startComponent,
        endComponent
    )
    val sourceSelection = computeSourceSelection(scope).onlyIncludedIndexes

    if (startComponent == endComponent) {
        return startComponent.nodesAfterOrAt(sourceSelection.first)
            .intersect(startComponent.nodesBeforeOrAt(sourceSelection.last).toSet())
            .toList()
    }

    val touchedNodes: List<Node> = selectedComponents
        .flatMap { component ->
            val componentRootNode = component.rootNode ?: return@flatMap emptyList()
            if (component == startComponent) return@flatMap component.nodesAfterOrAt(sourceSelection.first)
            if (component == endComponent) return@flatMap component.nodesBeforeOrAt(sourceSelection.last)

            componentRootNode.descendants + componentRootNode
        }
    return touchedNodes.includingParents
}

inline fun <reified T : Node> Selection.touchedNodesOfType(
    scope: InteractiveScope,
    sourceCursor: Int? = null
): List<T> {
    return touchedNodes(scope)
        .filterIsInstance<T>()
        .ifEmpty {
            if (sourceCursor == null) return@ifEmpty emptyList()
            val componentUnderCursor = scope.componentAtSource(sourceCursor)
            val nodeAtSource = componentUnderCursor.nodeAtSource<T>(sourceCursor)
            if (nodeAtSource == null) emptyList() else listOf(nodeAtSource)
        }
}
