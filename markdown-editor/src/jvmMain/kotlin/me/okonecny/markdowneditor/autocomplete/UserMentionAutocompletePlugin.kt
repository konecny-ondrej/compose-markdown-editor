package me.okonecny.markdowneditor.autocomplete

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.Type
import me.okonecny.wysiwyg.AutocompletePlugin
import me.okonecny.wysiwyg.AutocompleteSuggestion
import me.okonecny.wysiwyg.WysiwygEditorState

class UserMentionAutocompletePlugin(
    private val userNames: List<String>
) : AutocompletePlugin {
    override val name: String = "Users"

    override fun generateSuggestions(editorState: WysiwygEditorState): List<AutocompleteSuggestion> {
        // TODO: the default behaviour of autocomplete plugins out
        val contextWord = editorState.autocompleteContextWord
        if (!contextWord.startsWith("@")) return emptyList()

        val userNamePrefix = contextWord.substring(1)
        if (userNamePrefix.isEmpty()) return emptyList()

        val suggestions = userNames.filter { userName -> userName.startsWith(userNamePrefix) }
        if (suggestions.size == 1 && suggestions.first() == userNamePrefix) return emptyList()

        return suggestions
            .map { userName ->
                AutocompleteSuggestion(
                    render = @Composable {
                        Text(userName)
                    },
                    onClick = { handleInput ->
                        val userTag = "@$userName"
                        handleInput(Type(userTag.remainingText(contextWord)))
                        editorState.interactiveScope.focusRequester.requestFocus()
                    }
                )
            }
    }
}