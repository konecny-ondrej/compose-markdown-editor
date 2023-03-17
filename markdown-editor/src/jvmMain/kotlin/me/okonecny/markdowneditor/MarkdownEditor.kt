package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
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
import me.okonecny.markdowneditor.internal.*
import me.okonecny.markdowneditor.internal.interactive.InteractiveContainer
import me.okonecny.markdowneditor.internal.interactive.InteractiveText
import me.okonecny.markdowneditor.internal.interactive.rememberInteractiveScope

/**
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(
    sourceText: String,
    documentTheme: DocumentTheme = DocumentTheme.default,
    interactive: Boolean = true,
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
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
        InteractiveContainer(
            scope = rememberInteractiveScope(sourceText),
            selectionStyle = documentTheme.styles.selection,
            onInput = { textInputCommand ->
                Logger.d(textInputCommand.toString(), tag = "onInput")
            }
        ) {
            renderDocument()
        }
    } else {
        InteractiveContainer(scope = null) {
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
    InteractiveText(
        text = "!${node.nodeName}!",
        style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan),
    )
}

@Composable
private fun UiParagraph(paragraph: Paragraph) {
    val inlines = parseInlines(paragraph.children)
    val styles = DocumentTheme.current.styles
    InteractiveText(
        text = inlines.text,
        style = styles.paragraph,
        inlineContent = inlines.inlineContent,
    )
}

@Composable
private fun UiHeading(header: Heading) {
    val inlines = parseInlines(header.children)
    val styles = DocumentTheme.current.styles
    InteractiveText(
        text = inlines.text,
        style = when (header.level) {
            1 -> styles.h1
            2 -> styles.h2
            3 -> styles.h3
            4 -> styles.h4
            5 -> styles.h5
            6 -> styles.h6
            else -> styles.h1
        },
        inlineContent = inlines.inlineContent,
    )
}

// region inlines

private data class ParsedInlines(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>
)

@Composable
private fun parseInlines(inlines: Iterable<Node>): ParsedInlines {
    val styles = DocumentTheme.current.styles
    return ParsedInlines(
        text = buildAnnotatedString {
            inlines.forEach { inline ->
                when (inline) {
                    is Text -> append(inline.text())
                    is Code -> appendStyled(inline, styles.inlineCode.toSpanStyle())
                    is SoftLineBreak -> append(" ") // TODO: squash with the following whitespace
                    is Emphasis -> appendStyled(inline, styles.emphasis.toSpanStyle())
                    is StrongEmphasis -> appendStyled(inline, styles.strong.toSpanStyle())
                    is Strikethrough -> appendStyled(inline, styles.strikethrough.toSpanStyle())
                    is HardLineBreak -> append(System.lineSeparator())
                    is Link -> appendLink(inline)
                    is AutoLink -> appendLink(inline)
                    is LinkRef -> appendLinkRef(inline)
                    is HtmlEntity -> append(inline.text())
//                    // TODO: proper parsing
                    is MailLink -> appendUnparsed(inline)
                    is HtmlInlineBase -> appendUnparsed(inline)
                    is Image -> appendUnparsed(inline)
                    else -> appendUnparsed(inline)
                }
            }
        },
        inlineContent = mapOf()
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
    append(AnnotatedString(inlineNode.text(), style))
}

/**
 * Collects the node text, resolving all escapes.
 */
private fun Node.text(): String {
    val builder = TextCollectingVisitor()
    return builder.collectAndGetText(this)
}

/**
 * Returns the unprocessed Markdown source code corresponding to the node.
 */
private fun Node.rawCode(): String {
    return this.chars.toString()
}

// endregion inlines