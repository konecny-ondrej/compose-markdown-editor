package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.okonecny.markdowneditor.ast.data.OrderedList
import me.okonecny.markdowneditor.ast.data.OrderedListItem
import me.okonecny.markdowneditor.ast.data.TaskListItem
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiOrderedList : BlockRenderer<OrderedList, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<OrderedList>) {
        val list = block.data
        Column {
            block.children.forEachIndexed { index, child ->
                when (child.data) {
                    is TaskListItem, is OrderedListItem -> CompositionLocalProvider(
                        LocalListItemBullet provides (list.startingNumber + index).toString() + list.delimiter
                    ) {
                        renderBlock(child)
                    }

                    else -> renderBlock(child)
                }
            }
        }
    }
}