package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.round
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.textInput
import org.jetbrains.jewel.ui.component.PopupMenu

@Composable
internal fun <T> AutocompletePopup(
    visualCursorRect: Rect?,
    suggestions: List<T>,
    onClick: (clickedItem: T) -> Unit = {},
    renderItem: @Composable RowScope.(T) -> Unit
) {
    if (visualCursorRect == null) return
    if (suggestions.isEmpty()) return
    var dismissed by remember(suggestions) { mutableStateOf(false) }
    if (dismissed) return

    Box(Modifier.offset { visualCursorRect.bottomLeft.round() }) {
        PopupMenu(
            onDismissRequest = { _ -> dismissed = true; false },
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .textInput(LocalInteractiveInputHandler.current)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                        dismissed = true
                    }
                    false
                }
        ) {
            suggestions.forEachIndexed { index, suggestion ->
                selectableItem(
                    selected = index == 0,
                    onClick = { onClick(suggestion) }
                ) {
                    Row { renderItem(suggestion) }
                }
            }
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

fun String.wordRangeAt(pos: Int): IntRange {
    if (isBlank()) return IntRange.EMPTY
    if (pos < 0 || pos > lastIndex) return IntRange.EMPTY

    val whitespacePadding = substring(0..pos)
        .takeLastWhile { !it.isLetterOrDigit() }
        .length
    val charsTillStart = substring(0, (pos - whitespacePadding).coerceAtLeast(0))
        .takeLastWhile { it.isLetterOrDigit() }
        .length
    val wordStart = (pos - whitespacePadding - charsTillStart).coerceAtLeast(0)
    val wordLength = substring(wordStart..lastIndex)
        .takeWhile { it.isLetterOrDigit() }
        .length
    val wordEnd = wordStart + wordLength

    return wordStart until wordEnd
}

fun String.isSurroundedBy(range: IntRange, left: String, right: String = left.reversed()): Boolean {
    val leftRange = IntRange(range.first - left.length, range.first - 1)
    val rightRange = IntRange(range.last + 1, range.last + right.length)
    if (leftRange.first < 0 || rightRange.last > lastIndex) return false
    return substring(leftRange) == left && substring(rightRange) == right
}

fun String.wordAt(pos: Int): String {
    return substring(wordRangeAt(pos))
}
