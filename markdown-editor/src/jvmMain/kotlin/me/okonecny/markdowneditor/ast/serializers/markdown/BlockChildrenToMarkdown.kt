package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class BlockChildrenToMarkdown<T : Any, D : Any> : VisualNodeSerializer<T, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<T, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val childrenText = node.children.joinToAnnotatedString("\n\n", filter = AnnotatedString::isNotEmpty) { child ->
            serialize(child, selection)
        }
        if (childrenText.isBlank()) return@buildAnnotatedString
        append(childrenText)
    }
}