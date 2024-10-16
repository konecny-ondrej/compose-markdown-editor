package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.wysiwyg.ast.VisualNode

interface BlockRenderer<in T, D> {
    @Composable
    fun RenderContext<D>.render(block: VisualNode<T>)
}