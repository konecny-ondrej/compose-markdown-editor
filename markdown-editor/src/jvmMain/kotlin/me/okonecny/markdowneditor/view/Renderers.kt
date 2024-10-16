package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.MappedText
import me.okonecny.wysiwyg.ast.VisualNode
import kotlin.reflect.KClass

data class Renderers<Document>(
    val unknownBlockRenderer: BlockRenderer<Any, Document> = noopBlockRenderer(),
    val unknownInlineRenderer: InlineRenderer<Any, Document> = noopInlineRenderer(),
    val blockRenderers: Map<KClass<*>, BlockRenderer<*, Document>> = emptyMap(),
    val inlineRenderers: Map<KClass<*>, InlineRenderer<*, Document>> = emptyMap(),
    val ignoredNodeTypes: Set<KClass<*>> = emptySet()
) {
    inline fun <reified T> withRenderer(renderer: BlockRenderer<T, Document>): Renderers<Document> = copy(
        blockRenderers = blockRenderers + (T::class to renderer)
    )

    inline fun <reified T> withRenderer(renderer: InlineRenderer<T, Document>): Renderers<Document> = copy(
        inlineRenderers = inlineRenderers + (T::class to renderer)
    )

    inline fun <reified T> withIgnoredNodeType(): Renderers<Document> = copy(
        ignoredNodeTypes = ignoredNodeTypes + T::class
    )

    fun withUnknownBlockTypeRenderer(renderer: BlockRenderer<Any, Document>): Renderers<Document> = copy(
        unknownBlockRenderer = renderer
    )

    fun withUnknownInlineTypeRenderer(renderer: InlineRenderer<Any, Document>): Renderers<Document> = copy(
        unknownInlineRenderer = renderer
    )

    fun <T : Any> forBlock(block: VisualNode<T>): BlockRenderer<T, Document> {
        val rendererType = block.data::class
        return if (ignoredNodeTypes.contains(rendererType)) {
            noopBlockRenderer()
        } else {
            blockRenderers[rendererType] as? BlockRenderer<T, Document> ?: unknownBlockRenderer
        }
    }

    fun <T : Any> forInline(inline: VisualNode<T>): InlineRenderer<T, Document> {
        val rendererType = inline.data::class
        return if (ignoredNodeTypes.contains(rendererType)) {
            noopInlineRenderer()
        } else {
            inlineRenderers[rendererType] as? InlineRenderer<T, Document> ?: unknownInlineRenderer
        }
    }

    companion object {
        private fun <T, D> noopBlockRenderer(): BlockRenderer<T, D> =
            object : BlockRenderer<T, D> {
                @Composable
                override fun RenderContext<D>.render(block: VisualNode<T>) {
                    // Empty on purpose. Should not render anything, just skip the node.
                }
            }

        private fun <T, D> noopInlineRenderer(): InlineRenderer<T, D> =
            object : InlineRenderer<T, D> {
                @Composable
                override fun RenderContext<D>.render(inlineNode: VisualNode<T>): MappedText = MappedText.empty
            }
    }
}