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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import co.touchlab.kermit.Logger
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
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(
    sourceText: String,
    documentTheme: DocumentTheme = DocumentTheme.default,
    interactive: Boolean = true,
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList(),
    onInput: (String) -> Unit = {}
) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.documentParser }
    val document = parser.parse(sourceText)
    // TODO: make the source or the AST be state so we can edit.

    @Composable
    fun renderDocument() {
        CompositionLocalProvider(
            LocalDocumentTheme provides documentTheme,
            CodeFenceRenderers provides codeFenceRenderers.associateBy(CodeFenceRenderer::codeFenceType)
        ) {
            UiMdDocument(document.ast, interactive)
        }
    }

    if (interactive) {
        val interactiveScope = rememberInteractiveScope(sourceText)
        InteractiveContainer(
            scope = interactiveScope,
            selectionStyle = documentTheme.styles.selection,
            onInput = { textInputCommand ->
                val cursor by interactiveScope.cursorPosition
                val selection by interactiveScope.selection
                val layout = interactiveScope.requireComponentLayout()
// TODO: actually edit something.
                val mapping = layout.getComponent(cursor.componentId).textMapping
                val sourceCursorPos = mapping.toSource(TextRange(cursor.visualOffset)).start
                Logger.d("$cursor", tag = "Cursor")
                Logger.d("$textInputCommand@$sourceCursorPos '${sourceText[sourceCursorPos]}'", tag = "onInput")
                when(textInputCommand) {
                    Copy -> TODO()
                    is Delete -> TODO()
                    NewLine -> TODO()
                    Paste -> TODO()
                    is Type -> {
                        onInput(sourceText.substring(0, sourceCursorPos) + textInputCommand.text + sourceText.substring(sourceCursorPos))
                    }
                }
            }
        ) {
            renderDocument()
        }
    } else {
        DisabledInteractiveContainer {
            renderDocument()
        }
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
    InteractiveText(
        text = htmlBlock.contentLines.joinToString(System.lineSeparator()),
        textMapping = ZeroTextMapping, // TODO: replace all ZeroTextMapping with something useful.
        style = styles.codeBlock.textStyle,
        modifier = styles.codeBlock.modifier,
    )
}

@Composable
private fun UiCodeFence(codeFence: FencedCodeBlock) {
    val styles = DocumentTheme.current.styles
    Column {
        val code = buildAnnotatedString {
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
                text = code,
                textMapping = ZeroTextMapping, // TODO
                style = styles.codeBlock.textStyle,
                modifier = styles.codeBlock.modifier,
            )
        } else {
            codeFenceRenderer.render(code.text)
        }
    }
}

@Composable
private fun UiIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock) {
    val styles = DocumentTheme.current.styles
    InteractiveText(
        text = indentedCodeBlock.contentLines.joinToString(System.lineSeparator()),
        textMapping = ZeroTextMapping,
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

private data class MappedText(
    val text: AnnotatedString,
    val textMapping: TextMapping
)

@Composable
private fun parseInlines(inlines: Iterable<Node>): MappedText {
    val styles = DocumentTheme.current.styles
    val mappings = mutableListOf<TextMapping>()
    return MappedText(
        text = buildAnnotatedString {
            inlines.forEach { inline ->
                when (inline) {
                    is Text -> {
                        val parsedText = inline.text(startOffset = length)
                        append(parsedText.text)
                        mappings.add(parsedText.textMapping)
                    }

                    is Code -> appendStyled(inline, styles.inlineCode.toSpanStyle())
                    is SoftLineBreak -> append(" ") // TODO: squash with the following whitespace
                    is Emphasis -> {
                        val parsedText = inline.text(startOffset = length)
                        appendStyled(parsedText.text, styles.emphasis.toSpanStyle())
                        mappings.add(parsedText.textMapping)
                    }

                    is StrongEmphasis -> {
                        val parsedText = inline.text(startOffset = length)
                        appendStyled(parsedText.text, styles.strong.toSpanStyle())
                        mappings.add(parsedText.textMapping) // TODO: make all sections like this
                    }

                    is Strikethrough -> appendStyled(inline, styles.strikethrough.toSpanStyle())
                    is HardLineBreak -> append(System.lineSeparator())
                    is Link -> appendLink(inline)
                    is AutoLink -> appendLink(inline)
                    is LinkRef -> appendLinkRef(inline)
                    is HtmlEntity -> append(inline.text(length).text) // TODO: mapping
//                    // TODO: proper parsing
                    is MailLink -> appendUnparsed(inline)
                    is HtmlInlineBase -> appendUnparsed(inline)
                    is Image -> appendUnparsed(inline)
                    else -> appendUnparsed(inline)
                }
            }
        },
        textMapping = ChunkedSourceTextMapping(mappings)
    )
}

@Composable
private fun AnnotatedString.Builder.appendLinkRef(linkRef: LinkRef) {
    append(AnnotatedString(linkRef.reference.toString(), DocumentTheme.current.styles.link.toSpanStyle()))
}

@Composable
private fun AnnotatedString.Builder.appendLink(link: AutoLink) {
    append(AnnotatedString(link.text.toString(), DocumentTheme.current.styles.link.toSpanStyle()))
}

@Composable
private fun AnnotatedString.Builder.appendLink(link: Link) {
    val parsedInlines = parseInlines(link.children)
    appendStyled(parsedInlines.text, DocumentTheme.current.styles.link.toSpanStyle())
}

@Composable
private fun AnnotatedString.Builder.appendUnparsed(unparsedNode: Node) {
    appendStyled(
        unparsedNode,
        DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
    )
}

private fun AnnotatedString.Builder.appendStyled(annotatedString: AnnotatedString, style: SpanStyle) {
    val start = length
    append(annotatedString)
    val end = length
    addStyle(style, start, end)
}

private fun AnnotatedString.Builder.appendStyled(inlineNode: Node, style: SpanStyle) {
    append(AnnotatedString(inlineNode.text(length).text.text, style))
}

/**
 * Collects the node text, resolving all escapes.
 */
private fun Node.text(startOffset: Int): MappedText {
    val builder = TextCollectingVisitor()
    builder.collect(this)
    val text = builder.text
    return MappedText(
        text = AnnotatedString(builder.text),
        textMapping = SequenceTextMapping(
            TextRange(startOffset, startOffset + text.length),
            builder.sequence
        )
    )
}

private class SequenceTextMapping(
    private val coveredVisualRange: TextRange,
    private val sequence: BasedSequence
) : TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange {
        val baseOffset = coveredVisualRange.start
        val shiftedStart = visualTextRange.start - baseOffset
        val shiftedEnd = visualTextRange.end - baseOffset
        if (shiftedStart < 0 || shiftedEnd > sequence.lastIndex) return TextRange.Zero
        val sourceRange = sequence.subSequence(shiftedStart, shiftedEnd).sourceRange
        return TextRange(sourceRange.start, sourceRange.end)
    }

    override fun toVisual(sourceTextRange: TextRange): TextRange {
        TODO("Not yet implemented")
    }

}

/**
 * Returns the unprocessed Markdown source code corresponding to the node.
 */
private fun Node.rawCode(): String {
    return this.chars.toString()
}

// endregion inlines