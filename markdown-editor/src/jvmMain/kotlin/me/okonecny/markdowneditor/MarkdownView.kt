package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.LocalNavigation
import me.okonecny.interactivetext.NavigableLazyColumn
import me.okonecny.interactivetext.Navigation
import me.okonecny.markdowneditor.ast.data.LinkTarget
import me.okonecny.markdowneditor.inline.InternalAnchorLink
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.markdowneditor.view.Renderers
import me.okonecny.wysiwyg.ast.VisualNode

/**
 * Renders a Markdown document nicely.
 */
@Composable
fun <D : Any> MarkdownView(
    visualDocument: VisualNode<D, D>,
    renderers: Renderers<D>,
    modifier: Modifier = Modifier.fillMaxWidth(1f),
    documentTheme: DocumentTheme = DocumentTheme.default,
    scrollable: Boolean = true,
    linkHandlers: List<LinkHandler> = emptyList()
) {

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
    ) {
        UiMdDocument(visualDocument, modifier, scrollable, linkHandlers, renderers)
    }
}

private fun <D : Any> Navigation.registerNode(node: VisualNode<Any, D>, scrollId: Int) {
    val anchorRefId: String? = when (val nodeData = node.data) {
        is LinkTarget -> nodeData.anchorName
        else -> null
    }
    if (anchorRefId != null) registerAnchorTarget(anchorRefId, scrollId)
    node.children.forEach { registerNode(it, scrollId) }
}

@Composable
private fun <D : Any> UiMdDocument(
    markdownRoot: VisualNode<D, D>,
    modifier: Modifier,
    scrollable: Boolean,
    linkHandlers: List<LinkHandler>,
    renderers: Renderers<D>
) {
    val navigation = LocalNavigation.current
    val linkHandlersMap =
        (linkHandlers + listOf(InternalAnchorLink(navigation))).associateBy(LinkHandler::linkAnnotationTag)

    val context = object : RenderContext<D> {
        override val document: D = markdownRoot.data
        override val activeAnnotationTags: Set<String> = linkHandlersMap.keys

        @Composable
        override fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit {
            return { _: Int, annotations: List<AnnotatedString.Range<String>> ->
                linkHandlersMap.forEach { (actionTag, action) ->
                    annotations.filter { range ->
                        range.tag == actionTag
                    }.forEach { range ->
                        action.linkActivated(range.item)
                    }
                }
            }
        }

        override fun annotateLinkByHandler(linkText: MappedText, linkUrl: String?): MappedText {
            if (linkUrl.isNullOrEmpty()) return linkText
            return linkHandlersMap.mapValues { (_, handler) ->
                handler.parseLinkAnnotation(linkUrl)
            }
                .entries
                .runningFold(linkText) { inlines, annotation ->
                    inlines.annotatedWith(annotation.key, annotation.value ?: return@runningFold inlines)
                }
                .ifEmpty { listOf(linkText) }
                .last()
        }

        @Composable
        override fun renderInline(inline: VisualNode<Any, D>): MappedText =
            renderInlines(listOf(inline))

        @Composable
        override fun renderInlines(inlines: Iterable<VisualNode<Any, D>>): MappedText {
            return buildMappedString {
                inlines.forEach { inline ->
                    renderers.forInline(inline).run {
                        append(render(inline))
                    }
                }
            }
        }

        @Composable
        override fun renderBlocks(blocks: Iterable<VisualNode<Any, D>>) =
            blocks.forEach { childBlock ->
                renderBlock(childBlock)
            }

        @Composable
        override fun renderBlock(block: VisualNode<Any, D>) {
            renderers.forBlock(block).run {
                render(block)
            }
        }
    }

    if (scrollable) {
        NavigableLazyColumn(modifier = modifier, navigation = navigation) {
            markdownRoot.children.forEachIndexed { index, child ->
                navigation.registerNode(child, index)
                item {
                    context.renderBlock(child)
                }
            }
        }
    } else {
        Column {
            markdownRoot.children.forEach { child ->
                context.renderBlock(child)
            }
        }
    }
}
