package me.okonecny.whodoes

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.useResource
import me.okonecny.interactivetext.rememberInteractiveScope
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor
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
            MarkdownEditor(
                sourceText = markdownSource,
                basePath = Path("markdown-editor/src/jvmMain/resources"),
                interactiveScope = interactiveScope,
                showSource = true,
                documentTheme = documentTheme,
                codeFenceRenderers = listOf(ExampleRenderer()),
                linkHandlers = listOf(WebLink()),
                onChange = { newSource ->
                    markdownSource = newSource
                }
            )
        }
    }
}