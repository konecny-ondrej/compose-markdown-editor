package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.ext.tables.*
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import com.vladsch.flexmark.util.sequence.BasedSequence
import me.okonecny.interactivetext.*
import me.okonecny.markdowneditor.internal.*

/**
 * Renders a Markdown document nicely.
 */
@Composable
fun MarkdownView(
    sourceText: String,
    documentTheme: DocumentTheme = DocumentTheme.default,
    scrollable: Boolean = true,
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.documentParser }
    val document = remember(sourceText) { parser.parse(sourceText) }

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme,
        CodeFenceRenderers provides codeFenceRenderers.associateBy(CodeFenceRenderer::codeFenceType)
    ) {
        UiMdDocument(document.ast, scrollable)
    }
}

private val CodeFenceRenderers = compositionLocalOf { emptyMap<String, CodeFenceRenderer>() }

@Composable
private fun UiMdDocument(markdownRoot: Document, scrollable: Boolean) {
    if (scrollable) {
        LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
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
private fun TableScope.UiTableRowCells(rowType: RowType, tableSectionRows: Iterable<Node>) {
    tableSectionRows.forEach { tableRow ->
        when (tableRow) {
            is TableRow -> UiTableRow(rowType) {
                tableRow.children.forEach { tableHeadCell ->
                    when (tableHeadCell) {
                        is TableCell -> UiTableCell(
                            parseInlines(tableHeadCell.children).text,
                            textAlign = when (tableHeadCell.alignment) {
                                TableCell.Alignment.RIGHT -> TextAlign.Right
                                TableCell.Alignment.CENTER -> TextAlign.Center
                                else -> TextAlign.Left
                            }
                        )

                        else -> UiTableCell("Bad table cell", TextAlign.Left)
                    }

                }
            }

            else -> UiTableRow(rowType) { UiTableCell("Bad table row", TextAlign.Left) }
        }
    }
}

@Composable
private fun UiTableBlock(tableBlock: TableBlock) {
    UiTable {
        tableBlock.children.forEach { tableChild ->
            when (tableChild) {
                is TableHead -> UiTableRowCells(RowType.HEADER, tableChild.children)
                is TableBody -> UiTableRowCells(RowType.BODY, tableChild.children)
                is TableSeparator -> Unit // This is just a Markdown syntax to specify alignment of the columns.
                else -> UiUnparsedBlock(tableChild)
            }
        }
    }
}

@Composable
private fun UiTaskListItem(taskListItem: TaskListItem, bulletOrDelimiter: String, number: Int? = null) {
    val styles = DocumentTheme.current.styles
    Row {
        InteractiveText(
            text = if (taskListItem.isOrderedItem) {
                number.toString() + bulletOrDelimiter
            } else {
                bulletOrDelimiter
            },
            textMapping = ZeroTextMapping, // TODO: text mapping for the list item bullet.
            style = styles.listNumber,
        )
        Checkbox(checked = taskListItem.isItemDoneMarker, onCheckedChange = null)
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
                is TaskListItem -> UiTaskListItem(child, bulletOrDelimiter = bullet)
                is BulletListItem -> Row {
                    InteractiveText(
                        text = bullet,
                        textMapping = ZeroTextMapping, // TODO: text mapping for the list item bullet.
                        style = styles.listNumber,
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
                    number = computedNumber++
                )

                is OrderedListItem -> Row {
                    InteractiveText(
                        text = (computedNumber++).toString() + orderedList.delimiter,
                        textMapping = ZeroTextMapping, // TODO: text mapping for the list item number and delimiter.
                        style = styles.listNumber,
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
            codeFenceRenderer.render(code.text.text) // Pass the whole text with mapping?
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
        modifier = styles.codeBlock.modifier,
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
        style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan),
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
    )
}

@Composable
private fun UiHeading(header: Heading) {
    val inlines = parseInlines(header.children)
    val styles = DocumentTheme.current.styles
    InteractiveText(
        text = inlines.text,
        textMapping = inlines.textMapping,
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
private fun parseInlines(inlines: Iterable<Node>): MappedText {
    val styles = DocumentTheme.current.styles
    return buildMappedString {
        inlines.forEach { inline ->
            when (inline) {
                is Text -> append(inline.text(visualStartOffset = visualLength))
                is Code -> appendStyled(inline, styles.inlineCode.toSpanStyle())
                is SoftLineBreak -> append(" ") // TODO: squash with the following whitespace
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
                is Image -> appendUnparsed(inline)
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
            (sourceTextRange.start - sourceBase + visualBase).coerceAtMost(coveredVisualRange.end),
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
private fun Node.rawCode(): String {
    return this.chars.toString()
}

// endregion inlines