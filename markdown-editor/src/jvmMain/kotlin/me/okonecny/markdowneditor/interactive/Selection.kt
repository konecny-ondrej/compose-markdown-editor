package me.okonecny.markdowneditor.interactive

import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveComponent
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.compose.onlyIncludedIndexes

fun Selection.computeSourceSelection(
    interactiveComponentLayout: InteractiveComponentLayout
): TextRange {
    if (isEmpty) return TextRange.Zero
    val (startMapping, endMapping) = listOf(start.componentId, end.componentId)
        .map(interactiveComponentLayout::getComponent)
        .map(InteractiveComponent::textMapping)
    return TextRange(
        startMapping.toSource(TextRange(start.visualOffset))?.start ?: 0,
        endMapping.toSource(TextRange(end.visualOffset))?.end ?: 0
    )
}

fun Selection.spansMultipleLeafNodes(layout: InteractiveComponentLayout): Boolean {
    return touchedNodes(layout)
        .filter { node ->
            !node.hasChildren()
        }
        .size > 1
}

fun Selection.touchedNodes(layout: InteractiveComponentLayout): List<Node> {
    if (isEmpty) return emptyList()

    val borderComponents = listOf(start, end)
        .map(CursorPosition::componentId)
        .map(layout::getComponent)
    val (startComponent, endComponent) = borderComponents

    val selectedComponents = layout.componentsBetween(
        startComponent,
        endComponent
    )
    val sourceSelection = computeSourceSelection(layout).onlyIncludedIndexes

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
    return touchedNodes
}

inline fun <reified T : Node> Selection.touchedNodesOfType(layout: InteractiveComponentLayout): List<T> =
    touchedNodes(layout)
        .filterIsInstance<T>()
