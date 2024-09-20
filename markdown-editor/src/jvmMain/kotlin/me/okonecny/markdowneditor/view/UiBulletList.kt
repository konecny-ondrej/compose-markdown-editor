package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Node

internal class UiBulletList : BlockRenderer<BulletList, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: BulletList) {
        CompositionLocalProvider(
            LocalListItemBullet provides LIST_BULLET
        ) {
            Column {
                block.children.forEach { child ->
                    when (child) {
                        is TaskListItem -> renderBlock(child)
                        is BulletListItem -> renderBlock(child)
                        else -> renderBlock(child)
                    }
                }
            }
        }
    }
}