package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.sequence.BasedSequence
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.LocalDocument
import me.okonecny.markdowneditor.UiBlock
import me.okonecny.markdowneditor.flexmark.range

@Composable
internal fun UiTaskListItem(
    taskListItem: TaskListItem,
    bulletOrDelimiter: String,
    openingMarker: BasedSequence,
    number: Int? = null
) {
    val styles = DocumentTheme.current.styles
    val document = LocalDocument.current
    Row {
        InteractiveText(
            interactiveId = document.getInteractiveId(taskListItem),
            text = if (taskListItem.isOrderedItem) {
                number.toString() + bulletOrDelimiter
            } else {
                bulletOrDelimiter
            },
            textMapping = SequenceTextMapping(
                coveredVisualRange = TextRange(0, 1),
                sequence = openingMarker
            ),
            style = styles.listNumber
        )
        val onInput = LocalInteractiveInputHandler.current
        Checkbox(
            modifier = styles.taskListCheckbox.modifier,
            checked = taskListItem.isItemDoneMarker,
            onCheckedChange = { isChecked ->
                val taskMarkerRange = taskListItem.markerSuffix.range
                val newMarker = if (isChecked) "[X]" else "[ ]"
                onInput(ReplaceRange(taskMarkerRange, newMarker))
            })
        Column {
            taskListItem.children.forEach { listItemContent ->
                UiBlock(listItemContent)
            }
        }
    }
}