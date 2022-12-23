package me.okonecny.markdowneditor

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Component

@Composable
fun MarkdownEditor() {
    Text("This will be a markdown editor", style = MaterialTheme.typography.h1)
}

@Component
abstract class MarkdownEditorComponent