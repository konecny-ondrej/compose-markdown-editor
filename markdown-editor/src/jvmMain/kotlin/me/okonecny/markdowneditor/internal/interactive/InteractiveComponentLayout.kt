package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import kotlin.math.abs

internal class InteractiveComponentLayout {
    private var containerCoordinates: LayoutCoordinates? = null
    private val components: MutableList<InteractiveComponent> = mutableListOf()


    fun place(layoutCoordinates: LayoutCoordinates) {
        containerCoordinates = layoutCoordinates
    }

    fun add(component: InteractiveComponent) {
        components.add(component)
    }

    fun remove(component: InteractiveComponent) {
        components.remove(component)
    }

    private fun requireFirstComponent(): InteractiveComponent {
        if (components.isEmpty()) throw IllegalStateException("You need to register at least one interactive component.")
        return components.first()
    }

    /**
     * Finds the component, whose center is the closest to the specified point.
     */
    fun componentClosestTo(visualOffset: Offset): InteractiveComponent {
        val containerCoordinates = requireContainerCoordinates()
        fun computeDistance(offset1: Offset, offset2: Offset): Float =
            abs(offset1.x - offset2.x) + abs(offset1.y - offset2.y) // Use geometric instead of Manhattan metric?

        var closestComponent = requireFirstComponent()
        var closestDistance = computeDistance(visualOffset, containerCoordinates.localCenterPointOf(closestComponent))
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
        val containerCoordinates = requireContainerCoordinates()
        for (component in components) {
            if (containerCoordinates.localBoundingBoxOf(component).contains(visualOffset)) return component
        }
        return componentClosestTo(visualOffset)
    }

    private fun findClosestComponent(
        visualOffset: Offset,
        computeDistance: (bounds: Rect) -> Float
    ): InteractiveComponent {
        val containerCoordinates = requireContainerCoordinates()
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

    private fun requireContainerCoordinates(): LayoutCoordinates =
        containerCoordinates ?: throw IllegalStateException("Interactive Scope has not been placed yet.")

    private fun LayoutCoordinates.localBoundingBoxOf(component: InteractiveComponent): Rect =
        localBoundingBoxOf(component.layoutCoordinates, false)

    private fun LayoutCoordinates.localCenterPointOf(component: InteractiveComponent): Offset =
        localBoundingBoxOf(component).center
}