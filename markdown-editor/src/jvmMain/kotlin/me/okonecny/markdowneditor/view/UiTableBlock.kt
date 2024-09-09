package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.vladsch.flexmark.ext.tables.*
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.UserData
import me.okonecny.markdowneditor.BlockStyle
import me.okonecny.markdowneditor.DocumentTheme

internal class UiTableBlock : BlockRenderer<TableBlock> {
    @Composable
    override fun BlockRenderContext.render(block: TableBlock) {
        @Composable
        fun UiTableSection(tableSection: Node, cellStyle: BlockStyle) {
            tableSection.children.forEach { tableRow ->
                when (tableRow) {
                    is TableRow -> Row(Modifier.height(IntrinsicSize.Max)) {
                        tableRow.children.forEach { cell ->
                            when (cell) {
                                is TableCell -> {
                                    val inlines = me.okonecny.markdowneditor.parseInlines(cell.children)
                                    InteractiveText(
                                        interactiveId = document.getInteractiveId(cell),
                                        text = inlines.text,
                                        textMapping = inlines.textMapping,
                                        inlineContent = inlines.inlineContent,
                                        style = cellStyle.textStyle.copy(
                                            textAlign = when (cell.alignment) {
                                                TableCell.Alignment.LEFT -> TextAlign.Left
                                                TableCell.Alignment.CENTER -> TextAlign.Center
                                                TableCell.Alignment.RIGHT -> TextAlign.Right
                                                else -> TextAlign.Start
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1.0f)
                                            .then(cellStyle.modifier),
                                        activeAnnotationTags = activeAnnotationTags,
                                        onAnnotationCLick = handleLinks(),
                                        userData = UserData.of(Node::class, cell)
                                    )
                                }

                                else -> UiUnparsedBlock(cell)
                            }
                        }
                    }

                    else -> UiUnparsedBlock(tableRow)
                }
            }
        }

        val styles = DocumentTheme.current.styles
        Column(styles.table.modifier) {
            block.children.forEach { tableSection ->
                when (tableSection) {
                    is TableSeparator -> Unit
                    is TableHead -> UiTableSection(tableSection, styles.table.headerCellStyle)
                    is TableBody -> UiTableSection(tableSection, styles.table.bodyCellStyle)
                    else -> UiUnparsedBlock(tableSection)
                }
            }
        }
    }
}