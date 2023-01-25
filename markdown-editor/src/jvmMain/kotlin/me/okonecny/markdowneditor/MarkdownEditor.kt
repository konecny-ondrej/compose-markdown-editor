package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        UiMdDocument(document.dom, sourceText)
    }
}

@Composable
private fun UiBlock(block: MdBlock, sourceText: String) {
    when (block) {
        is MdIgnoredBlock -> Unit
        is MdEol -> Unit // Not significant between blocks.
        is MdAtxHeader -> UiAtxHeader(block, sourceText)
        is MdSetextHeader -> UiSetextHeader(block, sourceText)
        is MdParagraph -> UiParagraph(block, sourceText)
        is MdHorizontalRule -> UiHorizontalRule()
        is MdBlockQuote -> UiBlockQuote(block, sourceText)
        is MdIndentedCodeBlock -> UiIndentedCodeBlock(block, sourceText)
        is MdCodeFence -> UiCodeFence(block, sourceText)
        is MdHtmlBlock -> UiHtmlBlock(block, sourceText)
        is MdOrderedList -> UiOrderedList(block, sourceText)
        is MdUnorderedList -> UiUnorderedList(block, sourceText)
        is MdTable -> UiUnparsedBlock(block) // TODO
        is MdLinkDefinition -> UiUnparsedBlock(block)
        is MdUnparsedBlock -> UiUnparsedBlock(block)
    }
}

@Composable
private fun UiUnorderedList(unorderedList: MdUnorderedList, sourceText: String) {
    val styles = DocumentTheme.current.styles
    Column {
        unorderedList.children.forEach { child ->
            Row {
                Text(
                    text = "\u2022",
                    style = styles.listNumber
                )
                Column {
                    child.children.forEach { listItemContent ->
                        UiBlock(listItemContent, sourceText)
                    }
                }
            }
        }
    }

}

@Composable
private fun UiOrderedList(orderedList: MdOrderedList, sourceText: String) {
    val styles = DocumentTheme.current.styles
    Column {
        var computedNumber: Int = -1
        orderedList.children.forEach { child ->
            Row {
                Text(
                    text = if (computedNumber == -1) { // TODO: moce this logic to the parser?
                        computedNumber = child.number.text(sourceText).trim('.', ')', ' ').toInt()
                        computedNumber++
                    } else {
                        computedNumber++
                    }.toString() + ". ",
                    style = styles.listNumber
                )
                Column {
                    child.children.forEach { listItemContent ->
                        UiBlock(listItemContent, sourceText)
                    }
                }
            }
        }
    }
}

@Composable
private fun UiHtmlBlock(htmlBlock: MdHtmlBlock, sourceText: String) {
    val styles = DocumentTheme.current.styles
    Column(
        modifier = styles.codeBlock.modifier
    ) {
        htmlBlock.children.forEach { child ->
            when (child) {
                is MdHtmlBlockContent -> Text(
                    child.text(sourceText),
                    style = styles.codeBlock.textStyle
                )

                is MdIgnoredBlock -> Unit
                else -> UiUnparsedBlock(child)
            }
        }
    }
}

@Composable
private fun UiCodeFence(codeFence: MdCodeFence, sourceText: String) {
    val styles = DocumentTheme.current.styles
    Column(
        modifier = styles.codeBlock.modifier
    ) {
        val code = buildAnnotatedString {
            codeFence.children.forEach { child ->
                when (child) {
                    is MdCodeFenceLine -> append(child.text(sourceText))
                    is MdEol -> append(System.lineSeparator())
                    is MdIgnoredInline -> Unit
                    else -> appendUnparsed(child, sourceText)
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
private fun UiIndentedCodeBlock(indentedCodeBlock: MdIndentedCodeBlock, sourceText: String) {
    val styles = DocumentTheme.current.styles
    Column(
        modifier = styles.codeBlock.modifier
    ) {
        indentedCodeBlock.children.forEach { child ->
            when (child) {
                is MdIndentedCodeLine -> Text(
                    child.text(sourceText),
                    style = styles.codeBlock.textStyle
                )

                is MdUnparsedBlock -> UiUnparsedBlock(child)
            }
        }
    }
}

@Composable
private fun UiMdDocument(markdownRoot: MdDocument, sourceText: String) {
    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        items(markdownRoot.children) { child: MdBlock ->
            UiBlock(child, sourceText)
        }
    }
}

@Composable
private fun UiBlockQuote(blockQuote: MdBlockQuote, sourceText: String) {
    Column(
        modifier = DocumentTheme.current.styles.blockQuote.modifier
    ) {
        blockQuote.children.forEach { child ->
            UiBlock(child, sourceText)
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
private fun UiUnparsedBlock(node: MdDomNode) {
    val tag = when (node) {
        is MdUnparsedBlock -> node.name
        else -> node::class.simpleName
    }

    Text(
        text = "!${tag}!",
        style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
    )
}

@Composable
private fun UiParagraph(paragraph: MdParagraph, sourceText: String) {
    val inlines = parseInlines(paragraph.children, sourceText)
    val styles = DocumentTheme.current.styles
    Text(
        text = inlines.text,
        inlineContent = inlines.inlineContent,
        style = styles.paragraph
    )
}

@Composable
fun UiSetextHeader(header: MdSetextHeader, sourceText: String) {
    val inlines = parseInlines(header.children, sourceText)
    val styles = DocumentTheme.current.styles
    Text(
        text = inlines.text,
        inlineContent = inlines.inlineContent,
        style = when (header.level) {
            MdSetextHeader.Level.H1 -> styles.h1
            MdSetextHeader.Level.H2 -> styles.h2
        }
    )
}

@Composable
private fun UiAtxHeader(header: MdAtxHeader, sourceText: String) {
    val inlines = parseInlines(header.children, sourceText)
    val styles = DocumentTheme.current.styles
    Text(
        text = inlines.text,
        inlineContent = inlines.inlineContent,
        style = when (header.level) {
            MdAtxHeader.Level.H1 -> styles.h1
            MdAtxHeader.Level.H2 -> styles.h2
            MdAtxHeader.Level.H3 -> styles.h3
            MdAtxHeader.Level.H4 -> styles.h4
            MdAtxHeader.Level.H5 -> styles.h5
            MdAtxHeader.Level.H6 -> styles.h6
        }
    )
}

// region inlines

private data class ParsedInlines(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>
)

@Composable
private fun parseInlines(inlines: List<MdInline>, sourceText: String): ParsedInlines {
    val styles = DocumentTheme.current.styles
    return ParsedInlines(
        text = buildAnnotatedString {
            inlines.forEach { inline ->
                when (inline) {
                    is MdIgnoredInline -> Unit
                    is MdUnparsedInline -> appendUnparsed(inline, sourceText)
                    is MdText -> append(inline.text(sourceText))
                    is MdCodeSpan -> appendStyled(inline, sourceText, styles.inlineCode.toSpanStyle())
                    is MdEmphasis -> appendStyled(inline, sourceText, styles.emphasis.toSpanStyle())
                    is MdStrong -> appendStyled(inline, sourceText, styles.strong.toSpanStyle())
                    is MdStrikethrough -> appendStyled(inline, sourceText, styles.strikethrough.toSpanStyle())
                    is MdHardLineBreakToken -> append(System.lineSeparator())
                    is MdEol -> append(System.lineSeparator())
                    is MdInsignificantWhitespace -> append(" ")
                    // TODO: proper parsing
                    is MdAutolinkToken -> appendUnparsed(inline, sourceText)
                    is MdCheckBoxToken -> appendUnparsed(inline, sourceText)
                    is MdEmailAutolinkToken -> appendUnparsed(inline, sourceText)
                    is MdFullReferenceLink -> appendUnparsed(inline, sourceText)
                    is MdGfmAutolinkToken -> appendUnparsed(inline, sourceText)
                    is MdHtmlTagToken -> appendUnparsed(inline, sourceText)
                    is MdImage -> appendUnparsed(inline, sourceText)
                    is MdInlineLink -> appendUnparsed(inline, sourceText)
                    is MdShortReferenceLink -> appendUnparsed(inline, sourceText)
                }
            }
        },
        inlineContent = mapOf()
    )
}

@Composable
private fun AnnotatedString.Builder.appendUnparsed(unparsedNode: MdDomNode, sourceText: String) {
    appendStyled(
        unparsedNode,
        sourceText,
        DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
    )
}

private fun AnnotatedString.Builder.appendStyled(inlineNode: MdDomNode, sourceText: String, style: SpanStyle) {
    val start = length
    val text = inlineNode.text(sourceText)
    append(text)
    val end = length
    addStyle(style, start, end)
}

// endregion inlines