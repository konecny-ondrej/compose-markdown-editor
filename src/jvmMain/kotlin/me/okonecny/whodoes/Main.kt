package me.okonecny.whodoes

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.okonecny.interactivetext.rememberInteractiveScope
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownEditor
import me.okonecny.markdowneditor.codefence.ExampleRenderer
import me.tatarka.inject.annotations.Component

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
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Button(onClick = {
                isLong = !isLong
            }) {
                Text(if (isLong) "GFM Spec" else "Short demo")
            }

            val documentTheme = DocumentTheme.default
            MarkdownEditor(
                sourceText = markdownSource,
                interactiveScope = interactiveScope,
                documentTheme = documentTheme,
                codeFenceRenderers = listOf(ExampleRenderer()),
                onChange = { newSource ->
                    markdownSource = newSource
                }
            )
        }
    }
}

fun main() = application {
    MainComponent::class.create()
    Window(
        onCloseRequest = ::exitApplication,
        title = "WhoDoes",
        icon = painterResource("/app-icon.xml")
        /**
         * Vectors and icons by <a href="https://dribbble.com/trianglesquad?ref=svgrepo.com" target="_blank">Trianglesquad</a>
         * in CC Attribution License via <a href="https://www.svgrepo.com/" target="_blank">SVG Repo</a>
         */
    ) {
        App()
    }
}

@Component
abstract class MainComponent {

}