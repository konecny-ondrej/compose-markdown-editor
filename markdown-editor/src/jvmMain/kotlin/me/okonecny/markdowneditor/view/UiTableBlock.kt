package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.BlockStyle
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.*
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiTableBlock : BlockRenderer<Table, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<Table>) {
        @Composable
        fun UiTableSection(tableSection: VisualNode<*>, cellStyle: BlockStyle) {
            tableSection.children.forEach { tableRow ->
                when (tableRow.data) {
                    is TableRow -> Row(Modifier.height(IntrinsicSize.Max)) {
                        tableRow.children.forEach { cell ->
                            when (val cellData = cell.data) {
                                is TableCell -> {
                                    val inlines = renderInlines(cell.children)
                                    InteractiveText(
                                        interactiveId = cell.interactiveId,
                                        text = inlines.text,
                                        textMapping = inlines.textMapping,
                                        inlineContent = inlines.inlineContent,
                                        style = cellStyle.textStyle.copy(
                                            textAlign = when (cellData.alignment) {
                                                TableCell.Alignment.LEFT -> TextAlign.Left
                                                TableCell.Alignment.CENTER -> TextAlign.Center
                                                TableCell.Alignment.RIGHT -> TextAlign.Right
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(1.0f)
                                            .then(cellStyle.modifier),
                                        activeAnnotationTags = activeAnnotationTags,
                                        onAnnotationCLick = handleLinks()
                                    )
                                }

                                else -> renderBlock(cell)
                            }
                        }
                    }

                    else -> renderBlock(tableRow)
                }
            }
        }

        val styles = DocumentTheme.current.styles
        Column(styles.table.modifier) {
            block.children.forEach { tableSection ->
                when (tableSection.data) {
                    is TableHeader -> UiTableSection(tableSection, styles.table.headerCellStyle)
                    is TableBody -> UiTableSection(tableSection, styles.table.bodyCellStyle)
                    else -> renderBlock(tableSection)
                }
            }
        }
    }
}