package me.okonecny.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    val selectedItem: Int = 0
) {
    fun isEmpty(): Boolean = itemData.isEmpty()

    companion object {
        fun <T> empty() = AutocompleteState<T>(emptyList())
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
