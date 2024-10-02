package me.okonecny.markdowneditor.autocomplete

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import me.okonecny.interactivetext.Type
import me.okonecny.markdowneditor.view.inline.annotatedString
import me.okonecny.markdowneditor.view.inline.unicodeString
import me.okonecny.wysiwyg.AutocompletePlugin
import me.okonecny.wysiwyg.AutocompleteSuggestion
import me.okonecny.wysiwyg.WysiwygEditorState

class EmojiAutocompletePlugin : AutocompletePlugin {
    override val name: String = "Emoji"

    override fun generateSuggestions(editorState: WysiwygEditorState): List<AutocompleteSuggestion> {
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

val WysiwygEditorState.autocompleteContextWord: String
    get() = (sourceCursor ?: sourceCursorRequest)?.let { cursor ->
        sourceText.wordBefore(cursor)
    } ?: ""

fun String.remainingText(prefix: String): String {
    if (startsWith(prefix)) return substring(prefix.length)
    return this
}

private fun String.wordBefore(pos: Int): String {
    if (pos <= 0) return ""
    if ("\\s".toRegex().matches(this.substring(pos - 1, pos))) return ""
    return "\\S+".toRegex()
        .findAll(this.substring(0, pos))
        .lastOrNull()
        ?.value
        ?: ""
}

private fun String.isMaybeEmojiStart() = matches("^:[^:]+$".toRegex())