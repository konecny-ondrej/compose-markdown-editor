package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.CodeSpan
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class CodeSpanToMarkdown<D : Any> : VisualNodeSerializer<CodeSpan, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<CodeSpan, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val code =
            node.children.joinToAnnotatedString(" ", filter = AnnotatedString::isNotBlank) { childNode ->
                serialize(childNode, selection)
            }
        if (code.isBlank()) return@buildAnnotatedString

        append("`")
        append(code)
        append("`")
    }
}