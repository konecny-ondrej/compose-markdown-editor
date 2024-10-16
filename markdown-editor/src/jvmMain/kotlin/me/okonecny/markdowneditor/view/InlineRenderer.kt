package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.MappedText
import me.okonecny.wysiwyg.ast.VisualNode

interface InlineRenderer<in T, D> {
    @Composable
    fun RenderContext<D>.render(inlineNode: VisualNode<T>): MappedText
}