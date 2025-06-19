package me.okonecny.markdowneditor.ast.data

import me.okonecny.markdowneditor.view.LIST_BULLET
import me.okonecny.wysiwyg.ast.data.HasText

interface Block

data class Heading(
    val level: Level,
    override val anchorName: String
) : LinkTarget, Block {
    enum class Level {
        H1, H2, H3, H4, H5, H6;

        val numericLevel
            get() = when (this) {
                H1 -> 1
                H2 -> 2
                H3 -> 3
                H4 -> 4
                H5 -> 5
                H6 -> 6
            }

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

data object Paragraph : Block
data object BlockQuote : Block
data class BulletList(
    val bullet: String = LIST_BULLET
) : Block

data object BulletListItem : Block
data class OrderedList(
    val startingNumber: Int,
    val delimiter: Char
) : Block

data object OrderedListItem : Block
data class TaskListItem(
    val isDone: Boolean,
) : Block

data object Table : Block
data object TableHeader
data object TableBody
data class TableRow(
    val rowNumber: Int
)

data class TableCell(
    val alignment: Alignment,
) {
    enum class Alignment {
        LEFT, CENTER, RIGHT
    }
}

data class CodeBlock(
    val info: String = "",
    val code: String
) : HasText, Block {
    override val text: String by ::code
    override fun replaceText(text: String): CodeBlock = copy(code = text)
}

data object HorizontalRule : Block
data class HtmlBlock(
    val lines: List<String>
) : HasText, Block {
    override val text: String get() = lines.joinToString(System.lineSeparator())
    override fun replaceText(text: String): HtmlBlock = HtmlBlock(text.split(System.lineSeparator()))
}