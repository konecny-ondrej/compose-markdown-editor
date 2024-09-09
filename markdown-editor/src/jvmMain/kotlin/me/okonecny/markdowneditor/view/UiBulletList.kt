package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.util.ast.Block
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme

internal class UiBulletList : BlockRenderer<BulletList> {
    @Composable
    override fun BlockRenderContext.render(block: BulletList) {
        val styles = DocumentTheme.current.styles
        val bullet = "\u2022"
        Column {
            block.children.forEach { child ->
                when (child) {
                    is TaskListItem -> {
                        CompositionLocalProvider( // TODO: move out a little to wrap the whole "when" statement.
                            LocalListItemBullet provides LIST_BULLET
                        ) {
                            renderBlock(child)
                        }
                    }

                    is BulletListItem -> Row { // TODO: separate out into its own renderer
                        InteractiveText(
                            interactiveId = document.getInteractiveId(child),
                            text = bullet,
                            textMapping = SequenceTextMapping(
                                coveredVisualRange = TextRange(0, 1),
                                sequence = child.openingMarker
                            ),
                            style = styles.listNumber
                        )
                        Column {
                            renderBlocks(child.children.filterIsInstance<Block>())
                        }
                    }

                    else -> UiUnparsedBlock(child)
                }
            }
        }
    }
}