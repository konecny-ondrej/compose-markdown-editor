package me.okonecny.markdowneditor.internal

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import me.okonecny.markdowneditor.BlockStyle
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.TableStyle
import me.okonecny.markdowneditor.internal.interactive.InteractiveText

internal data class Cell(
    val text: AnnotatedString,
    val textAlign: TextAlign
)

internal interface TableRowScope {
    val cells: List<Cell>

    @Composable
    fun UiTableCell(text: String, textAlign: TextAlign)

    @Composable
    fun UiTableCell(text: AnnotatedString, textAlign: TextAlign)
}

internal enum class RowType {
    HEADER,
    BODY
}

internal interface TableScope {
    val rows: List<List<Cell>>
    val headerRows: List<List<Cell>>

    @Composable
    fun UiTableRow(rowType: RowType, cells: @Composable TableRowScope.() -> Unit)
}

private class TableScopeImpl : TableScope {
    override val rows: MutableList<List<Cell>> = mutableListOf()
    override val headerRows: MutableList<List<Cell>> = mutableListOf()

    @Composable
    override fun UiTableRow(rowType: RowType, cells: @Composable TableRowScope.() -> Unit) {
        val rowScope = TableRowScopeImpl()
        rowScope.cells()
        when (rowType) {
            RowType.HEADER -> headerRows.add(rowScope.cells)
            RowType.BODY -> rows.add(rowScope.cells)
        }
    }
}

private class TableRowScopeImpl : TableRowScope {
    override val cells: MutableList<Cell> = mutableListOf()

    @Composable
    override fun UiTableCell(text: String, textAlign: TextAlign) {
        cells.add(Cell(AnnotatedString(text), textAlign))
    }

    @Composable
    override fun UiTableCell(text: AnnotatedString, textAlign: TextAlign) {
        cells.add(Cell(text, textAlign))
    }
}

private fun computeWeights(table: TableScope): List<Float> {
    val maxSizes: MutableList<Int> = mutableListOf()
    val allRows = table.headerRows + table.rows
    if (allRows.isEmpty()) return emptyList()

    allRows.forEach { row ->
        if (row.size > maxSizes.size) {
            (1..(row.size - maxSizes.size)).forEach { _ ->
                maxSizes.add(0)
            }
        }
        row.forEachIndexed { columnIndex, column ->
            maxSizes[columnIndex] = maxSizes[columnIndex].coerceAtLeast(column.text.length)
        }
    }
    val totalMaxSize = maxSizes.sum().toFloat()
    val weights = maxSizes.map { columnMaxSize -> columnMaxSize / totalMaxSize }.toMutableList()
    val error = 1f - weights.sum()
    weights[0] += error
    return weights
}

@Composable
internal fun UiTable(
    columnWeights: List<Float>? = null,
    rows: @Composable TableScope.() -> Unit
) {
    val tableStyle: TableStyle = DocumentTheme.current.styles.table
    val tableScope: TableScope = TableScopeImpl()
    tableScope.rows()
    val computedWeights: List<Float> = columnWeights ?: computeWeights(tableScope)
    // TODO: remember the weights so we don't recalculate all the time.

    @Composable
    fun renderRow(row: List<Cell>, cellStyle: BlockStyle) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            row.forEachIndexed { columnIndex, cell ->
                InteractiveText(
                    text = cell.text,
                    textAlign = cell.textAlign,
                    style = cellStyle.textStyle,
                    modifier = cellStyle.modifier
                        .weight(weight = computedWeights[columnIndex])
                        .fillMaxHeight()
                )
            }
        }
    }

    Column(tableStyle.modifier) {
        tableScope.headerRows.forEach { row ->
            renderRow(row, tableStyle.headerCellStyle)
        }
        tableScope.rows.forEach { row ->
            renderRow(row, tableStyle.bodyCellStyle)
        }
    }
}