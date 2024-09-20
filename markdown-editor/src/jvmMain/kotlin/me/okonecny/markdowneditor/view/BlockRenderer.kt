package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable

interface BlockRenderer<in T : BaseNode, BaseNode> {
    @Composable
    fun RenderContext<BaseNode>.render(block: T)
}