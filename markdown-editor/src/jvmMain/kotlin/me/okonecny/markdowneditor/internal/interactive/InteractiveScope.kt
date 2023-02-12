package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import java.util.concurrent.atomic.AtomicLong

internal typealias InteractiveId = Long

internal const val firstInteractiveId: InteractiveId = 0

class InteractiveScope(
    val cursorPosition: MutableState<CursorPosition> = mutableStateOf(CursorPosition.home),
) {
    internal var containerCoordinates: LayoutCoordinates? = null

    private val currentId: AtomicLong = AtomicLong(firstInteractiveId)
    private val registeredComponents: MutableMap<InteractiveId, Interactive> = mutableMapOf()
    private val interactiveComponents: MutableList<Interactive> = mutableListOf()
    private var sorted: Boolean = false
    private val sortedInteractiveComponents: List<Interactive>
        get() = sortInteractiveComponentsVisually()

    /**
     * Generates ID for interactive components.
     * Automatically remembered so consumers always receive the same ID.
     */
    @Composable
    fun rememberInteractiveId(): InteractiveId =
        rememberSaveable(this, key = System.identityHashCode(this).toString()) {
            currentId.getAndIncrement()
        }

    fun register(component: Interactive) {
        if (registeredComponents.contains(component.id)) return
        registeredComponents[component.id] = component
        interactiveComponents.add(component)
        sorted = false
    }

    fun getComponent(id: InteractiveId): Interactive = registeredComponents[id]
        ?: throw IllegalStateException("Interactive component with id $id has not been registered.")

    fun next(id: InteractiveId): InteractiveId {
        val thisInteractive = registeredComponents[id]
            ?: throw IllegalStateException("Interactive component with id $id has not been registered.")
        val thisPosition = sortedInteractiveComponents.indexOf(thisInteractive)
        return sortedInteractiveComponents[
            (thisPosition + 1).coerceAtMost(sortedInteractiveComponents.lastIndex)
        ].id
    }

    fun prev(id: InteractiveId): InteractiveId {
        val thisInteractive = registeredComponents[id]
            ?: throw IllegalStateException("Interactive component with id $id has not been registered.")
        val thisPosition = sortedInteractiveComponents.indexOf(thisInteractive)
        return sortedInteractiveComponents[
            (thisPosition - 1).coerceAtLeast(0)
        ].id
    }

    private fun sortInteractiveComponentsVisually(): List<Interactive> {
        if (!sorted) {
            interactiveComponents.sortWith(::visualComparison)
            sorted = true
        }
        return interactiveComponents
    }

    private fun visualComparison(a: Interactive, b: Interactive): Int {
        val layoutCoordinatesA = a.layoutCoordinates
        val layoutCoordinatesB = b.layoutCoordinates
        // TODO: check that both coordinates are attached.

        val containerLayoutCoordinates =
            containerCoordinates ?: throw IllegalStateException("Interactive Scope has not been placed yet.")

        val positionA = containerLayoutCoordinates.localPositionOf(layoutCoordinatesA, Offset.Zero)
        val positionB = containerLayoutCoordinates.localPositionOf(layoutCoordinatesB, Offset.Zero)

        return if (positionA.y == positionB.y) {
            compareValues(positionA.x, positionB.x)
        } else {
            compareValues(positionA.y, positionB.y)
        }
    }
}

