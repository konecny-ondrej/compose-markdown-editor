package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import me.okonecny.markdowneditor.ast.serializers.markdown.markdown
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.inline.WebLink
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
import me.okonecny.markdowneditor.view.Renderers
import me.okonecny.wysiwyg.AutocompletePlugin
import me.okonecny.wysiwyg.WysiwygEditor
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers
import me.okonecny.wysiwyg.edit.CommandEditors
import kotlin.io.path.Path

@Composable
inline fun <reified D : Any> MarkdownEditor(
    editorState: WysiwygEditorState<D>,
    documentTheme: DocumentTheme,
    autocompletePlugins: List<AutocompletePlugin<D>> = listOf(),
    renderers: Renderers<D>,
    noinline onChange: (newEditorState: WysiwygEditorState<D>) -> Unit
) {
    WysiwygEditor(
        editorState = editorState,
        selectionStyle = documentTheme.styles.selection,
        autocompletePlugins = autocompletePlugins,
        onChange = onChange,
        commandEditors = CommandEditors.basic(
            LocalClipboardManager.current,
            VisualNodeSerializers.markdown<D>()
        )
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
        Toolbar { handleInput ->
            //MarkdownToolbar(editorState, handleInput)
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
    val visualDocument = markdown.markdownParser.parse(initialSourceText, basePath)
    mutableStateOf(
        WysiwygEditorState(
            sourceText = initialSourceText,
            visualDocument = visualDocument
        )
    )
}