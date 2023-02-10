package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import me.okonecny.markdowneditor.MarkdownDocument
import java.util.concurrent.atomic.AtomicLong

internal typealias InteractiveId = Long

internal const val firstInteractiveId: InteractiveId = 0

internal class InteractiveScope(
    private val key: Any,
    val cursorPosition: MutableState<CursorPosition> = mutableStateOf(CursorPosition.home),
) {
    private val currentId: AtomicLong = AtomicLong(firstInteractiveId)

    /**
     * Generates ID for interactive components.
     * Automatically remembered so consumers always receive the same ID.
     */
    @Composable
    fun rememberInteractiveId(): InteractiveId =
        rememberSaveable(
            key,
            saver = interactiveIdSaver()
        ) {
            currentId.getAndIncrement()
        }

    private fun interactiveIdSaver() = Saver<InteractiveId, InteractiveId>(
        save = { it },
        restore = { it }
    )
}

internal val LocalInteractiveScope = compositionLocalOf { InteractiveScope("") }

@Composable
internal fun InteractiveContainer(key: Any, interactiveContent: @Composable () -> Unit) {
    val scope: InteractiveScope = remember(key) { InteractiveScope(key) }
    CompositionLocalProvider(
        LocalInteractiveScope provides scope
    ) {
        interactiveContent()
    }
}
