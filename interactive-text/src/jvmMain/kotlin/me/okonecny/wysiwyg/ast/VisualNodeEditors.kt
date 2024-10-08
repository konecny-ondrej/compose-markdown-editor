package me.okonecny.wysiwyg.ast

import kotlin.reflect.KClass

data class VisualNodeEditors(
    val nodeEditors: Map<KClass<*>, VisualNodeEditor<*>>
) {
    fun <T : Any> forNodeOfType(node: VisualNode<T>): VisualNodeEditor<T> =
        (nodeEditors[node.data::class]
            ?: throw IllegalArgumentException(
                "No editor registered for node with data of type ${node.data::class}"
            )) as VisualNodeEditor<T>

    inline fun <reified T : Any> withEditor(nodeEditor: VisualNodeEditor<T>) = copy(
        nodeEditors = nodeEditors + (T::class to nodeEditor)
    )
}
