package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.ListItem
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme

internal class UiListItem : BlockRenderer<ListItem, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: ListItem) {
        val bullet = LocalListItemBullet.current
        Row {
            InteractiveText(
                interactiveId = document.getInteractiveId(block),
                text = bullet,
                textMapping = SequenceTextMapping(
                    coveredVisualRange = TextRange(0, bullet.length),
                    sequence = block.openingMarker
                ),
                style = DocumentTheme.current.styles.listNumber
            )
            Column {
                renderBlocks(block.children)
            }
        }
    }
}
