package me.okonecny.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveScope

@Composable
internal fun <T> AutocompletePopup(
    visualCursor: CursorPosition,
    interactiveScope: InteractiveScope,
    state: AutocompleteState<T>,
    renderItem: @Composable RowScope.(T) -> Unit
) {
    if (!visualCursor.isValid) return
    if (!interactiveScope.isPlaced) return
    if (state.isEmpty()) return
    Box(Modifier.absoluteOffset {
        val coords = visualCursor.visualOffset(interactiveScope.requireComponentLayout())
        coords.round()
    }) {
        Column(
            Modifier.shadow(5.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .padding(5.dp)
                .width(IntrinsicSize.Max)
        ) {
            state.itemData.forEachIndexed { index, item ->
                val selectedModifier =
                    if (index != state.selectedItem) Modifier else Modifier.background(Color.Cyan) // TODO: style
                Row(selectedModifier.fillMaxWidth(1f)) {
                    this.renderItem(item)
                }
            }
        }
    }
}

internal data class AutocompleteState<T>(
    val itemData: List<T>,
    val selectedItem: Int = 0,
    private val mapToSource: (T) -> String
) {
    fun isEmpty(): Boolean = itemData.isEmpty()

    fun remainingText(prefix: String): String {
        if (itemData.isEmpty()) return ""
        if (selectedItem !in 0..itemData.lastIndex) return ""
        val source = mapToSource(itemData[selectedItem])
        if (source.startsWith(prefix)) return source.substring(prefix.length)
        return source
    }

    companion object {
        fun <T> empty() = AutocompleteState<T>(emptyList()) { "" }
    }
}

internal fun <T> Modifier.autocompleteNavigation(
    state: AutocompleteState<T>,
    onChange: (newState: AutocompleteState<T>) -> Unit
) = if (state.isEmpty()) this else onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
    when (keyEvent.key) {
        @OptIn(ExperimentalComposeUiApi::class)
        Key.DirectionDown -> {
            onChange(state.copy(selectedItem = (state.selectedItem + 1).coerceAtMost(state.itemData.lastIndex)))
            true
        }

        @OptIn(ExperimentalComposeUiApi::class)
        Key.DirectionUp -> {
            onChange(state.copy(selectedItem = (state.selectedItem - 1).coerceAtLeast(0)))
            true
        }

        else -> false
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
