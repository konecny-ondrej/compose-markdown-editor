package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.MappedText
import kotlin.reflect.KClass

data class Renderers<BaseNode : Any>(
    val unknownBlockRenderer: BlockRenderer<BaseNode, BaseNode> = noopBlockRenderer(),
    val unknownInlineRenderer: InlineRenderer<BaseNode, BaseNode> = noopInlineRenderer(),
    val blockRenderers: Map<KClass<out BaseNode>, BlockRenderer<BaseNode, BaseNode>> = emptyMap(),
    val inlineRenderers: Map<KClass<out BaseNode>, InlineRenderer<BaseNode, BaseNode>> = emptyMap(),
    val ignoredNodeTypes: Set<KClass<out BaseNode>> = emptySet()
) {
    inline fun <reified T : BaseNode> withRenderer(renderer: BlockRenderer<T, BaseNode>): Renderers<BaseNode> = copy(
        blockRenderers = blockRenderers + (T::class to renderer as BlockRenderer<BaseNode, BaseNode>)
    )

    inline fun <reified T : BaseNode> withRenderer(renderer: InlineRenderer<T, BaseNode>): Renderers<BaseNode> = copy(
        inlineRenderers = inlineRenderers + (T::class to renderer as InlineRenderer<BaseNode, BaseNode>)
    )

    inline fun <reified T : BaseNode> withIgnoredNodeType(): Renderers<BaseNode> = copy(
        ignoredNodeTypes = ignoredNodeTypes + T::class
    )

    fun withUnknownBlockTypeRenderer(renderer: BlockRenderer<BaseNode, BaseNode>): Renderers<BaseNode> = copy(
        unknownBlockRenderer = renderer
    )

    fun withUnknownInlineTypeRenderer(renderer: InlineRenderer<BaseNode, BaseNode>): Renderers<BaseNode> = copy(
        unknownInlineRenderer = renderer
    )

    fun <T : BaseNode> forBlock(block: T): BlockRenderer<T, BaseNode> {
        return if (ignoredNodeTypes.contains(block::class)) {
            noopBlockRenderer()
        } else {
            blockRenderers[block::class] ?: unknownBlockRenderer
        }
    }

    fun <T : BaseNode> forInline(inline: T): InlineRenderer<T, BaseNode> {
        return if (ignoredNodeTypes.contains(inline::class)) {
            noopInlineRenderer()
        } else {
            inlineRenderers[inline::class] ?: unknownInlineRenderer
        }
    }

    companion object {
        private fun <T : BaseNode, BaseNode> noopBlockRenderer(): BlockRenderer<T, BaseNode> =
            object : BlockRenderer<T, BaseNode> {
                @Composable
                override fun RenderContext<BaseNode>.render(block: T) {
                    // Empty on purpose. Should not render anything, just skip the node.
                }
            }

        private fun <T : BaseNode, BaseNode> noopInlineRenderer(): InlineRenderer<T, BaseNode> =
            object : InlineRenderer<T, BaseNode> {
                @Composable
                override fun RenderContext<BaseNode>.render(inlineNode: T): MappedText = MappedText.empty
            }
    }
}