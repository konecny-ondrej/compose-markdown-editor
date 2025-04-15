package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.OrderedList
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class OrderedListToMarkdown<D : Any> : VisualNodeSerializer<OrderedList, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<OrderedList, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val maxItemNumberChars = node.children.size.toString().length
        var itemNumber = node.data.startingNumber

        val listItems = node.children.joinToAnnotatedString(System.lineSeparator(), filter = AnnotatedString::isNotBlank) { childNode ->
            val childText = serialize(childNode, selection)

            buildAnnotatedString {
                if (childText.isBlank()) return@buildAnnotatedString
                val thisItemNumber = itemNumber++
                val thisItemNumberChars = thisItemNumber.toString().length
                val indentChars = maxItemNumberChars - thisItemNumberChars
                append(thisItemNumber.toString())
                append(node.data.delimiter)
                append(" ".repeat(indentChars))
                val childLines = childText.lines()
                childLines.forEachIndexed { index, line ->
                    if (index > 0) {
                        append(" ".repeat(maxItemNumberChars + 1))
                    }
                    append(line)
                    if (index < childLines.lastIndex) {
                        appendLine()
                    }
                }
            }
        }
        append(listItems)
    }
}