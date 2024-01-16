package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.TextInputCommand

interface AutocompletePlugin {
    fun generateSuggestions(editorState: MarkdownEditorState): List<AutocompleteSuggestion>
}

data class AutocompleteSuggestion(
    val render: @Composable RowScope.() -> Unit,
    val onClick: (handleInput: (TextInputCommand) -> Unit) -> Unit
)