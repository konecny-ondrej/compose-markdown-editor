package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.LayoutCoordinates
import java.util.concurrent.atomic.AtomicLong

internal typealias InteractiveId = Long

internal const val firstInteractiveId: InteractiveId = 0
internal const val invalidInteractiveId: InteractiveId = -1

class InteractiveScope(
    val cursorPosition: MutableState<CursorPosition> = mutableStateOf(CursorPosition.invalid),
    val selection: MutableState<Selection> = mutableStateOf(Selection.empty)
    // TODO: EditingBuffer?
) {
    private var componentLayout: InteractiveComponentLayout? = null
    private val currentId: AtomicLong = AtomicLong(firstInteractiveId)

    /**
     * Generates ID for interactive components.
     * Automatically remembered so consumers always receive the same ID.
     */
    @Composable
    fun rememberInteractiveId(): InteractiveId =
        rememberSaveable(this, key = System.identityHashCode(this).toString()) {
            var newId = currentId.getAndIncrement()
            while (newId == invalidInteractiveId) {
                newId = currentId.getAndIncrement()
            }
            newId
        }

    internal fun requireComponentLayout(): InteractiveComponentLayout =
        componentLayout ?: throw IllegalStateException("You must place the interactive scope first.")

    val isPlaced: Boolean get() = componentLayout != null

    internal fun place(containerLayoutCoordinates: LayoutCoordinates) {
        componentLayout = InteractiveComponentLayout(containerLayoutCoordinates)
    }

    fun register(component: InteractiveComponent) {
        requireComponentLayout().add(component)
    }

    fun unregister(componentId: InteractiveId) {
        componentLayout?.remove(componentId)
        if (cursorPosition.value.componentId != componentId) return
        cursorPosition.value = CursorPosition.invalid
    }

    fun getComponent(id: InteractiveId): InteractiveComponent = requireComponentLayout().getComponent(id)
}

