package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.emoji.Emoji
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.users.GfmUser
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.LocalNavigation
import me.okonecny.interactivetext.NavigableLazyColumn
import me.okonecny.interactivetext.Navigation
import me.okonecny.markdowneditor.flexmark.rawCode
import me.okonecny.markdowneditor.flexmark.text
import me.okonecny.markdowneditor.inline.InternalAnchorLink
import me.okonecny.markdowneditor.inline.appendEmoji
import me.okonecny.markdowneditor.inline.appendImage
import me.okonecny.markdowneditor.inline.rememberImageState
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
import me.okonecny.markdowneditor.view.*
import java.nio.file.Path

fun Renderers.Companion.flexmarkDefault(
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
) = Renderers<Node>()
    .withUnknownBlockTypeRenderer(UiUnparsedBlock())
    //TODO: .withUnknownInlineTypeRenderer()
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

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
        LocalMarkdownEditorComponent provides markdown,
        LocalDocument provides document
    ) {
        UiMdDocument(document.ast, modifier, scrollable, linkHandlers, renderers)
    }
}

private val LinkHandlers = compositionLocalOf { emptyMap<String, LinkHandler>() }
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
                val styles = DocumentTheme.current.styles
                return buildMappedString {
                    inlines.forEach { inline ->
                        when (inline) {
                            is Text -> append(inline.text().visuallyOffset(visualLength))
                            is TextBase -> append(
                                renderInlines(inline.children).visuallyOffset(visualLength)
                            )

                            is Code -> {
                                val parsedText = renderInlines(inline.children).visuallyOffset(visualLength)
                                appendStyled(parsedText, styles.inlineCode.toSpanStyle())
                            }

                            is SoftLineBreak -> append(
                                MappedText(
                                    " ",
                                    SequenceTextMapping(
                                        TextRange(
                                            visualLength,
                                            visualLength + 1
                                        ), inline.chars
                                    )
                                )
                            )

                            is Emphasis -> {
                                val parsedText = renderInlines(inline.children).visuallyOffset(visualLength)
                                appendStyled(parsedText, styles.emphasis.toSpanStyle())
                            }

                            is StrongEmphasis -> {
                                val parsedText = renderInlines(inline.children).visuallyOffset(visualLength)
                                appendStyled(parsedText, styles.strong.toSpanStyle())
                            }

                            is GfmUser -> appendStyled(
                                inline.rawCode().visuallyOffset(visualLength),
                                styles.userMention.toSpanStyle()
                            )

                            is Strikethrough -> {
                                val parsedText = renderInlines(inline.children).visuallyOffset(visualLength)
                                appendStyled(parsedText, styles.strikethrough.toSpanStyle())
                            }

                            is HardLineBreak -> append(
                                MappedText(
                                    System.lineSeparator(),
                                    SequenceTextMapping(
                                        TextRange(
                                            visualLength,
                                            visualLength + 1
                                        ), inline.chars
                                    )
                                )
                            )

                            is Link -> appendLink(inline)
                            is AutoLink -> {
                                val url = inline.text.toString()
                                val linkText = MappedText(
                                    url,
                                    SequenceTextMapping(
                                        TextRange(
                                            visualLength,
                                            visualLength + inline.textLength
                                        ), inline.text
                                    )
                                )
                                val annotatedLinkText = annotateLinkByHandler(linkText, url, LinkHandlers.current)
                                appendStyled(
                                    annotatedLinkText,
                                    if (linkText == annotatedLinkText) {
                                        DocumentTheme.current.styles.deadLink.toSpanStyle()
                                    } else {
                                        DocumentTheme.current.styles.link.toSpanStyle()
                                    }
                                )
                            }

                            is LinkRef -> appendLinkRef(inline)
                            is HtmlEntity -> {
                                val parsedText = renderInlines(inline.children).visuallyOffset(visualLength)
                                appendStyled(parsedText, styles.inlineCode.toSpanStyle())
                            }
                            // TODO: proper parsing of MailLinks.
                            is MailLink -> appendUnparsed(inline)
                            is HtmlInlineBase -> appendUnparsed(inline)
                            is Image -> {
                                var imageState by rememberImageState(
                                    url = inline.url.toString(),
                                    title = inline.title.toString()
                                )
                                appendImage(inline, imageState) { newState ->
                                    imageState = newState
                                }
                            }

                            is ImageRef -> {
                                val reference = LocalDocument.current.resolveReference(inline.reference.toString())
                                var imageState by rememberImageState(
                                    url = reference?.url ?: "",
                                    title = reference?.title ?: ""
                                )
                                appendImage(inline, imageState) { newState ->
                                    imageState = newState
                                }
                            }

                            is Emoji -> appendEmoji(
                                inline,
                                inline.rawCode().visuallyOffset(visualLength)
                            )

                            else -> appendUnparsed(inline)
                        }
                    }
                }
            }

            @Composable
            private fun MappedText.Builder.appendLinkRef(linkRef: LinkRef) {
                val linkText = renderInlines(linkRef.children).visuallyOffset(visualLength)
                val reference = linkRef.reference.ifEmpty { linkRef.text }
                val url = LocalDocument.current.resolveReference(reference.toString())?.url
                val annotatedLinkText = annotateLinkByHandler(linkText, url, LinkHandlers.current)
                appendStyled(
                    annotatedLinkText,
                    if (linkText == annotatedLinkText) {
                        DocumentTheme.current.styles.deadLink.toSpanStyle()
                    } else {
                        DocumentTheme.current.styles.link.toSpanStyle()
                    }
                )
            }

            @Composable
            private fun MappedText.Builder.appendLink(link: Link) {
                val url = link.url.toString()
                val linkText = renderInlines(link.children).visuallyOffset(visualLength)
                val annotatedLinkText = annotateLinkByHandler(linkText, url, LinkHandlers.current)
                appendStyled(
                    annotatedLinkText,
                    if (link.isAnchor) {
                        DocumentTheme.current.styles.inlineAnchor.toSpanStyle()
                    } else if (linkText == annotatedLinkText) {
                        DocumentTheme.current.styles.deadLink.toSpanStyle()
                    } else {
                        DocumentTheme.current.styles.link.toSpanStyle()
                    }
                )
            }

            @Composable
            private fun MappedText.Builder.appendUnparsed(unparsedNode: Node) {
                val parsedText = renderInlines(unparsedNode.children).visuallyOffset(visualLength)
                appendStyled(
                    parsedText, DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
                )
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

private fun annotateLinkByHandler(
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
