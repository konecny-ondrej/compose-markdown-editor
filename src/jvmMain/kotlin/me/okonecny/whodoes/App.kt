package me.okonecny.whodoes

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.useResource
import me.okonecny.interactivetext.rememberInteractiveScope
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor
import me.okonecny.markdowneditor.MarkdownView
import me.okonecny.markdowneditor.UndoManager
import me.okonecny.markdowneditor.codefence.ExampleRenderer
import me.okonecny.markdowneditor.inline.WebLink
import kotlin.io.path.Path

@Composable
@Preview
fun App() {
    var isLong by remember { mutableStateOf(false) }
    val shortFilename = "/short.md"
    val longFilename = "/gfmSpec.md"
    val filename = if (isLong) longFilename else shortFilename
    var markdownSource by mutableStateOf(useResource(filename) { md ->
        md.bufferedReader().readText()
    })
    val interactiveScope = rememberInteractiveScope()

    MaterialTheme {
        Column {
            Button(onClick = {
                isLong = !isLong
            }) {
                Text(if (isLong) "GFM Spec" else "Short demo")
            }

            val documentTheme = DocumentTheme.default
            var undoManager by remember(filename) { mutableStateOf(UndoManager()) }
            MarkdownEditor(
                sourceText = markdownSource,
                interactiveScope = interactiveScope,
                undoManager = undoManager,
                showSource = true,
                documentTheme = documentTheme,
                onChange = { newSource, newUndoManager ->
                    markdownSource = newSource
                    undoManager = newUndoManager
                }
            ) {
                MarkdownView(
                    sourceText = markdownSource,
                    basePath = Path("markdown-editor/src/jvmMain/resources"),
                    modifier = Modifier.fillMaxSize(1f),
                    documentTheme = documentTheme,
                    scrollable = true,
                    codeFenceRenderers = listOf(ExampleRenderer()),
                    linkHandlers = listOf(WebLink(LocalUriHandler.current))
                )
            }
        }
    }
}