package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.LocalNavigation
import me.okonecny.interactivetext.NavigableLazyColumn
import me.okonecny.interactivetext.Navigation
import me.okonecny.markdowneditor.ast.data.LinkTarget
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.inline.InternalAnchorLink
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
import me.okonecny.markdowneditor.view.*
import me.okonecny.markdowneditor.view.inline.*
import me.okonecny.wysiwyg.ast.VisualNode
import java.nio.file.Path

fun Renderers.Companion.flexmarkDefault(
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
) = Renderers<FlexmarkDocument>()
    .withUnknownBlockTypeRenderer(UiUnparsedBlock())
    .withUnknownInlineTypeRenderer(UiUnparsedInline())
    .withRenderer(UiHeading())
    .withRenderer(UiParagraph())
    .withRenderer(UiHorizontalRule())
    .withRenderer(UiBlockQuote())
    .withRenderer(UiCodeFence(codeFenceRenderers))
    .withRenderer(UiHtmlBlock())
    .withRenderer(UiOrderedList())
    .withRenderer(UiOrderedListItem())
    .withRenderer(UiBulletList())
    .withRenderer(UiBulletListItem())
    .withRenderer(UiTaskListItem())
    .withRenderer(UiTableBlock())
    .withRenderer(UiText())
    .withRenderer(UiTextBase())
    .withRenderer(UiCode())
    .withRenderer(UiEmphasis())
    .withRenderer(UiStrongEmphasis())
    .withRenderer(UiSoftLineBreak())
    .withRenderer(UiGfmUser())
    .withRenderer(UiStrikethrough())
    .withRenderer(UiHardLineBreak())
    .withRenderer(UiLink())
    .withRenderer(UiAnchor())
    .withRenderer(UiAutoLink())
    .withRenderer(UiHtmlEntity())
    .withRenderer(UiImage())
    .withRenderer(UiEmoji())
//.withRenderer<MailLink>() // TODO
//.withRenderer<HtmlInlineBase>() // TODO

/**
 * Renders a Markdown document nicely.
 */
@Composable
fun MarkdownView(
    sourceText: String,
    basePath: Path,
    modifier: Modifier = Modifier.fillMaxWidth(1f),
    documentTheme: DocumentTheme = DocumentTheme.default,
    scrollable: Boolean = true,
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList(),
    linkHandlers: List<LinkHandler> = emptyList(),
    renderers: Renderers<FlexmarkDocument> = Renderers.flexmarkDefault(
        codeFenceRenderers
    )
) {
    val markdown = remember(basePath) { MarkdownEditorComponent::class.create() }
    val visualDocument = remember(sourceText, basePath) { markdown.markdownParser.parse(sourceText, basePath) }

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
        LocalMarkdownEditorComponent provides markdown,
        LocalDocument provides visualDocument.data
    ) {
        UiMdDocument(visualDocument, modifier, scrollable, linkHandlers, renderers)
    }
}

internal val LinkHandlers = compositionLocalOf { emptyMap<String, LinkHandler>() }
internal val LocalMarkdownEditorComponent = compositionLocalOf<MarkdownEditorComponent> {
    throw IllegalStateException("The editor component can only be used inside MarkdownView.")
}
internal val LocalDocument = compositionLocalOf<FlexmarkDocument> {
    throw IllegalStateException("The document can only be used inside MarkdownView.")
}

@Composable
private fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit {
    val actions = LinkHandlers.current
    return { _: Int, annotations: List<AnnotatedString.Range<String>> ->
        actions.forEach { (actionTag, action) ->
            annotations.filter { range ->
                range.tag == actionTag
            }.forEach { range ->
                action.linkActivated(range.item)
            }
        }
    }
}

private fun Navigation.registerNode(node: VisualNode<Any>, scrollId: Int) {
    val anchorRefId: String? = when (val nodeData = node.data) {
        is LinkTarget -> nodeData.anchorName
        else -> null
    }
    if (anchorRefId != null) registerAnchorTarget(anchorRefId, scrollId)
    node.children.forEach { registerNode(it, scrollId) }
}

@Composable
private fun UiMdDocument(
    markdownRoot: VisualNode<FlexmarkDocument>,
    modifier: Modifier,
    scrollable: Boolean,
    linkHandlers: List<LinkHandler>,
    renderers: Renderers<FlexmarkDocument>
) {
    if (scrollable) {
        val navigation = LocalNavigation.current
        CompositionLocalProvider(
            LinkHandlers provides (linkHandlers + listOf(InternalAnchorLink(navigation))).associateBy(LinkHandler::linkAnnotationTag),
        ) {
            NavigableLazyColumn(modifier = modifier, navigation = navigation) {
                markdownRoot.children.forEachIndexed { index, child ->
                    navigation.registerNode(child, index)
                    item {
                        UiBlock(child, renderers)
                    }
                }
            }
        }
    } else {
        Column {
            markdownRoot.children.forEach { child ->
                UiBlock(child, renderers)
            }
        }
    }
}

@Composable
internal fun UiBlock(block: VisualNode<Any>, renderers: Renderers<FlexmarkDocument>) {
    renderers.forBlock(block).run {
        val context = object : RenderContext<FlexmarkDocument> {
            override val document: FlexmarkDocument = LocalDocument.current
            override val activeAnnotationTags: Set<String> = LinkHandlers.current.keys

            @Composable
            override fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit =
                me.okonecny.markdowneditor.handleLinks()

            @Composable
            override fun renderInline(inline: VisualNode<Any>): MappedText = renderInlines(listOf(inline))

            @Composable
            override fun renderInlines(inlines: Iterable<VisualNode<Any>>): MappedText {
                return buildMappedString {
                    inlines.forEach { inline ->
                        renderers.forInline(inline).run {
                            append(render(inline).visuallyOffset(visualLength))
                        }
                    }
                }
            }


            @Composable
            override fun renderBlocks(blocks: Iterable<VisualNode<Any>>) = blocks.forEach { childBlock ->
                renderBlock(childBlock)
            }

            @Composable
            override fun renderBlock(block: VisualNode<Any>) {
                UiBlock(block, renderers)
            }
        }
        context.render(block)
    }
}

// region inlines

// e.g. after the link with no URL.

internal fun annotateLinkByHandler(
    linkText: MappedText,
    linkUrl: String?,
    linkHandlers: Map<String, LinkHandler>
): MappedText {
    if (linkUrl.isNullOrEmpty()) return linkText
    return linkHandlers.mapValues { (_, handler) ->
        handler.parseLinkAnnotation(linkUrl)
    }
        .entries
        .runningFold(linkText) { inlines, annotation ->
            inlines.annotatedWith(annotation.key, annotation.value ?: return@runningFold inlines)
        }
        .ifEmpty { listOf(linkText) }
        .last()
}

// endregion inlines
