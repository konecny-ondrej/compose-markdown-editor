package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.MarkdownDocument

interface RenderContext<in BaseNode> {
    val document: MarkdownDocument
    val activeAnnotationTags: Set<String>

    @Composable
    fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit

    @Composable
    fun renderInlines(inlines: Iterable<BaseNode>): MappedText

    @Composable
    fun renderInline(inline: BaseNode): MappedText

    @Composable
    fun <T : BaseNode> renderBlocks(blocks: Iterable<T>)

    @Composable
    fun <T : BaseNode> renderBlock(block: T)
}