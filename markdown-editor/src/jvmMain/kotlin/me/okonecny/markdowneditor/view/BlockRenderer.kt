package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable

interface BlockRenderer<T : Any> {
    @Composable
    fun RenderContext.render(block: T)
}