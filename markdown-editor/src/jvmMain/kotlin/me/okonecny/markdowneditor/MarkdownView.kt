package me.okonecny.markdowneditor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.emoji.Emoji
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.ext.gfm.users.GfmUser
import com.vladsch.flexmark.ext.tables.*
import com.vladsch.flexmark.util.ast.Block
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.*
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
    blockRenderers: BlockRenderers = BlockRenderers()
        .withRenderer(UiHeading())
        .withRenderer(UiParagraph())
        .withRenderer(UiHorizontalRule())
        .withRenderer(UiBlockQuote())
        .withRenderer(UiIndentedCodeBlock())
        .withRenderer(UiCodeFence(codeFenceRenderers))
        .withRenderer(UiHtmlBlock())
        .withRenderer(UiOrderedList())
        .withRenderer(UiBulletList())
        .withIgnoredBlockType<HtmlCommentBlock>()
        .withIgnoredBlockType<Reference>() // TODO: skip references so the user cannot delete them accidentally. Or make them visible somehow.
) {
    val markdown = remember(basePath) { MarkdownEditorComponent::class.create() }
    val parser = remember(markdown) { markdown.documentParser }
    val document = remember(sourceText, parser, basePath) { parser.parse(sourceText, basePath) }

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
        BlockRenderers provides blockRenderers,
        LocalMarkdownEditorComponent provides markdown,
        LocalDocument provides document
    ) {
        UiMdDocument(document.ast, modifier, scrollable, linkHandlers)
    }
}

private val BlockRenderers = compositionLocalOf { BlockRenderers() }
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
    linkHandlers: List<LinkHandler>
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
                        UiBlock(child)
                    }
                }
            }
        }
    } else {
        Column {
            markdownRoot.children.forEach { child ->
                UiBlock(child)
            }
        }
    }
}

@Composable
internal fun UiBlock(block: Node) {
    BlockRenderers.current[block]?.run {
        val context = object : BlockRenderContext {
            override val document: MarkdownDocument = LocalDocument.current
            override val activeAnnotationTags: Set<String> = LinkHandlers.current.keys

            @Composable
            override fun handleLinks(): (Int, List<AnnotatedString.Range<String>>) -> Unit =
                me.okonecny.markdowneditor.handleLinks()

            @Composable
            override fun parseInlines(inlines: Iterable<Node>): MappedText =
                me.okonecny.markdowneditor.parseInlines(inlines)

            @Composable
            override fun <T : Block> renderBlocks(blocks: Iterable<T>) = blocks.forEach { childBlock ->
                UiBlock(childBlock)
            }

            @Composable
            override fun <T : Block> renderBlock(block: T) {
                UiBlock(block)
            }
        }
        context.render(block)
        return@UiBlock
    }
    when (block) {
//        is Heading -> UiHeading(block)
//        is Paragraph -> UiParagraph(block)
//        is ThematicBreak -> UiHorizontalRule()
//        is BlockQuote -> UiBlockQuote(block)
//        is IndentedCodeBlock -> UiIndentedCodeBlock(block)
//        is FencedCodeBlock -> UiCodeFence(block)
//        is HtmlBlock -> UiHtmlBlock(block)
//        is OrderedList -> UiOrderedList(block)
//        is BulletList -> UiBulletList(block)
//        is HtmlCommentBlock -> Unit // Ignore HTML comments. They are not visible in HTML either.
        is TableBlock -> UiTableBlock(block)
//        is Reference -> Unit
        else -> UiUnparsedBlock(block)
    }
}

@Composable
private fun UiTableBlock(tableBlock: TableBlock) {

    @Composable
    fun UiTableSection(tableSection: Node, cellStyle: BlockStyle) {
        val document = LocalDocument.current
        tableSection.children.forEach { tableRow ->
            when (tableRow) {
                is TableRow -> Row(Modifier.height(IntrinsicSize.Max)) {
                    tableRow.children.forEach { cell ->
                        when (cell) {
                            is TableCell -> {
                                val inlines = parseInlines(cell.children)
                                InteractiveText(
                                    interactiveId = document.getInteractiveId(cell),
                                    text = inlines.text,
                                    textMapping = inlines.textMapping,
                                    inlineContent = inlines.inlineContent,
                                    style = cellStyle.textStyle.copy(
                                        textAlign = when (cell.alignment) {
                                            TableCell.Alignment.LEFT -> TextAlign.Left
                                            TableCell.Alignment.CENTER -> TextAlign.Center
                                            TableCell.Alignment.RIGHT -> TextAlign.Right
                                            else -> TextAlign.Start
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1.0f)
                                        .then(cellStyle.modifier),
                                    activeAnnotationTags = LinkHandlers.current.keys,
                                    onAnnotationCLick = handleLinks(),
                                    userData = UserData.of(Node::class, cell)
                                )
                            }

                            else -> UiUnparsedBlock(cell)
                        }
                    }
                }

                else -> UiUnparsedBlock(tableRow)
            }
        }
    }

    val styles = DocumentTheme.current.styles
    Column(styles.table.modifier) {
        tableBlock.children.forEach { tableSection ->
            when (tableSection) {
                is TableSeparator -> Unit
                is TableHead -> UiTableSection(tableSection, styles.table.headerCellStyle)
                is TableBody -> UiTableSection(tableSection, styles.table.bodyCellStyle)
                else -> UiUnparsedBlock(tableSection)
            }
        }
    }
}

// region inlines

// e.g. after the link with no URL.
@Composable
internal fun parseInlines(
    inlines: Iterable<Node>,
    visualStartOffset: Int = 0
): MappedText {
    val styles = DocumentTheme.current.styles
    return buildMappedString {
        inlines.forEach { inline ->
            when (inline) {
                is Text -> append(inline.text(visualStartOffset = visualLength + visualStartOffset))
                is TextBase -> append(
                    parseInlines(
                        inline.children,
                        visualStartOffset = visualLength + visualStartOffset
                    )
                )

                is Code -> appendStyled(inline, styles.inlineCode.toSpanStyle(), visualStartOffset)
                is SoftLineBreak -> append(
                    MappedText(
                        " ",
                        SequenceTextMapping(
                            TextRange(
                                visualLength + visualStartOffset,
                                visualLength + visualStartOffset + 1
                            ), inline.chars
                        )
                    )
                )

                is Emphasis -> appendStyled(inline, styles.emphasis.toSpanStyle(), visualStartOffset)
                is StrongEmphasis -> appendStyled(inline, styles.strong.toSpanStyle(), visualStartOffset)
                is GfmUser -> appendStyled(
                    inline.rawCode(visualLength + visualStartOffset),
                    styles.userMention.toSpanStyle()
                )

                is Strikethrough -> appendStyled(inline, styles.strikethrough.toSpanStyle(), visualStartOffset)
                is HardLineBreak -> append(
                    MappedText(
                        System.lineSeparator(),
                        SequenceTextMapping(
                            TextRange(
                                visualLength + visualStartOffset,
                                visualLength + visualStartOffset + 1
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
                                visualLength + visualStartOffset,
                                visualLength + visualStartOffset + inline.textLength
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
                is HtmlEntity -> appendStyled(inline, styles.inlineCode.toSpanStyle())
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
                    inline.rawCode(visualLength + visualStartOffset)
                )

                else -> appendUnparsed(inline)
            }
        }
    }
}

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

@Composable
private fun MappedText.Builder.appendLinkRef(linkRef: LinkRef) {
    val linkText = parseInlines(linkRef.children, visualStartOffset = visualLength)
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
    val linkText = parseInlines(link.children, visualStartOffset = visualLength)
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

// endregion inlines
