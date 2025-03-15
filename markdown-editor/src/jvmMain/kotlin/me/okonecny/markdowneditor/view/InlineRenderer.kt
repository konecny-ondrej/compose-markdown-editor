package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.TextWithInlines
import me.okonecny.wysiwyg.ast.VisualNode

interface InlineRenderer<in T : Any, D : Any> {
    @Composable
    fun RenderContext<D>.render(inlineNode: VisualNode<T, D>): TextWithInlines
}