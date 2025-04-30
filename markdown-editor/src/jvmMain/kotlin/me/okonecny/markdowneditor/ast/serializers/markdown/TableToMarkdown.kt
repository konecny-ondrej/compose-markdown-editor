package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.*
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.isSelected
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer
import me.okonecny.wysiwyg.ast.typedAs

class TableToMarkdown<D : Any> : VisualNodeSerializer<Table, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Table, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        // If selection is null, serialize the whole table with markdown syntax
        // If selection is not null, only include markdown syntax for selected cells
        val includeTableSyntax = node.isSelected(selection)

        var headerNode: VisualNode<*, D>? = null
        var bodyNode: VisualNode<*, D>? = null
        var columnCount = 0

        // Find header and body nodes, and determine column count
        node.children.forEach { section ->
            when (section.data) {
                is TableHeader -> {
                    headerNode = section
                    // Count columns from the first row of the header
                    section.children.firstOrNull()?.let { row ->
                        columnCount = maxOf(columnCount, row.children.size)
                    }
                }

                is TableBody -> {
                    bodyNode = section
                    // Count columns from the first row of the body if header is empty
                    if (columnCount == 0) {
                        section.children.firstOrNull()?.let { row ->
                            columnCount = maxOf(columnCount, row.children.size)
                        }
                    }
                }
            }
        }

        // If no columns found, return empty string
        if (columnCount == 0) return@buildAnnotatedString

        // Calculate column widths based on the widest cell in each column
        val columnWidths = calculateColumnWidths(node, columnCount)

        // Process header rows
        headerNode?.let { header ->
            val headerIncludeSyntax = includeTableSyntax && header.isSelected(selection)
            val headerRows = processTableSection(header, selection, columnCount, columnWidths, headerIncludeSyntax)
            append(headerRows)

            // Add separator row with alignment indicators only if table syntax should be included
            if (headerIncludeSyntax) {
                val separatorRow = buildAnnotatedString {
                    append("|")
                    for (i in 0 until columnCount) {
                        val alignment = getColumnAlignment(header, i)
                        when (alignment) {
                            TableCell.Alignment.LEFT -> append(":" + "-".repeat(columnWidths[i] + 1))
                            TableCell.Alignment.CENTER -> append(":" + "-".repeat(columnWidths[i]) + ":")
                            TableCell.Alignment.RIGHT -> append("-".repeat(columnWidths[i] + 1) + ":")
                        }
                        append("|")
                    }
                }
                appendLine()
                append(separatorRow)
                append("\n")
            }
        }

        // Process body rows
        bodyNode?.let { body ->
            val bodyIncludeSyntax = includeTableSyntax && body.isSelected(selection)
            val bodyRows = processTableSection(body, selection, columnCount, columnWidths, bodyIncludeSyntax)
            append(bodyRows)
        }
    }

    private fun VisualNodeSerializationContext<D, AnnotatedString>.processTableSection(
        section: VisualNode<*, D>,
        selection: VisualNodeSelection<D>?,
        columnCount: Int,
        columnWidths: IntArray,
        includeSyntax: Boolean = true
    ): AnnotatedString {
        return section.children.joinToAnnotatedString("\n", filter = AnnotatedString::isNotBlank) { rowNode ->
            if (rowNode.data is TableRow) {
                buildAnnotatedString {
                    // Check if this row is selected
                    val rowIncludeSyntax = includeSyntax && rowNode.isSelected(selection)

                    // Only add an opening pipe if we're including syntax
                    if (rowIncludeSyntax) {
                        append("|")
                    }

                    // Process each cell in the row
                    for (i in 0 until columnCount) {
                        val cellNode = rowNode.children.getOrNull(i) typedAs TableCell::class

                        if (cellNode != null) {
                            // Check if this cell is selected
                            val cellIncludeSyntax = rowIncludeSyntax && cellNode.isSelected(selection)

                            // Only add spacing if we're including syntax
                            if (cellIncludeSyntax) {
                                append(" ")
                            }

                            // Get cell content
                            val cellContent = cellNode.children.joinToAnnotatedString("") { childNode ->
                                serialize(childNode, selection)
                            }
                            // Replace newlines with spaces in cell content
                            val cellText = cellContent.text.replace('\n', ' ')

                            // Pad cell content according to alignment
                            val alignment = cellNode.data.alignment

                            val paddedText = when (alignment) {
                                TableCell.Alignment.LEFT -> cellText.padEnd(columnWidths[i])
                                TableCell.Alignment.RIGHT -> cellText.padStart(columnWidths[i])
                                TableCell.Alignment.CENTER -> {
                                    val leftPadding = (columnWidths[i] - cellText.length) / 2
                                    val rightPadding = columnWidths[i] - cellText.length - leftPadding
                                    " ".repeat(leftPadding) + cellText + " ".repeat(rightPadding)
                                }
                            }

                            append(paddedText)

                            // Only add spacing if we're including syntax
                            if (cellIncludeSyntax) {
                                append(" ")
                            }
                        } else {
                            // Empty cell
                            if (rowIncludeSyntax) {
                                append(" ".repeat(columnWidths[i] + 2))
                            }
                        }

                        // Only add pipe separator if we're including syntax
                        if (rowIncludeSyntax && i < columnCount - 1) {
                            append("|")
                        } else if (rowIncludeSyntax && i == columnCount - 1) {
                            append("|")
                        }
                    }
                }
            } else {
                AnnotatedString("")
            }
        }
    }

    private fun getColumnAlignment(headerSection: VisualNode<*, D>, columnIndex: Int): TableCell.Alignment {
        // Get alignment from the first row's cell at the given column index
        headerSection.children.firstOrNull()?.let { row ->
            row.children.getOrNull(columnIndex)?.let { cell ->
                val cellData = cell.data
                if (cellData is TableCell) {
                    return cellData.alignment
                }
            }
        }
        // Default to left alignment
        return TableCell.Alignment.LEFT
    }

    /**
     * Calculate the width of each column based on the widest cell in each column.
     * Returns an array of column widths.
     */
    private fun VisualNodeSerializationContext<D, AnnotatedString>.calculateColumnWidths(
        tableNode: VisualNode<Table, D>,
        columnCount: Int
    ): IntArray {
        val columnWidths = IntArray(columnCount) { 0 }

        // Process all rows in the table to find the widest cell in each column
        tableNode.children.forEach { section ->
            section.children.forEach { rowNode ->
                if (rowNode.data is TableRow) {
                    for (i in 0 until columnCount) {
                        val cellNode = rowNode.children.getOrNull(i) typedAs TableCell::class
                        if (cellNode != null) {
                            // Get cell content
                            val cellContent = cellNode.children.joinToAnnotatedString("") { childNode ->
                                serialize(childNode, null)
                            }
                            // Update the column width if this cell is wider
                            val cellWidth = cellContent.text.replace('\n', ' ').length
                            columnWidths[i] = maxOf(columnWidths[i], cellWidth)
                        }
                    }
                }
            }
        }

        return columnWidths
    }
}
