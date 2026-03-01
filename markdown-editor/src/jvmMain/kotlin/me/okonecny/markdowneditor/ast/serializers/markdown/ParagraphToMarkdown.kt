package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Paragraph
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class ParagraphToMarkdown<D : Any> : VisualNodeSerializer<Paragraph, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Paragraph, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val paragraphText = node
            .children
            .joinToAnnotatedString("", filter = AnnotatedString::isNotEmpty) { childNode ->
                serialize(childNode, selection)
            }
        if (paragraphText.isBlank()) return@buildAnnotatedString

        append(paragraphText)
    }
}