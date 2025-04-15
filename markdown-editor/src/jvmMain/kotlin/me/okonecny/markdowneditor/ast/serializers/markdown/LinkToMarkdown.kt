package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Link
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class LinkToMarkdown<D : Any> : VisualNodeSerializer<Link, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Link, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val linkText =
            node.children.joinToAnnotatedString(" ", filter = AnnotatedString::isNotBlank) { childNode ->
                serialize(childNode, selection)
            }
        if (linkText.isBlank()) return@buildAnnotatedString

        append("[")
        append(linkText)
        append("]")
        if (node.data.target.isBlank() && node.data.title.isNullOrBlank()) return@buildAnnotatedString

        append("(")
        append(node.data.target)
        val title = node.data.title
        if (!title.isNullOrBlank()) {
            append(" \"$title\"")
        }
        append(")")
    }
}