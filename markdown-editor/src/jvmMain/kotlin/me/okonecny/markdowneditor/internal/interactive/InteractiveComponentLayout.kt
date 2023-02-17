package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import kotlin.math.abs
import kotlin.math.max

internal class InteractiveComponentLayout(
    internal val containerCoordinates: LayoutCoordinates
) {
    private val registeredComponents: MutableMap<InteractiveId, InteractiveComponent> = mutableMapOf()
    private val orderedComponents: MutableList<InteractiveComponent> = mutableListOf()
    private var sortedInLineOrder: Boolean = true
    private val componentsInLineOrder: List<InteractiveComponent>
        get() = sortInteractiveComponentsToLines()

    fun add(component: InteractiveComponent) {
        if (registeredComponents.contains(component.id)) return
        registeredComponents[component.id] = component
        orderedComponents.add(component)
        sortedInLineOrder = false
    }

    fun getComponent(id: InteractiveId): InteractiveComponent = registeredComponents[id]
        ?: throw IllegalStateException("Interactive component with id $id has not been registered.")

    fun remove(componentId: InteractiveId) {
        registeredComponents.remove(componentId)?.let { removedComponent ->
            orderedComponents.remove(removedComponent)
        }
    }

    private fun requireFirstComponent(): InteractiveComponent {
        if (orderedComponents.isEmpty()) throw IllegalStateException("You need to register at least one interactive component.")
        return orderedComponents.first()
    }

    /**
     * Find the visual position of the cursor in the layout.
     */
    fun cursorVisualOffset(cursorPosition: CursorPosition): Offset {
        val component = getComponent(cursorPosition.componentId)
        val componentTextLayout = component.textLayoutResult
        if (!component.hasText || componentTextLayout == null) {
            return containerCoordinates.localCenterPointOf(component)
        }
        val componentCursorRect = componentTextLayout.getCursorRect(cursorPosition.offset)
        return containerCoordinates.localPositionOf(component.layoutCoordinates, componentCursorRect.center)
    }

    /**
     * Finds the component, whose center is the closest to the specified point.
     */
    fun componentClosestTo(visualOffset: Offset): InteractiveComponent {
        fun computeDistance(offset1: Offset, offset2: Offset): Float =
            abs(offset1.x - offset2.x) + abs(offset1.y - offset2.y) // Use geometric instead of Manhattan metric?

        var closestComponent = requireFirstComponent()
        var closestDistance = computeDistance(
            visualOffset,
            containerCoordinates.localCenterPointOf(closestComponent)
        )
        for (component in orderedComponents) {
            val distance = computeDistance(visualOffset, containerCoordinates.localCenterPointOf(component))
            if (distance < closestDistance) {
                closestComponent = component
                closestDistance = distance
            }
        }
        return closestComponent
    }

    /**
     * Finds the component, which contains the point specified by the offset.
     * If components overlap, returns the first one it finds.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentAt(visualOffset: Offset): InteractiveComponent {
        for (component in orderedComponents) {
            if (containerCoordinates.localBoundingBoxOf(component).contains(visualOffset)) return component
        }
        return componentClosestTo(visualOffset)
    }

    private fun findClosestComponent(
        visualOffset: Offset,
        computeDistance: (bounds: Rect) -> Float
    ): InteractiveComponent {
        var bestComponent: InteractiveComponent? = null
        var bestDistance = Float.MAX_VALUE
        for (component in orderedComponents) {
            val componentBounds = containerCoordinates.localBoundingBoxOf(component)
            val distance = computeDistance(componentBounds)
            if (distance < bestDistance) {
                bestComponent = component
                bestDistance = distance
            }
        }
        return bestComponent ?: componentAt(visualOffset)
    }


    /**
     * Finds the closest component, by  both X and Y coordinates, which ends above the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentAbove(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            // Don't consider components below.
            if (componentBounds.bottom >= visualOffset.y) return@findClosestComponent Float.MAX_VALUE
            var disadvantage = 0f
            // Prefer components in the vertical general direction.
            disadvantage += max(
                (visualOffset.x - componentBounds.right).coerceAtLeast(0f), // Disadvantage for components left of.
                (componentBounds.left - visualOffset.x).coerceAtLeast(0f) // Disadvantage for components right of.
            ) / 100f // It must not be better to skip a line.
            visualOffset.y - componentBounds.bottom + disadvantage
        }

    /**
     * Finds the closest component, by  both X and Y coordinates, which begins below the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentBelow(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            // Don't consider components above.
            if (componentBounds.top <= visualOffset.y) return@findClosestComponent Float.MAX_VALUE
            var disadvantage = 0f
            // Prefer components in the vertical general direction.
            disadvantage += max(
                (visualOffset.x - componentBounds.right).coerceAtLeast(0f), // Disadvantage for components left of.
                (componentBounds.left - visualOffset.x).coerceAtLeast(0f) // Disadvantage for components right of.
            ) / 100f // It must not be better to skip a line.
            componentBounds.top - visualOffset.y + disadvantage
        }

    /**
     * Finds the closest component, by  both X and Y coordinates, which ends left of the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentLeftOf(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            // Don't consider components right of this one.
            if (componentBounds.right >= visualOffset.x) return@findClosestComponent Float.MAX_VALUE
            visualOffset.x - componentBounds.right
        }

    /**
     * Finds the closest component, by  both X and Y coordinates, which begins right of the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentRightOf(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            // Don't consider components left of this one.
            if (componentBounds.left <= visualOffset.x) return@findClosestComponent Float.MAX_VALUE
            componentBounds.right - visualOffset.x
        }

    fun componentNextOnLineTo(component: InteractiveComponent): InteractiveComponent {
        val registeredComponent = registeredComponents[component.id]
            ?: throw IllegalStateException("Interactive component with id ${component.id} has not been registered.")
        val registeredPosition = componentsInLineOrder.indexOf(registeredComponent)
        return componentsInLineOrder[
            (registeredPosition + 1).coerceAtMost(componentsInLineOrder.lastIndex)
        ]
    }

    fun componentPreviousOnLineFrom(component: InteractiveComponent): InteractiveComponent {
        val registeredComponent = registeredComponents[component.id]
            ?: throw IllegalStateException("Interactive component with id ${component.id} has not been registered.")
        val registeredPosition = componentsInLineOrder.indexOf(registeredComponent)
        return componentsInLineOrder[
            (registeredPosition - 1).coerceAtLeast(0)
        ]
    }

    private fun sortInteractiveComponentsToLines(): List<InteractiveComponent> {
        if (!sortedInLineOrder) {
            orderedComponents.sortWith(::textLineComparison)
            sortedInLineOrder = true
        }
        return orderedComponents
    }

    private fun textLineComparison(a: InteractiveComponent, b: InteractiveComponent): Int {
        val layoutCoordinatesA = a.layoutCoordinates
        val layoutCoordinatesB = b.layoutCoordinates
        // TODO: check that both coordinates are attached.

        val positionA = containerCoordinates.localPositionOf(layoutCoordinatesA, Offset.Zero)
        val positionB = containerCoordinates.localPositionOf(layoutCoordinatesB, Offset.Zero)

        return if (positionA.y == positionB.y) {
            compareValues(positionA.x, positionB.x)
        } else {
            compareValues(positionA.y, positionB.y)
        }
    }

    private fun LayoutCoordinates.localBoundingBoxOf(component: InteractiveComponent): Rect =
        localBoundingBoxOf(component.layoutCoordinates, false)

    private fun LayoutCoordinates.localCenterPointOf(component: InteractiveComponent): Offset =
        localBoundingBoxOf(component).center
}