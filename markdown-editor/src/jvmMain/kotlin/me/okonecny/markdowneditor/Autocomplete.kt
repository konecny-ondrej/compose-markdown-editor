package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.round
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveScope

@Composable
internal fun AutocompletePopup(
    visualCursor: CursorPosition,
    interactiveScope: InteractiveScope,
    content: @Composable () -> Unit
) {
    if (!visualCursor.isValid) return
    if (!interactiveScope.isPlaced) return
    Box(Modifier.absoluteOffset {
        val coords = visualCursor.visualOffset(interactiveScope.requireComponentLayout())
        coords.round()
    }) {
        content()
    }
}

fun String.wordBefore(pos: Int): String {
    if (pos <= 0) return ""
    if ("\\s".toRegex().matches(this.substring(pos - 1, pos))) return ""
    return "\\S+".toRegex()
        .findAll(this.substring(0, pos))
        .lastOrNull()
        ?.value
        ?: ""
}
