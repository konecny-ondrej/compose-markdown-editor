package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

data class Renderers<BaseNode : Any>(
    val unknownBlockRenderer: BlockRenderer<in BaseNode> = noopBlockRenderer(),
    val typeToRenderer: Map<KClass<out BaseNode>, BlockRenderer<in BaseNode>> = emptyMap(),
    val ignoredBlockTypes: Set<KClass<out BaseNode>> = emptySet()
) {
    inline fun <reified T : BaseNode> withRenderer(renderer: BlockRenderer<in T>): Renderers<BaseNode> = copy(
        typeToRenderer = typeToRenderer + (T::class to renderer as BlockRenderer<in BaseNode>)
    )

    inline fun <reified T : BaseNode> withIgnoredBlockType(): Renderers<BaseNode> = copy(
        ignoredBlockTypes = ignoredBlockTypes + T::class
    )

    fun withUnknownNodeTypeRenderer(renderer: BlockRenderer<in BaseNode>): Renderers<BaseNode> = copy(
        unknownBlockRenderer = renderer
    )

    fun <T : BaseNode> forBlock(block: T): BlockRenderer<in T> {
        if (ignoredBlockTypes.contains(block::class)) {
            return noopBlockRenderer()
        } else {
            val renderer = typeToRenderer[block::class]
            return renderer ?: unknownBlockRenderer
        }
    }

    companion object {
        private fun <T : Any> noopBlockRenderer(): BlockRenderer<in T> = object : BlockRenderer<T> {
            @Composable
            override fun RenderContext.render(block: T) {
                // Empty on purpose. Should not render anything, just skip the node.
            }
        }
    }
}