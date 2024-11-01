package me.okonecny.wysiwyg.ast

import kotlin.reflect.KClass

data class VisualNodeEditors<D : Any>(
    val nodeEditors: Map<KClass<*>, VisualNodeEditor<*, D>>
) {
    inline fun <reified T : Any> forNodeOfType(node: VisualNode<T, D>): VisualNodeEditor<T, D> =
        (nodeEditors[T::class]
            ?: throw IllegalArgumentException(
                "No editor registered for node with data of type ${T::class}"
            )) as VisualNodeEditor<T, D>

    inline fun <reified T : Any> withEditor(nodeEditor: VisualNodeEditor<T, D>) = copy(
        nodeEditors = nodeEditors + (T::class to nodeEditor)
    )
}
