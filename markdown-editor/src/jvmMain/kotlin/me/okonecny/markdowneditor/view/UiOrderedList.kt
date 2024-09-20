package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Node

internal class UiOrderedList : BlockRenderer<OrderedList, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: OrderedList) {
        Column {
            var computedNumber: Int = block.startNumber

            block.children.forEach { child ->
                when (child) {
                    is TaskListItem -> CompositionLocalProvider(
                        LocalListItemBullet provides (computedNumber++).toString() + block.delimiter.toString()
                    ) {
                        renderBlock(child)
                    }

                    is OrderedListItem -> CompositionLocalProvider(
                        LocalListItemBullet provides (computedNumber++).toString() + block.delimiter.toString()
                    ) {
                        renderBlock(child)
                    }

                    else -> renderBlock(child)
                }
            }
        }
    }
}