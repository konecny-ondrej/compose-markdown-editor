package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.TaskListItem
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal const val LIST_BULLET = "\u2022"
internal val LocalListItemBullet = compositionLocalOf { LIST_BULLET }

internal class UiTaskListItem : BlockRenderer<TaskListItem, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<TaskListItem>) {
        val styles = DocumentTheme.current.styles
        Row {
            InteractiveText(
                interactiveId = block.interactiveId,
                text = LocalListItemBullet.current,
                textMapping = BoundedBlockTextMapping(
                    visualTextRange = TextRange(0, 1),
                    coveredSourceRange = TextRange(0, LocalListItemBullet.current.length)
                ),
                style = styles.listNumber
            )
            Checkbox(
                modifier = styles.taskListCheckbox.modifier,
                checked = block.data.isDone,
                onCheckedChange = { isChecked ->
                    TODO()
                })
            Column {
                renderBlocks(block.children)
            }
        }
    }
}
