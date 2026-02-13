package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.OrderedListItem
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiOrderedListItem : BlockRenderer<OrderedListItem, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<OrderedListItem, FlexmarkDocument>) {
        val bullet = LocalListItemBullet.current
        Row {
            InteractiveText(
                node = block,
                text = bullet,
                style = DocumentTheme.current.styles.listNumber
            )
            Column {
                renderBlocks(block.children)
            }
        }
    }
}
