package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.BulletList
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class BulletListToMarkdown<D : Any> : VisualNodeSerializer<BulletList, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<BulletList, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val listItems = node.children.joinToAnnotatedString(System.lineSeparator(), filter = AnnotatedString::isNotBlank) { childNode ->
            val childText = serialize(childNode, selection)

            buildAnnotatedString {
                if (childText.isBlank()) return@buildAnnotatedString
                append("- ")
                val childLines = childText.lines()
                childLines.forEachIndexed { index, line ->
                    if (index > 0) {
                        append(" ".repeat(1))
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