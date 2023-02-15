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
    private val registeredComponents: MutableMap<InteractiveId, InteractiveComponent> = mutableMapOf()
    private val interactiveComponents: MutableList<InteractiveComponent> = mutableListOf()
    private var sorted: Boolean = false
    private val sortedInteractiveComponents: List<InteractiveComponent>
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

    fun register(component: InteractiveComponent) {
        if (registeredComponents.contains(component.id)) return
        registeredComponents[component.id] = component
        interactiveComponents.add(component)
        sorted = false
    }

    fun getComponent(id: InteractiveId): InteractiveComponent = registeredComponents[id]
        ?: throw IllegalStateException("Interactive component with id $id has not been registered.")

    fun nextTo(component: InteractiveComponent): InteractiveComponent {
        val registeredComponent = registeredComponents[component.id]
            ?: throw IllegalStateException("Interactive component with id ${component.id} has not been registered.")
        val registeredPosition = sortedInteractiveComponents.indexOf(registeredComponent)
        return sortedInteractiveComponents[
            (registeredPosition + 1).coerceAtMost(sortedInteractiveComponents.lastIndex)
        ]
    }

    fun prevTo(component: InteractiveComponent): InteractiveComponent {
        val registeredComponent = registeredComponents[component.id]
            ?: throw IllegalStateException("Interactive component with id ${component.id} has not been registered.")
        val registeredPosition = sortedInteractiveComponents.indexOf(registeredComponent)
        return sortedInteractiveComponents[
            (registeredPosition - 1).coerceAtLeast(0)
        ]
    }

    fun above(component: InteractiveComponent): InteractiveComponent {
        return component // TODO
    }

    fun below(component: InteractiveComponent): InteractiveComponent {
        return component // TODO
    }

    private fun sortInteractiveComponentsVisually(): List<InteractiveComponent> {
        if (!sorted) {
            interactiveComponents.sortWith(::linearComparison)
            sorted = true
        }
        return interactiveComponents
    }

    private fun linearComparison(a: InteractiveComponent, b: InteractiveComponent): Int {
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

