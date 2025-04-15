package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Strikethrough
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class StrikethroughToMarkdown<D : Any> : VisualNodeSerializer<Strikethrough, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Strikethrough, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val text =
            node.children.joinToAnnotatedString(" ", filter = AnnotatedString::isNotBlank) { childNode ->
                serialize(childNode, selection)
            }
        if (text.isBlank()) return@buildAnnotatedString

        append("~~")
        append(text)
        append("~~")
    }
}