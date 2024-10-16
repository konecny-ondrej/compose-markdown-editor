package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.OrderedListItem
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiOrderedListItem : BlockRenderer<OrderedListItem, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<OrderedListItem>) {
        val bullet = LocalListItemBullet.current
        Row {
            InteractiveText(
                interactiveId = block.interactiveId,
                text = bullet,
                textMapping = BoundedBlockTextMapping(
                    coveredSourceRange = block.sourceRange,
                    visualTextRange = TextRange(0, bullet.length)
                ),
                style = DocumentTheme.current.styles.listNumber
            )
            Column {
                renderBlocks(block.children)
            }
        }
    }
}
