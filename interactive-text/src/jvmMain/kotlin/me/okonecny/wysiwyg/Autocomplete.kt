package me.okonecny.wysiwyg

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.round
import kotlinx.coroutines.delay
import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.interactivetext.textInput
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.MenuItemState
import org.jetbrains.jewel.ui.component.PopupMenu
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.menuStyle
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun AutocompletePopup(
    editorState: WysiwygEditorState,
    plugins: List<AutocompletePlugin>,
    handleInput: (TextInputCommand) -> Unit
) {
    var dismissed by remember(editorState.sourceText) { mutableStateOf(false) }
    if (dismissed) return
    /*
     * Workaround for a bug in PopupMenu where it would crash the app when a key event is received just before the popup
     * is shown. That happens quite often, because the autocomplete is shown in response to the "KEY_TYPED" event,
     * where "KEY_UP" follows right after it.
     * Both Jewel and Material 3 suffer from this issue.
     */
    var openMenu by remember(editorState.sourceText) { mutableStateOf(false) }
    LaunchedEffect(editorState.sourceText) {
        delay(300.milliseconds)
        openMenu = true
    }
    if (!openMenu) return

    val visualCursorRect = editorState.visualCursorRect ?: return // FIXME: Because visualCursorRect is not State, this does not recompose automatically.
    val suggestionsByPlugin = remember(editorState.sourceText) {
        plugins.associateWith { plugin ->
            plugin.generateSuggestions(editorState)
        }
    }
    if (suggestionsByPlugin.flatMap { it.value }.isEmpty()) return

    Box(Modifier.offset { visualCursorRect.bottomLeft.round() }) {
        PopupMenu(
            onDismissRequest = { _ -> dismissed = true; openMenu = false; false },
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
                passiveItem {
                    Text(
                        text = plugin.name,
                        modifier = Modifier.padding(JewelTheme.menuStyle.metrics.itemMetrics.contentPadding),
                        color = JewelTheme.menuStyle.colors.itemColors.contentFor(
                            MenuItemState.of(
                                selected = false,
                                enabled = false
                            )
                        ).value
                    )
                }
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
