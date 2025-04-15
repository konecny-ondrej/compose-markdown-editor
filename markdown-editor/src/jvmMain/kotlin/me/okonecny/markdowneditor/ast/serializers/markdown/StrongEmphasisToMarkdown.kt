package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.StrongEmphasis
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class StrongEmphasisToMarkdown<D : Any> : VisualNodeSerializer<StrongEmphasis, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<StrongEmphasis, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val strongText =
            node.children.joinToAnnotatedString(" ", filter = AnnotatedString::isNotBlank) { childNode ->
                serialize(childNode, selection)
            }
        if (strongText.isBlank()) return@buildAnnotatedString

        append("**")
        append(strongText)
        append("**")
    }
}
