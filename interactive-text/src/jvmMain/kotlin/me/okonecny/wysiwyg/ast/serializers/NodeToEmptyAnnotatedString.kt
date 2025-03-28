package me.okonecny.wysiwyg.ast.serializers

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection

class NodeToEmptyAnnotatedString<D : Any> : VisualNodeSerializer<Any, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Any, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = AnnotatedString("")
}