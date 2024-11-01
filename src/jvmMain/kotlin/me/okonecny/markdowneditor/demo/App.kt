package me.okonecny.markdowneditor.demo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.useResource
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor
import me.okonecny.markdowneditor.autocomplete.EmojiAutocompletePlugin
import me.okonecny.markdowneditor.autocomplete.UserMentionAutocompletePlugin
import me.okonecny.markdowneditor.codefence.ExampleRenderer
import me.okonecny.markdowneditor.rememberFlexmarkMarkdownEditorState
import me.okonecny.markdowneditor.view.Renderers
import me.okonecny.markdowneditor.view.flexmarkDefault

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

    var editorState by rememberFlexmarkMarkdownEditorState(markdownSource, filename)

    MaterialTheme {
        Column {
            Button(onClick = {
                isLong = !isLong
            }) {
                Text(if (isLong) "GFM Spec" else "Short demo")
            }

            val documentTheme = DocumentTheme.default
            MarkdownEditor(
                editorState = editorState,
                documentTheme = documentTheme,
                autocompletePlugins = listOf(
                    EmojiAutocompletePlugin(),
                    UserMentionAutocompletePlugin(
                        listOf(
                            "user1", "user2", "alice", "amanda", "bob", "barney"
                        )
                    )
                ),
                renderers = Renderers.flexmarkDefault(
                    codeFenceRenderers = listOf(ExampleRenderer())
                ),
                onChange = { newEditorState -> editorState = newEditorState }
            )
        }
    }
}