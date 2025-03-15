package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.TextWithInlines
import me.okonecny.wysiwyg.ast.VisualNode

interface RenderContext<Document : Any> {
    val document: Document
    val activeAnnotationTags: Set<String>

    @Composable
    fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit

    fun annotateLinkByHandler(linkText: TextWithInlines, linkUrl: String?): TextWithInlines

    @Composable
    fun renderInlines(inlines: Iterable<VisualNode<Any, Document>>): TextWithInlines

    @Composable
    fun renderInline(inline: VisualNode<Any, Document>): TextWithInlines

    @Composable
    fun renderBlocks(blocks: Iterable<VisualNode<Any, Document>>)

    @Composable
    fun renderBlock(block: VisualNode<Any, Document>)
}