package me.okonecny.markdowneditor

import androidx.compose.runtime.Composable

interface CodeFenceRenderer {
    val codeFenceType: String

    @Composable
    fun render(code: MappedText)
}