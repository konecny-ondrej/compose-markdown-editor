import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.okonecny.markdowneditor.MarkdownEditor
import me.tatarka.inject.annotations.Component

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    val markdownSource = remember {
        useResource("/short.md") { md ->
            md.bufferedReader().readText()
        }
    }

    MaterialTheme {
        Column {
            Button(onClick = {
                text = "Hello, Desktop!"
            }) {
                Text(text)
            }
            MarkdownEditor(markdownSource)
        }
    }
}

fun main() = application {
    MainComponent::class.create()
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Component
abstract class MainComponent {

}