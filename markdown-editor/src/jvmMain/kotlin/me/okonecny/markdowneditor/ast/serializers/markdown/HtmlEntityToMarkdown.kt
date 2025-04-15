package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.HtmlEntity
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.markdowneditor.trim
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class HtmlEntityToMarkdown<D : Any> : VisualNodeSerializer<HtmlEntity, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<HtmlEntity, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val separator = ' '
        val htmlEntityText = node.children.joinToAnnotatedString(separator.toString(), filter = AnnotatedString::isNotBlank) { childNode ->
            serialize(childNode, selection).trim(separator)
        }
        append(htmlEntityText)
    }
}