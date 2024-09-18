package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

data class BlockRenderers<BaseNode : Any>(
    val unknownBlockRenderer: BlockRenderer<in BaseNode> = noopRenderer(),
    val typeToRenderer: Map<KClass<out BaseNode>, BlockRenderer<in BaseNode>> = emptyMap(),
    val ignoredBlockTypes: Set<KClass<out BaseNode>> = emptySet()
) {
    inline fun <reified T : BaseNode> withRenderer(renderer: BlockRenderer<in T>): BlockRenderers<BaseNode> = copy(
        typeToRenderer = typeToRenderer + (T::class to renderer as BlockRenderer<in BaseNode>)
    )

    inline fun <reified T : BaseNode> withIgnoredBlockType(): BlockRenderers<BaseNode> = copy(
        ignoredBlockTypes = ignoredBlockTypes + T::class
    )

    fun withUnknownNodeTypeRenderer(renderer: BlockRenderer<in BaseNode>): BlockRenderers<BaseNode> = copy(
        unknownBlockRenderer = renderer
    )

    operator fun <T : BaseNode> get(block: T): BlockRenderer<in T> {
        if (ignoredBlockTypes.contains(block::class)) {
            return noopRenderer()
        } else {
            val renderer = typeToRenderer[block::class]
            return renderer ?: unknownBlockRenderer
        }
    }

    companion object {
        private fun <T : Any> noopRenderer(): BlockRenderer<in T> = object : BlockRenderer<T> {
            @Composable
            override fun BlockRenderContext.render(block: T) {
                // Empty on purpose. Should not render anything, just skip the node.
            }
        }
    }
}