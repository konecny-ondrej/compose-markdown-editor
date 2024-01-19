package me.okonecny.markdowneditor.autocomplete

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import me.okonecny.interactivetext.Type
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.inline.annotatedString
import me.okonecny.markdowneditor.inline.isMaybeEmojiStart
import me.okonecny.markdowneditor.inline.unicodeString
import org.jetbrains.jewel.ui.component.Text

class EmojiAutocompletePlugin : AutocompletePlugin {
    override val name: String = "Emoji"

    override fun generateSuggestions(editorState: MarkdownEditorState): List<AutocompleteSuggestion> {
        val contextWord = editorState.autocompleteContextWord
        if (!contextWord.isMaybeEmojiStart()) return emptyList()
        val emojiNamePrefix = contextWord.substring(1)
        if (emojiNamePrefix.isEmpty()) return emptyList()

        val emojis = EmojiReference.getEmojiList()
            .filter { it.shortcut?.startsWith(emojiNamePrefix) ?: false }
            .filter { it.unicodeString.isNotEmpty() }
            .take(5)

        return emojis.map { emoji ->
            AutocompleteSuggestion(
                render = @Composable {
                    Text(emoji.annotatedString)
                    Spacer(Modifier.width(3.dp))
                    Text(":${emoji.shortcut}:")
                },
                onClick = { handleInput ->
                    val emojiTag = ":" + emoji.shortcut + ":"
                    handleInput(Type(emojiTag.remainingText(contextWord)))
                    editorState.interactiveScope.focusRequester.requestFocus()
                }
            )
        }
    }
}
