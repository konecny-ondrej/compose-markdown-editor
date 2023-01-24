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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.intellij.markdown.MarkdownTokenTypes

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
private fun UiMdDocument(markdownRoot: MdDocument, sourceText: String) {
    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        items(markdownRoot.children) { child: MdDocumentChild ->
            when(child) {
                is MdAtxHeader -> UiAtxHeader(child, sourceText)
                is MdSetextHeader -> UiSetextHeader(child, sourceText)
                is MdParagraph -> UiParagraph(child, sourceText)
                is MdHorizontalRule -> UiHorizontalRule()
                is MdBlockQuote -> UiBlockQuote(child, sourceText)
                is MdIndentedCodeBlock -> UiUnparsed(child) // TODO
                is MdCodeFence -> UiUnparsed(child)
                is MdHtmlBlock -> UiUnparsed(child)
                is MdLinkDefinition -> UiUnparsed(child)
                is MdOrderedList -> UiUnparsed(child)
                is MdTable -> UiUnparsed(child)
                is MdUnorderedList -> UiUnparsed(child)
                is MdUnparsed -> UiUnparsed(child)
                is MdIgnored -> Unit
            }
        }
    }
}

@Composable
private fun UiBlockQuote(blockQuote: MdBlockQuote, sourceText: String) {
    Column(
        modifier = DocumentTheme.current.styles.blockQuote.modifier
    ) {
        blockQuote.children.forEach { child ->
            when (child) {
                else -> UiUnparsed(child) // TODO
            }
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
private fun UiUnparsed(node: MdDomNode) {
    val tag = when(node) {
        is MdUnparsed -> node.name
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
        style = when(header.level) {
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
        style = when(header.level) {
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

private fun parseInlines(inlines: List<MdInline>, sourceText: String): ParsedInlines {
    return ParsedInlines(
        text = buildAnnotatedString {
            inlines.forEach { inline ->
                // TODO: proper parsing
                append(inline.text(sourceText))
            }
        },
        inlineContent = mapOf()
    )
}

// endregion inlines