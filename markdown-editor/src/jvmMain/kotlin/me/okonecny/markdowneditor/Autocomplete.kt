package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.DropdownMenuState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveScope
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.textInput

@Composable
internal fun <T> AutocompletePopup(
    visualCursor: CursorPosition,
    interactiveScope: InteractiveScope,
    suggestions: List<T>,
    onClick: (clickedItem: Int) -> Unit = {},
    renderItem: @Composable RowScope.(T) -> Unit
) {
    if (!visualCursor.isValid) return
    if (!interactiveScope.isPlaced) return
    if (suggestions.isEmpty()) return

    val menuPosition = visualCursor.visualRect(interactiveScope.requireComponentLayout()).bottomCenter
    val menuState = remember {
        DropdownMenuState(DropdownMenuState.Status.Open(menuPosition))
    }

    val focusRequester = remember { FocusRequester() }
    DropdownMenu(
        state = menuState,
        focusable = true,
        modifier = Modifier
            .focusRequester(focusRequester)
            .textInput(LocalInteractiveInputHandler.current)
    ) {
        suggestions.forEachIndexed { index, item ->
            DropdownMenuItem(
                onClick = { onClick(index) }
            ) {
                this.renderItem(item)
            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

fun String.remainingText(prefix: String): String {
    if (startsWith(prefix)) return substring(prefix.length)
    return this
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
