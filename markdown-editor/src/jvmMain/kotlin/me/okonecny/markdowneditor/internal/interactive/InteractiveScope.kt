package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.concurrent.atomic.AtomicLong

internal typealias InteractiveId = Long

internal const val firstInteractiveId: InteractiveId = 0

class InteractiveScope(
    val cursorPosition: MutableState<CursorPosition> = mutableStateOf(CursorPosition.home),
) {
    private val currentId: AtomicLong = AtomicLong(firstInteractiveId)
    private val registeredInteractives: MutableSet<InteractiveId> = mutableSetOf()

    /**
     * Generates ID for interactive components.
     * Automatically remembered so consumers always receive the same ID.
     */
    @Composable
    fun rememberInteractiveId(): InteractiveId =
        rememberSaveable(this, key = System.identityHashCode(this).toString()) {
            currentId.getAndIncrement()
        }

    fun register(id: InteractiveId) { // TODO pass some representation of the interactive element.
        if (registeredInteractives.contains(id)) return
        registeredInteractives.add(id)
    }
}

internal val LocalInteractiveScope = compositionLocalOf<InteractiveScope?> { null }

@Composable
fun InteractiveContainer(
    scope: InteractiveScope? = rememberInteractiveScope(),
    interactiveContent: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalInteractiveScope provides scope
    ) {
        interactiveContent()
    }
}

@Composable
fun rememberInteractiveScope(vararg keys: Any?) = remember(keys) { InteractiveScope() }
