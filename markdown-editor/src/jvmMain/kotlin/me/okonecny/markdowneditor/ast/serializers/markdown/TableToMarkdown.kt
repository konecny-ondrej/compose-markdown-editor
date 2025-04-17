package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Table
import me.okonecny.markdowneditor.ast.data.TableBody
import me.okonecny.markdowneditor.ast.data.TableCell
import me.okonecny.markdowneditor.ast.data.TableHeader
import me.okonecny.markdowneditor.ast.data.TableRow
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.hitsNode
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class TableToMarkdown<D : Any> : VisualNodeSerializer<Table, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Table, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        // If selection is null, serialize the whole table with markdown syntax
        // If selection is not null, only include markdown syntax for selected cells
        val includeTableSyntax = selection == null || selection.hitsNode(node)

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

        // Process header rows
        headerNode?.let { header ->
            val headerIncludeSyntax = includeTableSyntax && (selection == null || selection.hitsNode(header))
            val headerRows = processTableSection(header, selection, columnCount, headerIncludeSyntax)
            append(headerRows)

            // Add separator row with alignment indicators only if table syntax should be included
            if (headerIncludeSyntax) {
                val separatorRow = buildAnnotatedString {
                    append("|")
                    for (i in 0 until columnCount) {
                        val alignment = getColumnAlignment(header, i)
                        append(" ")
                        when (alignment) {
                            TableCell.Alignment.LEFT -> append(":-")
                            TableCell.Alignment.CENTER -> append(":-:")
                            TableCell.Alignment.RIGHT -> append("-:")
                        }
                        append(" |")
                    }
                }
                appendLine()
                append(separatorRow)
                append("\n")
            }
        }

        // Process body rows
        bodyNode?.let { body ->
            val bodyIncludeSyntax = includeTableSyntax && (selection == null || selection.hitsNode(body))
            val bodyRows = processTableSection(body, selection, columnCount, bodyIncludeSyntax)
            append(bodyRows)
        }
    }

    private fun VisualNodeSerializationContext<D, AnnotatedString>.processTableSection(
        section: VisualNode<*, D>,
        selection: VisualNodeSelection<D>?,
        columnCount: Int,
        includeSyntax: Boolean = true
    ): AnnotatedString {
        return section.children.joinToAnnotatedString("\n", filter = AnnotatedString::isNotBlank) { rowNode ->
            if (rowNode.data is TableRow) {
                buildAnnotatedString {
                    // Check if this row is selected
                    val rowIncludeSyntax = includeSyntax && (selection == null || selection.hitsNode(rowNode))

                    // Only add opening pipe if we're including syntax
                    if (rowIncludeSyntax) {
                        append("|")
                    }

                    // Process each cell in the row
                    for (i in 0 until columnCount) {
                        val cellNode = rowNode.children.getOrNull(i)

                        if (cellNode != null && cellNode.data is TableCell) {
                            // Check if this cell is selected
                            val cellIncludeSyntax = rowIncludeSyntax && (selection == null || selection.hitsNode(cellNode))

                            // Only add spacing if we're including syntax
                            if (cellIncludeSyntax) {
                                append(" ")
                            }

                            // Get cell content
                            val cellContent = cellNode.children.joinToAnnotatedString("") { childNode ->
                                serialize(childNode, selection)
                            }
                            // Replace newlines with spaces in cell content
                            append(cellContent.text.replace('\n', ' '))

                            // Only add spacing if we're including syntax
                            if (cellIncludeSyntax) {
                                append(" ")
                            }
                        } else {
                            // Empty cell
                            if (rowIncludeSyntax) {
                                append("   ")
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
                if (cell.data is TableCell) {
                    return (cell.data as TableCell).alignment
                }
            }
        }
        // Default to left alignment
        return TableCell.Alignment.LEFT
    }
}
