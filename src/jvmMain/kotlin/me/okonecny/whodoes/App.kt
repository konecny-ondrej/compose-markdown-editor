package me.okonecny.whodoes

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.useResource
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor
import me.okonecny.markdowneditor.MarkdownView
import me.okonecny.markdowneditor.codefence.ExampleRenderer
import me.okonecny.markdowneditor.inline.WebLink
import me.okonecny.markdowneditor.rememberMarkdownEditorState
import me.okonecny.markdowneditor.toolbar.FloatingTextToolbar
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import kotlin.io.path.Path

@Composable
@Preview
fun App() {
    var isLong by remember { mutableStateOf(false) }
    val shortFilename = "/short.md"
    val longFilename = "/gfmSpec.md"
    val filename = if (isLong) longFilename else shortFilename
    val markdownSource by mutableStateOf(useResource(filename) { md ->
        md.bufferedReader().readText()
    })

    var editorState by rememberMarkdownEditorState(markdownSource, filename)

    MaterialTheme {
        Column {
            DefaultButton(onClick = {
                isLong = !isLong
            }) {
                Text(if (isLong) "GFM Spec" else "Short demo")
            }

            val documentTheme = DocumentTheme.default
            MarkdownEditor(
                editorState = editorState,
                documentTheme = documentTheme,
                onChange = { newEditorState ->
                    editorState = newEditorState
                }
            ) {
                // TODO: implement proper WYSIWYG / source / both modes.
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
                FloatingToolbar {
                    FloatingTextToolbar(editorState)
                }
            }
        }
    }
}