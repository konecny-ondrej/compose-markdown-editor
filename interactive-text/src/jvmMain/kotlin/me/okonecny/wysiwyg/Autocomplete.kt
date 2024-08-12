package me.okonecny.wysiwyg

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.interactivetext.textInput
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.MenuItemState
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.menuStyle

@Composable
internal fun AutocompletePopup(
    editorState: WysiwygEditorState,
    plugins: List<AutocompletePlugin>,
    handleInput: (TextInputCommand) -> Unit
) {
    var dismissed by remember(editorState) { mutableStateOf(false) }
    if (dismissed) return

    val editorFocusRequester = editorState.interactiveScope.focusRequester
    val visualCursorRect = remember(editorState) { editorState.visualCursorRect }
    if (visualCursorRect == null) {
        LaunchedEffect(Unit) {
            editorFocusRequester.requestFocus()
        }
        return
    }

    val suggestionsByPlugin = remember(editorState.sourceText) {
        plugins.associateWith { plugin ->
            plugin.generateSuggestions(editorState)
        }
    }
    val allSuggestions = suggestionsByPlugin.flatMap { it.value }
    if (allSuggestions.isEmpty()) {
        LaunchedEffect(Unit) {
            editorFocusRequester.requestFocus()
        }
        return
    }

    FloatingToolbar(
        offset = visualCursorRect.bottomLeft,
        alignment = FloatingAlignment.TOP
    ) {
        AutocompleteMenu(
            editorState.sourceText,
            suggestionsByPlugin = suggestionsByPlugin,
            handleInput = handleInput,
            onDismissRequest = {
                dismissed = true
                editorFocusRequester.requestFocus()
            }
        )
    }
}

@Composable
private fun AutocompleteMenu(
    sourceText: String,
    suggestionsByPlugin: Map<AutocompletePlugin, List<AutocompleteSuggestion>>,
    handleInput: (TextInputCommand) -> Unit,
    onDismissRequest: () -> Unit
) {
    val menuFocusRequester = remember { FocusRequester() }
    LaunchedEffect(sourceText) {
        menuFocusRequester.requestFocus()
    }
    var selectedSuggestionIndex by remember(sourceText) { mutableStateOf(0) }
    val allSuggestions = suggestionsByPlugin.values.flatten()
    Column(Modifier
        .border(
            width = JewelTheme.menuStyle.metrics.borderWidth,
            color = JewelTheme.menuStyle.colors.border,
            shape = RoundedCornerShape(JewelTheme.menuStyle.metrics.cornerSize)
        )
        .shadow(
            elevation = JewelTheme.menuStyle.metrics.shadowSize,
            shape = RoundedCornerShape(JewelTheme.menuStyle.metrics.cornerSize)
        )
        .clip(RoundedCornerShape(JewelTheme.menuStyle.metrics.cornerSize))
        .background(
            color = JewelTheme.menuStyle.colors.background
        )
        .padding(JewelTheme.menuStyle.metrics.contentPadding)
        .focusRequester(menuFocusRequester)
        .focusable()
        .width(IntrinsicSize.Max)
        .textInput(handleInput)
        .onKeyEvent { keyEvent ->
            if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
            when (keyEvent.key) {
                Key.Escape -> onDismissRequest()
                Key.Enter -> {
                    allSuggestions[selectedSuggestionIndex].onClick(handleInput)
                    return@onKeyEvent true
                }

                Key.DirectionUp -> selectedSuggestionIndex =
                    if (selectedSuggestionIndex == 0) allSuggestions.size - 1 else selectedSuggestionIndex - 1

                Key.DirectionDown -> selectedSuggestionIndex = (selectedSuggestionIndex + 1) % allSuggestions.size
            }
            false
        }
    ) {
        var globalSuggestionIndex = 0
        suggestionsByPlugin.entries.forEach { (plugin, suggestions) ->
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
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .clickable { suggestion.onClick(handleInput) }
                        .background(
                            color = JewelTheme.menuStyle.colors.itemColors.backgroundFor(
                                MenuItemState.of(
                                    focused = (globalSuggestionIndex++) == selectedSuggestionIndex,
                                    selected = false,
                                    enabled = true
                                )
                            ).value
                        )
                        .padding(JewelTheme.menuStyle.metrics.itemMetrics.contentPadding)
                        .fillMaxWidth()
                ) {
                    with(suggestion) {
                        render()
                    }
                }
            }
        }
    }
}
