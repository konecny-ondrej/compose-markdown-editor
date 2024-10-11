package me.okonecny.wysiwyg.ast

import kotlin.reflect.KClass

data class VisualNodeEditors(
    val nodeEditors: Map<KClass<*>, VisualNodeEditor<*>>
) {
    inline fun <reified T : Any> forNodeOfType(node: VisualNode<T>): VisualNodeEditor<T> =
        (nodeEditors[T::class]
            ?: throw IllegalArgumentException(
                "No editor registered for node with data of type ${T::class}"
            )) as VisualNodeEditor<T>

    inline fun <reified T : Any> withEditor(nodeEditor: VisualNodeEditor<T>) = copy(
        nodeEditors = nodeEditors + (T::class to nodeEditor)
    )
}
