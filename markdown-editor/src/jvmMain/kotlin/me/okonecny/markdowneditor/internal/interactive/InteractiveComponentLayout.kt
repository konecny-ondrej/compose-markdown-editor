package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import kotlin.math.abs

internal class InteractiveComponentLayout(
    private val containerCoordinates: LayoutCoordinates
) {
    private val registeredComponents: MutableMap<InteractiveId, InteractiveComponent> = mutableMapOf()
    private val components: MutableList<InteractiveComponent> = mutableListOf()
    private var sortedInLineOrder: Boolean = true
    private val componentsInLineOrder: List<InteractiveComponent>
        get() = sortInteractiveComponentsToLines()

    fun add(component: InteractiveComponent) {
        if (registeredComponents.contains(component.id)) return
        registeredComponents[component.id] = component
        components.add(component)
        sortedInLineOrder = false
    }

    fun getComponent(id: InteractiveId): InteractiveComponent = registeredComponents[id]
        ?: throw IllegalStateException("Interactive component with id $id has not been registered.")

    fun remove(component: InteractiveComponent) {
        components.remove(component)
    }

    private fun requireFirstComponent(): InteractiveComponent {
        if (components.isEmpty()) throw IllegalStateException("You need to register at least one interactive component.")
        return components.first()
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
        for (component in components) {
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
        for (component in components) {
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
        for (component in components) {
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
            if (componentBounds.bottom >= visualOffset.y) {
                Float.MAX_VALUE
            } else {
                visualOffset.y - componentBounds.bottom
            }
        }

    /**
     * Finds the closest component, by  both X and Y coordinates, which begins below the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentBelow(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            if (componentBounds.top <= visualOffset.y) {
                Float.MAX_VALUE
            } else {
                componentBounds.top - visualOffset.y
            }
        }

    /**
     * Finds the closest component, by  both X and Y coordinates, which ends left of the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentLeftOf(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            if (componentBounds.right >= visualOffset.x) {
                Float.MAX_VALUE
            } else {
                visualOffset.x - componentBounds.right
            }
        }

    /**
     * Finds the closest component, by  both X and Y coordinates, which begins right of the specified point.
     * If there is no such component, returns the component closest to the specified point.
     */
    fun componentRightOf(visualOffset: Offset): InteractiveComponent =
        findClosestComponent(visualOffset) { componentBounds ->
            if (componentBounds.left <= visualOffset.x) {
                Float.MAX_VALUE
            } else {
                componentBounds.right - visualOffset.x
            }
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
            components.sortWith(::textLineComparison)
            sortedInLineOrder = true
        }
        return components
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