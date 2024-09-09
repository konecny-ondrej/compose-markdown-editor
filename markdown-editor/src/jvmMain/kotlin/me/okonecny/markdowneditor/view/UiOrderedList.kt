package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Block
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme

internal class UiOrderedList : BlockRenderer<OrderedList> {
    @Composable
    override fun BlockRenderContext.render(block: OrderedList) {
        val styles = DocumentTheme.current.styles
        Column {
            var computedNumber: Int = block.startNumber

            block.children.forEach { child ->
                when (child) {
                    is TaskListItem -> {
                        CompositionLocalProvider( // TODO: move out a little to wrap the whole "when" statement.
                            LocalListItemBullet provides (computedNumber++).toString() + block.delimiter.toString()
                        ) {
                            renderBlock(child)
                        }
                    }

                    is OrderedListItem -> Row { // TODO: separate out into its own renderer
                        InteractiveText(
                            interactiveId = document.getInteractiveId(child),
                            text = (computedNumber++).toString() + block.delimiter,
                            textMapping = SequenceTextMapping( // The displayed number generally is not the same as in the source code.
                                coveredVisualRange = TextRange(0, computedNumber.toString().length + 1),
                                sequence = child.openingMarker
                            ),
                            style = styles.listNumber
                        )
                        Column {
                            // Render list item contents.
                            renderBlocks(child.children.filterIsInstance<Block>())
                        }
                    }

                    else -> UiUnparsedBlock(child)
                }

            }
        }
    }
}