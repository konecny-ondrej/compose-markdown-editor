package me.okonecny.wysiwyg.ast.serializers

import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection

class VisualNodeSerializationContext<Document : Any, out Output : Any>(
    private val serializers: VisualNodeSerializers<Document, Output>
) {
    fun <T : Any> serialize(node: VisualNode<T, Document>, selection: VisualNodeSelection<Document>? = null): Output =
        serializers.forNode(node).run {
            serializeNode(node, selection)
        }
}