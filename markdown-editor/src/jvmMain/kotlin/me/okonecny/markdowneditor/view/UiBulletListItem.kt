package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.BulletListItem
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiBulletListItem : BlockRenderer<BulletListItem, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<BulletListItem, FlexmarkDocument>) {
        val bullet = LocalListItemBullet.current
        Row {
            InteractiveText(
                interactiveId = block.interactiveId,
                text = bullet,
                style = DocumentTheme.current.styles.listNumber
            )
            Column {
                renderBlocks(block.children)
            }
        }
    }
}
