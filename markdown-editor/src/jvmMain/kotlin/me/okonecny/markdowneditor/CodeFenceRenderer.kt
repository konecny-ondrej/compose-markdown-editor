package me.okonecny.markdowneditor

import androidx.compose.runtime.Composable
import java.nio.file.Path

interface CodeFenceRenderer {
    val codeFenceType: String

    @Composable
    fun render(code: String, basePath: Path)
}