package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import me.okonecny.markdowneditor.autocomplete.EmojiAutocompletePlugin
import me.okonecny.markdowneditor.codefence.ExampleRenderer
import me.okonecny.markdowneditor.inline.WebLink
import me.okonecny.markdowneditor.toolbar.MarkdownToolbar
import me.okonecny.wysiwyg.AutocompletePlugin
import me.okonecny.wysiwyg.WysiwygEditor
import me.okonecny.wysiwyg.WysiwygEditorState
import kotlin.io.path.Path

@Composable
fun MarkdownEditor(
    editorState: WysiwygEditorState,
    documentTheme: DocumentTheme,
    autocompletePlugins: List<AutocompletePlugin> = listOf(EmojiAutocompletePlugin()),
    onChange: (newEditorState: WysiwygEditorState) -> Unit
) {
    WysiwygEditor(
        editorState = editorState,
        selectionStyle = documentTheme.styles.selection,
        autocompletePlugins = autocompletePlugins,
        onChange = onChange
    ) {
        WysiwygView {
            MarkdownView(
                sourceText = editorState.sourceText,
                basePath = Path("markdown-editor/src/jvmMain/resources"),
                modifier = Modifier.fillMaxSize(1f),
                documentTheme = documentTheme,
                scrollable = true,
                codeFenceRenderers = listOf(ExampleRenderer()),
                linkHandlers = listOf(WebLink(LocalUriHandler.current))
            )
        }
        Toolbar { handleInput ->
            MarkdownToolbar(editorState, handleInput)
        }
    }
}