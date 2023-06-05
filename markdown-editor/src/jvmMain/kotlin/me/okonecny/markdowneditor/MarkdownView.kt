package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.ext.tables.*
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import com.vladsch.flexmark.util.sequence.BasedSequence
import me.okonecny.interactivetext.*
import me.okonecny.markdowneditor.inline.appendImage
import me.okonecny.markdowneditor.inline.rememberImageState
import me.okonecny.markdowneditor.internal.MarkdownEditorComponent
import me.okonecny.markdowneditor.internal.create
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
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
) {
    val markdown = remember(basePath) { MarkdownEditorComponent::class.create() }
    val parser = remember(markdown) { markdown.documentParser }
    val document = remember(sourceText, parser) { parser.parse(sourceText, basePath) }

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
        CodeFenceRenderers provides codeFenceRenderers.associateBy(CodeFenceRenderer::codeFenceType),
        LocalMarkdownEditorComponent provides markdown,
        LocalDocument provides document
    ) {
        UiMdDocument(document.ast, modifier, scrollable)
    }
}

private val CodeFenceRenderers = compositionLocalOf { emptyMap<String, CodeFenceRenderer>() }
internal val LocalMarkdownEditorComponent = compositionLocalOf<MarkdownEditorComponent> {
    throw IllegalStateException("The editor component can only be used inside MarkdownView.")
}
internal val LocalDocument = compositionLocalOf<MarkdownDocument> {
    throw IllegalStateException("The document can only be used inside MarkdownView.")
}


@Composable
private fun UiMdDocument(markdownRoot: Document, modifier: Modifier, scrollable: Boolean) {
    if (scrollable) {
        LazyColumn(modifier = modifier) {
            markdownRoot.children.forEach { child ->
                item {
                    UiBlock(child)
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
private fun UiBlock(block: Node) {
    when (block) {
        is Heading -> UiHeading(block)
        is Paragraph -> UiParagraph(block)
        is ThematicBreak -> UiHorizontalRule()
        is BlockQuote -> UiBlockQuote(block)
        is IndentedCodeBlock -> UiIndentedCodeBlock(block)
        is FencedCodeBlock -> UiCodeFence(block)
        is HtmlBlock -> UiHtmlBlock(block)
        is OrderedList -> UiOrderedList(block)
        is BulletList -> UiBulletList(block)
        is HtmlCommentBlock -> Unit // Ignore HTML comments. They are not visible in HTML either.
        is TableBlock -> UiTableBlock(block)
        else -> UiUnparsedBlock(block)
    }
}

@Composable
private fun UiTableBlock(tableBlock: TableBlock) {

    @Composable
    fun UiTableSection(tableSection: Node, cellStyle: BlockStyle) {
        tableSection.children.forEach { tableRow ->
            when (tableRow) {
                is TableRow -> Row(Modifier.height(IntrinsicSize.Max)) {
                    tableRow.children.forEach { cell ->
                        when (cell) {
                            is TableCell -> {
                                val inlines = parseInlines(cell.children)
                                InteractiveText(
                                    text = inlines.text,
                                    textMapping = inlines.textMapping,
                                    inlineContent = inlines.inlineContent,
                                    style = cellStyle.textStyle,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1.0f)
                                        .then(cellStyle.modifier)
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

@Composable
private fun UiTaskListItem(
    taskListItem: TaskListItem,
    bulletOrDelimiter: String,
    openingMarker: BasedSequence,
    number: Int? = null
) {
    val styles = DocumentTheme.current.styles
    Row {
        InteractiveText(
            text = if (taskListItem.isOrderedItem) {
                number.toString() + bulletOrDelimiter
            } else {
                bulletOrDelimiter
            },
            textMapping = SequenceTextMapping(
                coveredVisualRange = TextRange(0, 1),
                sequence = openingMarker
            ),
            style = styles.listNumber
        )
        val onInput = LocalInteractiveInputHandler.current
        Checkbox(
            modifier = styles.taskListCheckbox.modifier,
            checked = taskListItem.isItemDoneMarker,
            onCheckedChange = { isChecked ->
                val taskMarkerRange = TextRange(
                    taskListItem.markerSuffix.startOffset,
                    taskListItem.markerSuffix.endOffset,
                )
                val newMarker = if (isChecked) "[X]" else "[ ]"
                onInput(ReplaceRange(taskMarkerRange, newMarker))
            })
        Column {
            taskListItem.children.forEach { listItemContent ->
                UiBlock(listItemContent)
            }
        }
    }
}

@Composable
private fun UiBulletList(unorderedList: BulletList) {
    val styles = DocumentTheme.current.styles
    val bullet = "\u2022"
    Column {
        unorderedList.children.forEach { child ->
            when (child) {
                is TaskListItem -> UiTaskListItem(child, bulletOrDelimiter = bullet, child.openingMarker)
                is BulletListItem -> Row {
                    InteractiveText(
                        text = bullet,
                        textMapping = SequenceTextMapping(
                            coveredVisualRange = TextRange(0, 1),
                            sequence = child.openingMarker
                        ),
                        style = styles.listNumber
                    )
                    Column {
                        child.children.forEach { listItemContent ->
                            UiBlock(listItemContent)
                        }
                    }
                }

                else -> UiUnparsedBlock(child)
            }
        }
    }
}

@Composable
private fun UiOrderedList(orderedList: OrderedList) {
    val styles = DocumentTheme.current.styles
    Column {
        var computedNumber: Int = orderedList.startNumber

        orderedList.children.forEach { child ->
            when (child) {
                is TaskListItem -> UiTaskListItem(
                    child,
                    bulletOrDelimiter = orderedList.delimiter.toString(),
                    openingMarker = child.openingMarker,
                    number = computedNumber++
                )

                is OrderedListItem -> Row {
                    InteractiveText(
                        text = (computedNumber++).toString() + orderedList.delimiter,
                        textMapping = SequenceTextMapping( // The displayed number generally is not the same as in the source code.
                            coveredVisualRange = TextRange(0, computedNumber.toString().length + 1),
                            sequence = child.openingMarker
                        ),
                        style = styles.listNumber
                    )
                    Column {
                        child.children.forEach { listItemContent ->
                            UiBlock(listItemContent)
                        }
                    }
                }

                else -> UiUnparsedBlock(child)
            }

        }
    }
}

@Composable
private fun UiHtmlBlock(htmlBlock: HtmlBlock) {
    val styles = DocumentTheme.current.styles
    Column(modifier = styles.codeBlock.modifier) {
        htmlBlock.contentLines.forEach { line ->
            InteractiveText(
                text = line.toString(),
                textMapping = SequenceTextMapping(TextRange(0, line.length), line),
                style = styles.codeBlock.textStyle
            )
        }
    }
}

@Composable
private fun UiCodeFence(codeFence: FencedCodeBlock) {
    val styles = DocumentTheme.current.styles
    Column {
        val code = buildMappedString {
            codeFence.children.forEach { child ->
                when (child) {
                    is Text -> append(child.rawCode())
                    else -> appendUnparsed(child)
                }
            }
        }
        val codeFenceType = codeFence.info.toString()
        val codeFenceRenderer = CodeFenceRenderers.current[codeFenceType]
        if (codeFenceRenderer == null) {
            InteractiveText(
                text = code.text,
                textMapping = code.textMapping,
                style = styles.codeBlock.textStyle,
                modifier = styles.codeBlock.modifier,
            )
        } else {
            codeFenceRenderer.render(code, LocalDocument.current.basePath)
        }
    }
}

@Composable
private fun UiIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock) {
    val styles = DocumentTheme.current.styles
    val lines = indentedCodeBlock.contentLines
    InteractiveText(
        text = lines.joinToString(System.lineSeparator()),
        textMapping = ChunkedSourceTextMapping(lines.map { line ->
            SequenceTextMapping(TextRange(0, line.length), line)
        }),
        style = styles.codeBlock.textStyle,
        modifier = styles.codeBlock.modifier
    )
}

@Composable
private fun UiBlockQuote(blockQuote: BlockQuote) {
    Column(
        modifier = DocumentTheme.current.styles.blockQuote.modifier
    ) {
        blockQuote.children.forEach { child ->
            UiBlock(child)
        }
    }
}

@Composable
private fun UiHorizontalRule() {
    val lineStyle = DocumentTheme.current.lineStyle
    Box(
        modifier = Modifier.fillMaxWidth(1f)
            .then(Modifier.height(lineStyle.width * 2))
            .then(Modifier.border(lineStyle))
    )
}

@Composable
private fun UiUnparsedBlock(node: Node) {
    val text = "!${node.nodeName}!"
    InteractiveText(
        text = text,
        textMapping = ConstantTextMapping(
            TextRange(node.startOffset, node.endOffset), // TODO +1?
            TextRange(0, text.length)
        ),
        style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
    )
}

@Composable
private fun UiParagraph(paragraph: Paragraph) {
    val inlines = parseInlines(paragraph.children)
    val styles = DocumentTheme.current.styles
    InteractiveText(
        text = inlines.text,
        textMapping = inlines.textMapping,
        style = styles.paragraph,
        inlineContent = inlines.inlineContent
    )
}

@Composable
private fun UiHeading(header: Heading) {
    val inlines = parseInlines(header.children)
    val styles = DocumentTheme.current.styles
    InteractiveText(
        text = inlines.text,
        textMapping = inlines.textMapping,
        inlineContent = inlines.inlineContent,
        style = when (header.level) {
            1 -> styles.h1
            2 -> styles.h2
            3 -> styles.h3
            4 -> styles.h4
            5 -> styles.h5
            6 -> styles.h6
            else -> styles.h1
        }
    )
}

// region inlines

@Composable
private fun parseInlines(
    inlines: Iterable<Node>
): MappedText {
    val styles = DocumentTheme.current.styles
    return buildMappedString {
        inlines.forEach { inline ->
            when (inline) {
                is Text -> append(inline.text(visualStartOffset = visualLength))
                is Code -> appendStyled(inline, styles.inlineCode.toSpanStyle())
                is SoftLineBreak -> append(System.lineSeparator())
                is Emphasis -> appendStyled(inline, styles.emphasis.toSpanStyle())
                is StrongEmphasis -> appendStyled(inline, styles.strong.toSpanStyle())
                is Strikethrough -> appendStyled(inline, styles.strikethrough.toSpanStyle())
                is HardLineBreak -> append(System.lineSeparator())
                is Link -> appendLink(inline)
                is AutoLink -> appendStyled(
                    MappedText(
                        inline.text.toString(),
                        SequenceTextMapping(TextRange(visualLength, visualLength + inline.textLength), inline.text)
                    ), styles.link.toSpanStyle()
                )

                is LinkRef -> appendLinkRef(inline)
                is HtmlEntity -> appendStyled(inline, styles.inlineCode.toSpanStyle())
//                    // TODO: proper parsing
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

                else -> appendUnparsed(inline)
            }
        }
    }
}

@Composable
private fun MappedText.Builder.appendLinkRef(linkRef: LinkRef) {
    val reference = linkRef.text.ifEmpty { linkRef.reference }
    appendStyled(
        MappedText(
            text = AnnotatedString(reference.toString()),
            textMapping = SequenceTextMapping(
                TextRange(visualLength, visualLength + reference.length),
                reference
            )
        ), DocumentTheme.current.styles.link.toSpanStyle()
    )
}

@Composable
private fun MappedText.Builder.appendLink(link: Link) {
    val parsedInlines = parseInlines(link.children)
    appendStyled(parsedInlines, DocumentTheme.current.styles.link.toSpanStyle())
}

@Composable
private fun MappedText.Builder.appendUnparsed(unparsedNode: Node) =
    appendStyled(
        unparsedNode,
        DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
    )

private fun MappedText.Builder.appendStyled(mappedText: MappedText, style: SpanStyle) {
    append(
        MappedText(
            text = AnnotatedString(mappedText.text.text, style),
            textMapping = mappedText.textMapping
        )
    )
}

private fun MappedText.Builder.appendStyled(inlineNode: Node, style: SpanStyle) {
    val parsedText = inlineNode.text(visualLength)
    appendStyled(parsedText, style)
}

/**
 * Collects the node text, resolving all escapes.
 */
private fun Node.text(visualStartOffset: Int): MappedText {
    val builder = TextCollectingVisitor()
    builder.collect(this)

    data class ST(
        val sequence: BasedSequence,
        val text: String
    )

    val (sequence, text) = if (builder.sequence.isNull) {
        ST(chars, chars.toString())
    } else {
        ST(builder.sequence, builder.text)
    }

    return MappedText(
        text = AnnotatedString(builder.text),
        textMapping = SequenceTextMapping(
            TextRange(visualStartOffset, visualStartOffset + text.length),
            sequence
        )
    )
}

private class SequenceTextMapping(
    private val coveredVisualRange: TextRange,
    private val sequence: BasedSequence
) : TextMapping {
    init {
        if (sequence.isNull) {
            throw IllegalArgumentException("Sequence cannot be a null sequence")
        }
    }

    override val coveredSourceRange: TextRange by lazy {
        val sourceRange = sequence.sourceRange
        // Include spaces to the covered source range, even though they are not rendered.
        // This mitigates "jumping cursor" when typing spaces at the end of a block.
        val baseSequence = sequence.baseSequence
        val spacesCount = if (sourceRange.end >= baseSequence.endOffset) 0 else "[ \t]*".toRegex()
            .matchAt(baseSequence, sourceRange.end)?.range?.endInclusive
            ?.minus(sourceRange.end - 1)?.coerceAtLeast(0)
            ?: 0
        TextRange(sourceRange.start, sourceRange.end + spacesCount)
    }

    override fun toSource(visualTextRange: TextRange): TextRange? {
        val baseOffset = coveredVisualRange.start
        val shiftedStart = visualTextRange.start - baseOffset
        val shiftedEnd = visualTextRange.end - baseOffset
        if (shiftedStart < 0 || shiftedEnd > sequence.length) return null
        val sourceRange = sequence.subSequence(shiftedStart, shiftedEnd).sourceRange
        return TextRange(sourceRange.start, sourceRange.end)
    }

    override fun toVisual(sourceTextRange: TextRange): TextRange? {
        if (!sourceTextRange.intersects(this.coveredSourceRange) && !this.coveredSourceRange.contains(sourceTextRange))
            return null
        val sourceBase = this.coveredSourceRange.start
        val visualBase = coveredVisualRange.start
        return TextRange(
            (sourceTextRange.start - sourceBase + visualBase).coerceIn(0, coveredVisualRange.end),
            (sourceTextRange.end - sourceBase + visualBase).coerceAtMost(coveredVisualRange.end)
        )
    }

    override fun toString(): String {
        return "SequenceTextMapping(S:${sequence.sourceRange}, V:${coveredVisualRange}"
    }
}

/**
 * Returns the unprocessed Markdown source code corresponding to the node.
 */
private fun Node.rawCode(): MappedText {
    val sequence = this.chars
    return MappedText(chars.toString(), SequenceTextMapping(TextRange(0, sequence.length), sequence))
}

// endregion inlines
