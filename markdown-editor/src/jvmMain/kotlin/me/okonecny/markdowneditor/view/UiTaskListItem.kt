package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Block
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.flexmark.range

internal const val LIST_BULLET = "\u2022"
internal val LocalListItemBullet = compositionLocalOf { LIST_BULLET }

internal class UiTaskListItem : BlockRenderer<TaskListItem> {
    @Composable
    override fun BlockRenderContext.render(block: TaskListItem) {
        val styles = DocumentTheme.current.styles
        Row {
            InteractiveText(
                interactiveId = document.getInteractiveId(block),
                text = LocalListItemBullet.current,
                textMapping = SequenceTextMapping(
                    coveredVisualRange = TextRange(0, 1),
                    sequence = block.openingMarker
                ),
                style = styles.listNumber
            )
            val onInput = LocalInteractiveInputHandler.current
            Checkbox(
                modifier = styles.taskListCheckbox.modifier,
                checked = block.isItemDoneMarker,
                onCheckedChange = { isChecked ->
                    val taskMarkerRange = block.markerSuffix.range
                    val newMarker = if (isChecked) "[X]" else "[ ]"
                    onInput(ReplaceRange(taskMarkerRange, newMarker))
                })
            Column {
                renderBlocks(block.children.filterIsInstance<Block>())
            }
        }
    }
}
