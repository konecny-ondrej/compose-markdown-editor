package me.okonecny.wysiwyg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.interactivetext.textInput

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
        .shadow(
            elevation = 8.dp,
            shape = MaterialTheme.shapes.small
        )
        .clip(MaterialTheme.shapes.small)
        .background(
            color = MaterialTheme.colors.background
        )
        .padding(8.dp)
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
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colors.onBackground
            )
            suggestions.forEach { suggestion ->
                val suggestionIsActive = (globalSuggestionIndex++) == selectedSuggestionIndex
                Row(
                    modifier = Modifier
                        .clip(shape = MaterialTheme.shapes.small)
                        .clickable { suggestion.onClick(handleInput) }
                        .background(
                            color = if (suggestionIsActive) MaterialTheme.colors.primarySurface else Color.Transparent
                        )
                        .padding(4.dp)
                        .fillMaxWidth()
                ) {
                    with(suggestion) {
                        ProvideTextStyle(
                            if (suggestionIsActive) {
                                TextStyle(color = contentColorFor(MaterialTheme.colors.primarySurface))
                            } else {
                                TextStyle()
                            }
                        ) {
                            render()
                        }
                    }
                }
            }
        }
    }
}
