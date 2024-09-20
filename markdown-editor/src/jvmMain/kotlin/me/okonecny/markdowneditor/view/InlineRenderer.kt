package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.MappedText

interface InlineRenderer<in T : BaseNode, BaseNode> {
    @Composable
    fun RenderContext<BaseNode>.render(inlineNode: T): MappedText
}