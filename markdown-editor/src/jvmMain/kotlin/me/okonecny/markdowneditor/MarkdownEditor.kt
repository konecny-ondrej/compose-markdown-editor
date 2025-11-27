package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import me.okonecny.markdowneditor.ast.Document
import me.okonecny.markdowneditor.ast.serializers.markdown.markdown
import me.okonecny.markdowneditor.edit.MarkdownNewLineEditor
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.inline.WebLink
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
import me.okonecny.markdowneditor.toolbar.MarkdownToolbar
import me.okonecny.markdowneditor.view.Renderers
import me.okonecny.wysiwyg.AutocompletePlugin
import me.okonecny.wysiwyg.WysiwygEditor
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers
import me.okonecny.wysiwyg.edit.CommandEditors
import kotlin.io.path.Path

@Composable
inline fun <reified D : Document> MarkdownEditor(
    editorState: WysiwygEditorState<D>,
    documentTheme: DocumentTheme,
    autocompletePlugins: List<AutocompletePlugin<D>> = listOf(),
    renderers: Renderers<D>,
    noinline onChange: (newEditorState: WysiwygEditorState<D>) -> Unit
) {
    val editorState by rememberUpdatedState(editorState)
    WysiwygEditor(
        editorState = editorState,
        selectionStyle = documentTheme.styles.selection,
        autocompletePlugins = autocompletePlugins,
        onChange = onChange,
        commandEditors = CommandEditors.basic(
            LocalClipboard.current,
            VisualNodeSerializers.markdown<D>()
        ).withCommandEditor(MarkdownNewLineEditor())
    ) {
        View {
            MarkdownView(
                visualDocument = editorState.visualDocument,
                modifier = Modifier.fillMaxSize(1f),
                documentTheme = documentTheme,
                scrollable = true,
                linkHandlers = listOf(WebLink(LocalUriHandler.current)),
                renderers = renderers
            )
        }
        Toolbar {
            MarkdownToolbar(editorState, onChange)
        }
    }
}

@Composable
fun rememberFlexmarkMarkdownEditorState(
    initialSourceText: String,
    vararg keys: Any?
): MutableState<WysiwygEditorState<FlexmarkDocument>> = remember(keys) {
    val basePath = Path("markdown-editor/src/jvmMain/resources")
    val markdown = MarkdownEditorComponent::class.create()
    val parser = markdown.markdownParser
    val visualDocument = parser.parse(initialSourceText, basePath)
    mutableStateOf(
        WysiwygEditorState(
            visualDocument = visualDocument,
            nodeCursor = null,
            nodeSelection = null
        )
    )
}