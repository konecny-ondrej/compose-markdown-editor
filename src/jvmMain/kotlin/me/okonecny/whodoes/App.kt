package me.okonecny.whodoes

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.useResource
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor
import me.okonecny.wysiwyg.rememberWysiwygEditorState
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text

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

    var editorState by rememberWysiwygEditorState(markdownSource, filename)

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
                onChange = { newEditorState -> editorState = newEditorState }
            )
        }
    }
}