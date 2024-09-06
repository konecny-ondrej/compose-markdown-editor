package me.okonecny.markdowneditor.view

import kotlin.reflect.KClass

data class BlockRenderers (
    val typeToRenderer: Map<KClass<*>, BlockRenderer<*>> = emptyMap(),
    val ignoredBlockTypes: Set<KClass<*>> = emptySet()
    // TODO: add a renderer for an unknown block type
) {
    inline fun <reified T: Any> withRenderer(renderer: BlockRenderer<T>): BlockRenderers = copy(
        typeToRenderer = typeToRenderer + (T::class to renderer)
    )

    inline fun <reified T: Any> withIgnoredBlockType(): BlockRenderers = copy(
        ignoredBlockTypes = ignoredBlockTypes + T::class
    )

    operator fun <T: Any> get(block: T): BlockRenderer<in T>? =
        if(ignoredBlockTypes.contains(block::class)) {
            null
        } else {
            typeToRenderer[block::class] as BlockRenderer<in T>?
        }
}