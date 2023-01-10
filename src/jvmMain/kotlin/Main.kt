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
    var isLong by remember { mutableStateOf(true) }
    val shortFilename = "/short.md"
    val longFilename = "/commonMarkSpec.md"

    MaterialTheme {
        Column {
            Button(onClick = {
                isLong = !isLong
            }) {
                Text(if (isLong) "Make short" else "Make long")
            }
            val filename = if (isLong) longFilename else shortFilename
            val markdownSource = useResource(filename) { md ->
                md.bufferedReader().readText()
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