package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import me.okonecny.markdowneditor.ast.data.BulletList
import me.okonecny.markdowneditor.ast.data.BulletListItem
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiBulletList : BlockRenderer<BulletList, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<BulletList>) {
        CompositionLocalProvider(
            LocalListItemBullet provides LIST_BULLET
        ) {
            Column {
                block.children.forEach { child ->
                    when (child.data) {
                        is TaskListItem, is BulletListItem -> renderBlock(child)
                        else -> renderBlock(child)
                    }
                }
            }
        }
    }
}