package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Image
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class ImageToMarkdown<D : Any> : VisualNodeSerializer<Image, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Image, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val imageText =
            node.children.joinToAnnotatedString(" ", filter = AnnotatedString::isNotBlank) { childNode ->
                serialize(childNode, selection)
            }
        if (imageText.isBlank()) return@buildAnnotatedString

        append("![")
        append(imageText)
        append("]")
        if (node.data.url.isBlank() && node.data.title.isNullOrBlank()) return@buildAnnotatedString

        append("(")
        append(node.data.url)
        val title = node.data.title
        if (!title.isNullOrBlank()) {
            append(" \"$title\"")
        }
        append(")")
    }
}