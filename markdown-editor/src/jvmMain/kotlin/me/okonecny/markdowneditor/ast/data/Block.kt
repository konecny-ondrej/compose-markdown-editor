package me.okonecny.markdowneditor.ast.data

import me.okonecny.markdowneditor.view.LIST_BULLET

data class Heading(
    val level: Level
) {
    enum class Level {
        H1, H2, H3, H4, H5, H6;

        companion object {
            fun forNumericLevel(level: Int) = when (level) {
                1 -> H1
                2 -> H2
                3 -> H3
                4 -> H4
                5 -> H5
                6 -> H6
                else -> H1
            }
        }
    }
}

data object Paragraph
data object BlockQuote
data class BulletList(
    val bullet: String = LIST_BULLET
)

data object BulletListItem
data class OrderedList(
    val startingNumber: Int,
    val delimiter: Char
)

data object OrderedListItem
data class TaskListItem(
    val isDone: Boolean,
)

data object Table
data class TableRow(
    val rowNumber: Int
)

data object TableCell
data class CodeBlock(
    val info: String = ""
)

data object HorizontalRule
data class HtmlBlock(
    val lines: List<String>
)