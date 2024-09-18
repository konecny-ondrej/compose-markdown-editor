package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.MarkdownDocument

interface RenderContext {
    val document: MarkdownDocument
    val activeAnnotationTags: Set<String>

    @Composable
    fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit

    @Composable
    fun parseInlines(inlines: Iterable<Node>): MappedText

    @Composable
    fun <T : Block> renderBlocks(blocks: Iterable<T>)

    @Composable
    fun <T : Block> renderBlock(block: T)
}