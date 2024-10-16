package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.BlockQuote
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiBlockQuote : BlockRenderer<BlockQuote, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<BlockQuote>) {
        Column(
            modifier = DocumentTheme.current.styles.blockQuote.modifier
        ) {
            renderBlocks(block.children)
        }
    }
}