package me.okonecny.wysiwyg.ast.serializers

import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection

/**
 * Serialize a subtree represented by a node of a particular data type.
 */
interface VisualNodeSerializer<in DataType : Any, Document : Any, Output : Any> {
    /**
     * Serialize a subtree represented by the node into the output format, taking into account selection.
     * @param node Subtree to serialize.
     * @param selection The part of the subtree to serialize, represented as selected span of nodes in reading order.
     * null means serialize the whole subtree.
     */
    fun VisualNodeSerializationContext<Document, Output>.serializeNode(
        node: VisualNode<DataType, Document>,
        selection: VisualNodeSelection<Document>? = null
    ): Output
}