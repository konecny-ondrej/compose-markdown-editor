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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import me.okonecny.interactivetext.*
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MarkdownView
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
            val interactiveScope = rememberInteractiveScope()
            InteractiveContainer(
                scope = interactiveScope,
                selectionStyle = documentTheme.styles.selection,
                onInput = { textInputCommand ->
                    var cursor by interactiveScope.cursorPosition
                    val selection by interactiveScope.selection
                    val layout = interactiveScope.requireComponentLayout()

                    val mapping = layout.getComponent(cursor.componentId).textMapping
                    val sourceCursorPos = mapping.toSource(TextRange(cursor.visualOffset))
                    Logger.d("$cursor", tag = "Cursor")
                    Logger.d(
                        "$textInputCommand@$sourceCursorPos '${markdownSource[sourceCursorPos.start]}'",
                        tag = "onInput"
                    )
                    when (textInputCommand) {
                        Copy -> TODO()
                        is Delete -> TODO()
                        NewLine -> TODO()
                        Paste -> TODO()
                        is Type -> {
                            markdownSource = markdownSource.substring(0, sourceCursorPos.start) +
                                    textInputCommand.text +
                                    markdownSource.substring(sourceCursorPos.end)
                            cursor = interactiveScope.moveCursorRight(cursor)
                        }
                    }
                }
            ) {
                MarkdownView(
                    markdownSource,
                    documentTheme = documentTheme,
                    codeFenceRenderers = listOf(ExampleRenderer())
                )
            }
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