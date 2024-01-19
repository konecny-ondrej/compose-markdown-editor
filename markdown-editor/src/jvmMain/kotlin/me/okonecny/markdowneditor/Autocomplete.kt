package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.round
import kotlinx.coroutines.delay
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.textInput
import org.jetbrains.jewel.ui.component.PopupMenu
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun AutocompletePopup(
    editorState: MarkdownEditorState,
    plugins: List<AutocompletePlugin>
) {
    val visualCursorRect = editorState.visualCursorRect ?: return
    val suggestionsByPlugin = remember(editorState.sourceText) {
        plugins.associateWith { plugin ->
            plugin.generateSuggestions(editorState)
        }
    }
    if (suggestionsByPlugin.flatMap { it.value }.isEmpty()) return
    var dismissed by remember(editorState.sourceText) { mutableStateOf(false) }
    if (dismissed) return

    /*
     * Workaround for a bug in PopupMenu where it would crash the app when a key event is received just before the popup
     * is shown. That happens quite often, because the autocomplete is shown in response to the "KEY_TYPED" event,
     * where "KEY_UP" follows right after it.
     * Both Jewel and Material 3 suffer from this issue.
     */
    var openMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200.milliseconds)
        openMenu = true
    }
    if (!openMenu) return

    Box(Modifier.offset { visualCursorRect.bottomLeft.round() }) {
        val handleInput = LocalInteractiveInputHandler.current
        PopupMenu(
            onDismissRequest = { _ -> dismissed = true; false },
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .textInput(handleInput)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                        dismissed = true
                    }
                    false
                }
        ) {
            suggestionsByPlugin.entries.forEachIndexed { pluginIndex, (plugin, suggestions) ->
                suggestions.forEachIndexed { suggestionIndex, suggestion ->
                    selectableItem(
                        selected = pluginIndex == 0 && suggestionIndex == 0,
                        onClick = { suggestion.onClick(handleInput) }
                    ) {
                        Row {
                            with(suggestion) {
                                render()
                            }
                        }
                    }
                }
            }
        }
    }
}

val MarkdownEditorState.autocompleteContextWord: String
    get() = (sourceCursor ?: sourceCursorRequest)?.let { cursor ->
        sourceText.wordBefore(cursor)
    } ?: ""

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
