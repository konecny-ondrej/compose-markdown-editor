package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.LocalNavigation
import me.okonecny.interactivetext.NavigableLazyColumn
import me.okonecny.interactivetext.Navigation
import me.okonecny.markdowneditor.inline.InternalAnchorLink
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
import me.okonecny.markdowneditor.view.*
import me.okonecny.markdowneditor.view.inline.*
import java.nio.file.Path

fun Renderers.Companion.flexmarkDefault(
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
) = Renderers<Node>()
    .withUnknownBlockTypeRenderer(UiUnparsedBlock())
    .withUnknownInlineTypeRenderer(UiUnparsedInline())
    .withRenderer(UiHeading())
    .withRenderer(UiParagraph())
    .withRenderer(UiHorizontalRule())
    .withRenderer(UiBlockQuote())
    .withRenderer(UiIndentedCodeBlock())
    .withRenderer(UiCodeFence(codeFenceRenderers))
    .withRenderer(UiHtmlBlock())
    .withRenderer(UiOrderedList())
    .withRenderer<OrderedListItem>(UiListItem())
    .withRenderer(UiBulletList())
    .withRenderer<BulletListItem>(UiListItem())
    .withRenderer(UiTaskListItem())
    .withIgnoredNodeType<HtmlCommentBlock>()
    .withIgnoredNodeType<Reference>() // TODO: skip references so the user cannot delete them accidentally. Or make them visible somehow.
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
    .withRenderer(UiAutoLink())
    .withRenderer(UiLinkRef())
    .withRenderer(UiHtmlEntity())
    .withRenderer(UiImage())
    .withRenderer(UiImageRef())
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
    renderers: Renderers<Node> = Renderers.flexmarkDefault(
        codeFenceRenderers
    )
) {
    val markdown = remember(basePath) { MarkdownEditorComponent::class.create() }
    val parser = remember(markdown) { markdown.documentParser }
    val document = remember(sourceText, parser, basePath) { parser.parse(sourceText, basePath) }
    val visualDocument = remember(sourceText, basePath) { markdown.markdownParser.parse(sourceText, basePath) }

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
        LocalMarkdownEditorComponent provides markdown,
        LocalDocument provides document
    ) {
        UiMdDocument(document.ast, modifier, scrollable, linkHandlers, renderers)
    }
}

internal val LinkHandlers = compositionLocalOf { emptyMap<String, LinkHandler>() }
internal val LocalMarkdownEditorComponent = compositionLocalOf<MarkdownEditorComponent> {
    throw IllegalStateException("The editor component can only be used inside MarkdownView.")
}
internal val LocalDocument = compositionLocalOf<MarkdownDocument> {
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

private fun Navigation.registerNode(node: Node, scrollId: Int) {
    val anchorRefId: String? = when (node) {
        is AnchorRefTarget -> node.anchorRefId
        is Link -> node.anchorRefId
        else -> null
    }
    if (anchorRefId != null) registerAnchorTarget(anchorRefId, scrollId)
    if (node.hasChildren()) node.children.forEach { registerNode(it, scrollId) }
}

@Composable
private fun UiMdDocument(
    markdownRoot: Document,
    modifier: Modifier,
    scrollable: Boolean,
    linkHandlers: List<LinkHandler>,
    renderers: Renderers<Node>
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
internal fun UiBlock(block: Node, renderers: Renderers<Node>) {
    renderers.forBlock(block).run {
        val context = object : RenderContext<Node> {
            override val document: MarkdownDocument = LocalDocument.current
            override val activeAnnotationTags: Set<String> = LinkHandlers.current.keys

            @Composable
            override fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit =
                me.okonecny.markdowneditor.handleLinks()

            @Composable
            override fun renderInline(inline: Node): MappedText = renderInlines(listOf(inline))

            @Composable
            override fun renderInlines(inlines: Iterable<Node>): MappedText {
                return buildMappedString {
                    inlines.forEach { inline ->
                        renderers.forInline(inline).run {
                            append(render(inline).visuallyOffset(visualLength))
                        }
                    }
                }
            }


            @Composable
            override fun <T : Node> renderBlocks(blocks: Iterable<T>) = blocks.forEach { childBlock ->
                renderBlock(childBlock)
            }

            @Composable
            override fun <T : Node> renderBlock(block: T) {
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
