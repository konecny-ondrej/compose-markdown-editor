package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
        is MdIndentedCodeBlock -> UiUnparsedBlock(block) // TODO
        is MdCodeFence -> UiUnparsedBlock(block)
        is MdHtmlBlock -> UiUnparsedBlock(block)
        is MdLinkDefinition -> UiUnparsedBlock(block)
        is MdOrderedList -> UiUnparsedBlock(block)
        is MdTable -> UiUnparsedBlock(block)
        is MdUnorderedList -> UiUnparsedBlock(block)
        is MdUnparsedBlock -> UiUnparsedBlock(block)
        is MdOrderedListItem -> UiUnparsedBlock(block)
        is MdUnorderedListItem -> UiUnparsedBlock(block)
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
private fun UiUnparsedBlock(node: MdBlock) {
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
                // TODO: proper parsing
                when (inline) {
                    is MdIgnoredInline -> Unit
                    is MdUnparsedInline -> appendUnparsed(inline, sourceText)
                    is MdText -> append(inline.text(sourceText))
                    is MdCodeSpan -> appendStyled(inline, sourceText, styles.inlineCode.toSpanStyle())
                    is MdEmphasis -> appendStyled(inline, sourceText, styles.emphasis.toSpanStyle())
                    is MdStrong -> appendStyled(inline, sourceText, styles.strong.toSpanStyle())
                    // TODO
                    is MdAutolinkToken -> appendUnparsed(inline, sourceText)
                    is MdCheckBoxToken -> appendUnparsed(inline, sourceText)
                    is MdEmailAutolinkToken -> appendUnparsed(inline, sourceText)
                    is MdEol -> appendUnparsed(inline, sourceText)
                    is MdFullReferenceLink -> appendUnparsed(inline, sourceText)
                    is MdGfmAutolinkToken -> appendUnparsed(inline, sourceText)
                    is MdHardLineBreakToken -> appendUnparsed(inline, sourceText)
                    is MdHtmlTagToken -> appendUnparsed(inline, sourceText)
                    is MdImage -> appendUnparsed(inline, sourceText)
                    is MdInlineLink -> appendUnparsed(inline, sourceText)
                    is MdShortReferenceLink -> appendUnparsed(inline, sourceText)
                    is MdStrikethrough -> appendUnparsed(inline, sourceText)
                    is MdWhitespace -> append(" ")
                }
            }
        },
        inlineContent = mapOf()
    )
}

@Composable
private fun AnnotatedString.Builder.appendUnparsed(unparsedNode: MdInline, sourceText: String) {
    appendStyled(
        unparsedNode,
        sourceText,
        DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
    )
}

private fun AnnotatedString.Builder.appendStyled(inlineNode: MdInline, sourceText: String, style: SpanStyle) {
    val start = length
    val text = inlineNode.text(sourceText)
    append(text)
    val end = length
    addStyle(style, start, end)
}

// endregion inlines