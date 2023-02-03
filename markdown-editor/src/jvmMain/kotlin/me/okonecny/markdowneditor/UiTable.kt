package me.okonecny.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
private fun RowScope.TableCell(
    text: AnnotatedString,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp)
            .fillMaxHeight()
    )
}

internal interface TableRowScope {
    val cells: List<AnnotatedString>

    @Composable
    fun UiTableCell(text: String)

    @Composable
    fun UiTableCell(text: AnnotatedString)
}

internal interface TableScope {
    val rows: List<List<AnnotatedString>>
    val headerRow: List<AnnotatedString>
    val hasHeader: Boolean

    @Composable
    fun UiTableRow(cells: @Composable TableRowScope.() -> Unit)

    @Composable
    fun UiTableHeaderRow(cells: @Composable TableRowScope.() -> Unit)
}

private class TableScopeImpl : TableScope {
    override val rows: MutableList<List<AnnotatedString>> = mutableListOf()
    override val headerRow: MutableList<AnnotatedString> = mutableListOf()
    override var hasHeader: Boolean = false

    @Composable
    override fun UiTableRow(cells: @Composable TableRowScope.() -> Unit) {
        val rowScope = TableRowScopeImpl()
        rowScope.cells()
        rows.add(rowScope.cells)
    }

    @Composable
    override fun UiTableHeaderRow(cells: @Composable TableRowScope.() -> Unit) {
        val rowScope = TableRowScopeImpl()
        rowScope.cells()
        hasHeader = true
        headerRow.addAll(rowScope.cells)
    }
}

private class TableRowScopeImpl : TableRowScope {
    override val cells: MutableList<AnnotatedString> = mutableListOf()

    @Composable
    override fun UiTableCell(text: String) {
        cells.add(AnnotatedString(text))
    }

    @Composable
    override fun UiTableCell(text: AnnotatedString) {
        cells.add(text)
    }
}

private fun computeWeights(table: TableScope): List<Float> {
    val maxSizes: MutableList<Int> = mutableListOf()
    val allRows = mutableListOf(table.headerRow)
    allRows.addAll(table.rows)
    if (allRows.isEmpty()) return emptyList()

    allRows.forEach { row ->
        if (row.size > maxSizes.size) {
            (1..(row.size - maxSizes.size)).forEach { _ ->
                maxSizes.add(0)
            }
        }
        row.forEachIndexed { columnIndex, column ->
            maxSizes[columnIndex] = maxSizes[columnIndex].coerceAtLeast(column.length)
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
    val tableScope: TableScope = TableScopeImpl()
    tableScope.rows()
    val tableRows = tableScope.rows
    val computedWeights: List<Float> = columnWeights ?: computeWeights(tableScope)
    // TODO: remember the weights so we don't recalculate all the time.

    // Table
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Header row
        if (tableScope.hasHeader) {
            Row(Modifier.background(Color.Gray).height(IntrinsicSize.Max)) {
                tableScope.headerRow.forEachIndexed { columnIndex, columnText ->
                    TableCell(text = columnText, weight = computedWeights[columnIndex])
                }
            }
        }

        // Body rows
        tableRows.forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                row.forEachIndexed { columnIndex, columnText ->
                    TableCell(
                        text = columnText,
                        weight = computedWeights[columnIndex]
                    )
                }
            }
        }
    }
}