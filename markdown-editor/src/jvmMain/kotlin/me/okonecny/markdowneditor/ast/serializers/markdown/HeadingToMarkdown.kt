package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.Heading
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.markdowneditor.trim
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class HeadingToMarkdown<D : Any> : VisualNodeSerializer<Heading, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Heading, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val separator = ' '
        val headingText = node.children.joinToAnnotatedString(separator.toString(), filter = AnnotatedString::isNotBlank) { childNode ->
            serialize(childNode, selection).trim(separator)
        }
        if (headingText.isBlank()) return@buildAnnotatedString

        append("#".repeat(node.data.level.numericLevel()))
        append(" ")
        append(headingText)
    }
}