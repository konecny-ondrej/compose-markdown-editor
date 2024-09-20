package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.DocumentTheme

internal class UiBlockQuote : BlockRenderer<BlockQuote, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: BlockQuote) {
        Column(
            modifier = DocumentTheme.current.styles.blockQuote.modifier
        ) {
            renderBlocks(block.children)
        }
    }
}