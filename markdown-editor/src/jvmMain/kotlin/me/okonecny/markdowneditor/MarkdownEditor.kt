package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import me.okonecny.markdowneditor.codefence.ExampleRenderer
import me.okonecny.markdowneditor.inline.WebLink
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
import me.okonecny.markdowneditor.view.Renderers
import me.okonecny.markdowneditor.view.flexmarkDefault
import me.okonecny.wysiwyg.AutocompletePlugin
import me.okonecny.wysiwyg.WysiwygEditor
import me.okonecny.wysiwyg.WysiwygEditorState
import kotlin.io.path.Path

@Composable
fun <D : Any> MarkdownEditor(
    editorState: WysiwygEditorState<D>,
    documentTheme: DocumentTheme,
    autocompletePlugins: List<AutocompletePlugin<D>> = listOf(),
    onChange: (newEditorState: WysiwygEditorState<D>) -> Unit
) {
    WysiwygEditor(
        editorState = editorState,
        selectionStyle = documentTheme.styles.selection,
        autocompletePlugins = autocompletePlugins,
        onChange = onChange
    ) {
        View {
            val basePath = Path("markdown-editor/src/jvmMain/resources")
            val markdown = remember(basePath) { MarkdownEditorComponent::class.create() }
            val visualDocument = remember(editorState.sourceText, basePath) {
                markdown.markdownParser.parse(
                    editorState.sourceText,
                    basePath
                )
            }
            CompositionLocalProvider(
                LocalMarkdownEditorComponent provides markdown
            ) {
                MarkdownView(
                    visualDocument = visualDocument,
                    modifier = Modifier.fillMaxSize(1f),
                    documentTheme = documentTheme,
                    scrollable = true,
                    linkHandlers = listOf(WebLink(LocalUriHandler.current)),
                    renderers = Renderers.flexmarkDefault(
                        codeFenceRenderers = listOf(ExampleRenderer())
                    )
                )
            }
        }
        Toolbar { handleInput ->
            //MarkdownToolbar(editorState, handleInput)
        }
    }
}