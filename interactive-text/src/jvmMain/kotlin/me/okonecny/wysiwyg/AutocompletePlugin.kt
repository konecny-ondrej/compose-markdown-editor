package me.okonecny.wysiwyg

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.TextInputCommand

/**
 * Plugin for suggesting fast text completions to the user in the form of a popup menu, which appears at the text cursor.
 */
interface AutocompletePlugin {
    /**
     * Name of the plugin to be shown to the user in the autocomplete menu.
     */
    val name: String
    /**
     * Generate the suggestions to show to the user.
     */
    fun generateSuggestions(editorState: WysiwygEditorState): List<AutocompleteSuggestion>
}

/**
 * Represents one suggestion for fast completion.
 */
data class AutocompleteSuggestion(
    /**
     * Render the content of the popup menu item, which will correspond to the suggestion.
     */
    val render: @Composable RowScope.() -> Unit,
    /**
     * Action to be executed when the user selects this suggestion. The method will be passed the editor's input
     * handler as a parameter, so you can do edits to the source.
     */
    val onClick: (handleInput: (TextInputCommand) -> Unit) -> Unit
)