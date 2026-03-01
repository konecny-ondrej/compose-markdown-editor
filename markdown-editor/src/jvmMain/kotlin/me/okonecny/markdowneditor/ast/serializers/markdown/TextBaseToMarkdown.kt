package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.TextBase
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class TextBaseToMarkdown<D : Any> : VisualNodeSerializer<TextBase, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<TextBase, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val text =
            node.children.joinToAnnotatedString("", filter = AnnotatedString::isNotBlank) { childNode ->
                serialize(childNode, selection)
            }
        if (text.isBlank()) return@buildAnnotatedString

        append(text)
    }
}