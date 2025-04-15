package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Paragraph
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.markdowneditor.trim
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext

internal fun <Document : Any> VisualNodeSerializationContext<Document, AnnotatedString>.serializeListItemToMarkdown(
    node: VisualNode<Any, Document>,
    selection: VisualNodeSelection<Document>?,
    prefix: AnnotatedString = AnnotatedString("")
): AnnotatedString = buildAnnotatedString {
    val separator = System.lineSeparator()
    var childrenCount = 0
    val listItemText = node
        .children
        .joinToAnnotatedString(separator = separator, filter = AnnotatedString::isNotBlank) { childNode ->
            val paragraphSeparator = if (childrenCount++ > 0 && childNode.data is Paragraph) {
                System.lineSeparator()
            } else {
                ""
            }
            AnnotatedString(paragraphSeparator) + serialize(childNode, selection).trim(*separator.toCharArray())
        }
    if (listItemText.isBlank()) return@buildAnnotatedString
    append(prefix)
    append(listItemText)
}