package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor

/**
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(sourceText: String, documentTheme: DocumentTheme = DocumentTheme.default) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.documentParser }
    val document = parser.parse(sourceText)
    // TODO: make the source or the AST be state so we can edit.

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme
    ) {
        UiMdDocument(document.ast)
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
        else -> UiUnparsedBlock(block)
    }
}

@Composable
private fun UiBulletList(unorderedList: BulletList) {
    val styles = DocumentTheme.current.styles
    Column {
        unorderedList.children.forEach { child ->
            when (child) {
                is BulletListItem -> Row {
                    Text(
                        text = "\u2022",
                        style = styles.listNumber
                    )
                    Column {
                        child.children.forEach { listItemContent ->
                            UiBlock(listItemContent)
                        }
                    }
                }
                // TODO: task list item
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
                is OrderedListItem -> Row {
                    Text(
                        text = (computedNumber++).toString() + orderedList.delimiter,
                        style = styles.listNumber
                    )
                    Column {
                        child.children.forEach { listItemContent ->
                            UiBlock(listItemContent)
                        }
                    }
                }
                // TODO: task list item
                else -> UiUnparsedBlock(child)
            }

        }
    }
}

@Composable
private fun UiHtmlBlock(htmlBlock: HtmlBlock) {
    val styles = DocumentTheme.current.styles
    Text(
        text = htmlBlock.contentLines.joinToString(System.lineSeparator()),
        style = styles.codeBlock.textStyle,
        modifier = styles.codeBlock.modifier
    )
}

@Composable
private fun UiCodeFence(codeFence: FencedCodeBlock) {
    val styles = DocumentTheme.current.styles
    Column(
        modifier = styles.codeBlock.modifier
    ) {
        val code = buildAnnotatedString {
            codeFence.children.forEach { child ->
                when (child) {
                    is Text -> append(child.text())
                    else -> appendUnparsed(child)
                }
            }
        }
        Text(
            text = code,
            style = styles.codeBlock.textStyle
        )
    }
}

@Composable
private fun UiIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock) {
    val styles = DocumentTheme.current.styles
    Text(
        text = indentedCodeBlock.contentLines.joinToString(System.lineSeparator()),
        style = styles.codeBlock.textStyle,
        modifier = styles.codeBlock.modifier
    )
}

@Composable
private fun UiMdDocument(markdownRoot: Document) {
    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        markdownRoot.children.forEach { child ->
            item {
                UiBlock(child)
            }
        }
    }
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
    Text(
        text = "!${node.nodeName}!",
        style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
    )
}

@Composable
private fun UiParagraph(paragraph: Paragraph) {
    val inlines = parseInlines(paragraph.children)
    val styles = DocumentTheme.current.styles
    Text(
        text = inlines.text,
        inlineContent = inlines.inlineContent,
        style = styles.paragraph
    )
}

@Composable
private fun UiHeading(header: Heading) {
    val inlines = parseInlines(header.children)
    val styles = DocumentTheme.current.styles
//    System.err.println(header.anchorRefId)
    Text(
        text = inlines.text,
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
//                    // TODO: proper parsing
                    is MailLink -> appendUnparsed(inline)
                    is AutoLink -> appendUnparsed(inline)
                    is HtmlInlineBase -> appendUnparsed(inline)
                    is Image -> appendUnparsed(inline)
                    is Link -> appendUnparsed(inline)
                    else -> appendUnparsed(inline)
                }
            }
        },
        inlineContent = mapOf()
    )
}

@Composable
private fun AnnotatedString.Builder.appendUnparsed(unparsedNode: Node) {
    appendStyled(
        unparsedNode,
        DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
    )
}

private fun AnnotatedString.Builder.appendStyled(inlineNode: Node, style: SpanStyle) {
    val start = length
    append(inlineNode.text())
    val end = length
    addStyle(style, start, end)
}

private fun Node.text(): String {
    val builder = TextCollectingVisitor()
    return builder.collectAndGetText(this)
}

// endregion inlines