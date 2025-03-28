package me.okonecny.wysiwyg.ast.serializers

import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import kotlin.reflect.KClass

data class VisualNodeSerializers<Document : Any, Output : Any>(
    val unknownNodeSerializer: VisualNodeSerializer<Any, Document, Output> = throwingNodeSerializer(),
    val nodeSerializers: Map<KClass<*>, VisualNodeSerializer<*, Document, Output>> = emptyMap()
) {

    inline fun <reified T : Any> withSerializer(serializer: VisualNodeSerializer<T, Document, Output>): VisualNodeSerializers<Document, Output> =
        copy(
            nodeSerializers = nodeSerializers + (T::class to serializer)
        )

    fun withUnknownNodeSerializer(serializer: VisualNodeSerializer<Any, Document, Output>): VisualNodeSerializers<Document, Output> =
        copy(
            unknownNodeSerializer = serializer
        )

    fun <T : Any> forNode(node: VisualNode<T, Document>): VisualNodeSerializer<T, Document, Output> {
        val nodeDataType = node.data::class
        return nodeSerializers[nodeDataType] as? VisualNodeSerializer<T, Document, Output> ?: unknownNodeSerializer
    }

    companion object {
        private fun <T : Any, D : Any, O : Any> throwingNodeSerializer(): VisualNodeSerializer<T, D, O> =
            object : VisualNodeSerializer<T, D, O> {
                override fun VisualNodeSerializationContext<D, O>.serializeNode(
                    node: VisualNode<T, D>,
                    selection: VisualNodeSelection<D>?
                ): Nothing {
                    throw UnsupportedOperationException(
                        "Cannot serialize VisualNode<%s, %s>. Did you forget to specify a serializer for it?".format(
                            node.data::class, node.root.data::class
                        )
                    )
                }
            }
    }
}