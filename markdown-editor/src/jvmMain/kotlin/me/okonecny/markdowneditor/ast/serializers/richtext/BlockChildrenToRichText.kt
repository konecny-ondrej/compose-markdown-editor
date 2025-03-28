package me.okonecny.markdowneditor.ast.serializers.richtext

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class BlockChildrenToRichText<T : Any, D : Any>(
    private val textStyle: TextStyle? = null
) : VisualNodeSerializer<T, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<T, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        if (textStyle != null) {
            pushStyle(textStyle.toSpanStyle())
        }
        append(
            node.children.joinToAnnotatedString("\n", filter = AnnotatedString::isNotBlank) { child ->
                serialize(child, selection)
            }
        )
        if (textStyle != null) {
            pop()
        }
    }
}