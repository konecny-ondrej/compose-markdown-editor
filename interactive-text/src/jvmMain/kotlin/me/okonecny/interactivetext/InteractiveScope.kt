package me.okonecny.interactivetext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.LayoutCoordinates
import java.util.concurrent.atomic.AtomicLong

internal typealias InteractiveId = Long

internal const val firstInteractiveId: InteractiveId = 0
internal const val invalidInteractiveId: InteractiveId = -1

class InteractiveScope(
    val focusRequester: FocusRequester = FocusRequester()
) {
    var cursorPosition: CursorPosition by mutableStateOf(CursorPosition.invalid)
    var selection: Selection by mutableStateOf(Selection.empty)

    private var componentLayout: InteractiveComponentLayout? = null
    private val currentId: AtomicLong = AtomicLong(firstInteractiveId)

    val componentUnderCursor: InteractiveComponent?
        get() = if (!isPlaced || !cursorPosition.isValid) null else {
            getComponent(cursorPosition.componentId)
        }


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

    fun requireComponentLayout(): InteractiveComponentLayout =
        componentLayout ?: throw IllegalStateException("You must place the interactive scope first.")

    val isPlaced: Boolean get() = componentLayout != null

    internal fun place(containerLayoutCoordinates: LayoutCoordinates) {
        componentLayout = InteractiveComponentLayout(containerLayoutCoordinates)
    }

    fun register(component: InteractiveComponent) {
        requireComponentLayout().put(component)
    }

    fun unregister(componentId: InteractiveId) {
        componentLayout?.remove(componentId)
        if (cursorPosition.componentId != componentId) return
        cursorPosition = CursorPosition.invalid
        selection = Selection.empty
    }

    fun getComponent(id: InteractiveId): InteractiveComponent = requireComponentLayout().getComponent(id)
}

