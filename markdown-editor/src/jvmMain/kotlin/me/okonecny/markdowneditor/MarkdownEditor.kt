package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownElementTypes.ATX_1
import org.intellij.markdown.MarkdownElementTypes.ATX_2
import org.intellij.markdown.MarkdownElementTypes.ATX_3
import org.intellij.markdown.MarkdownElementTypes.ATX_4
import org.intellij.markdown.MarkdownElementTypes.ATX_5
import org.intellij.markdown.MarkdownElementTypes.ATX_6
import org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK
import org.intellij.markdown.MarkdownElementTypes.CODE_FENCE
import org.intellij.markdown.MarkdownElementTypes.CODE_SPAN
import org.intellij.markdown.MarkdownElementTypes.LIST_ITEM
import org.intellij.markdown.MarkdownElementTypes.MARKDOWN_FILE
import org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST
import org.intellij.markdown.MarkdownElementTypes.PARAGRAPH
import org.intellij.markdown.MarkdownElementTypes.SETEXT_1
import org.intellij.markdown.MarkdownElementTypes.SETEXT_2
import org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.MarkdownTokenTypes.Companion.BACKTICK
import org.intellij.markdown.MarkdownTokenTypes.Companion.CODE_FENCE_CONTENT
import org.intellij.markdown.MarkdownTokenTypes.Companion.CODE_FENCE_END
import org.intellij.markdown.MarkdownTokenTypes.Companion.CODE_FENCE_START
import org.intellij.markdown.MarkdownTokenTypes.Companion.COLON
import org.intellij.markdown.MarkdownTokenTypes.Companion.EOL
import org.intellij.markdown.MarkdownTokenTypes.Companion.EXCLAMATION_MARK
import org.intellij.markdown.MarkdownTokenTypes.Companion.FENCE_LANG
import org.intellij.markdown.MarkdownTokenTypes.Companion.GT
import org.intellij.markdown.MarkdownTokenTypes.Companion.HORIZONTAL_RULE
import org.intellij.markdown.MarkdownTokenTypes.Companion.LBRACKET
import org.intellij.markdown.MarkdownTokenTypes.Companion.LPAREN
import org.intellij.markdown.MarkdownTokenTypes.Companion.LT
import org.intellij.markdown.MarkdownTokenTypes.Companion.RBRACKET
import org.intellij.markdown.MarkdownTokenTypes.Companion.RPAREN
import org.intellij.markdown.MarkdownTokenTypes.Companion.TEXT
import org.intellij.markdown.MarkdownTokenTypes.Companion.WHITE_SPACE
import org.intellij.markdown.ast.ASTNode

/**
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(sourceText: String, documentTheme: DocumentTheme = DocumentTheme.default) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.parser }
    val markdownRoot = parser.buildMarkdownTreeFromString(sourceText)
    // TODO: make the source or the AST be state so we can edit.

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme
    ) {
        LazyColumn {
            items(markdownRoot.children) { node ->
                RenderedNode(node, sourceText)
            }
        }
    }
}

/**
 * Component to render any Markdown Element.
 * @see MarkdownElementTypes
 * @see MarkdownTokenTypes
 * @see GFMElementTypes
 * @see GFMTokenTypes
 */
@Composable
private fun RenderedNode(node: ASTNode, sourceText: String) {
    val styles = DocumentTheme.current.styles
    when (node.type) {
        // Blocks
        MARKDOWN_FILE -> File(node, sourceText) // Not really used.
        ATX_1 -> AtxHeader(node, sourceText, styles.h1)
        SETEXT_1 -> SetextHeader(node, sourceText, styles.h1)
        ATX_2 -> AtxHeader(node, sourceText, styles.h2)
        SETEXT_2 -> SetextHeader(node, sourceText, styles.h2)
        ATX_3 -> AtxHeader(node, sourceText, styles.h3)
        ATX_4 -> AtxHeader(node, sourceText, styles.h4)
        ATX_5 -> AtxHeader(node, sourceText, styles.h5)
        ATX_6 -> AtxHeader(node, sourceText, styles.h6)
        PARAGRAPH -> Paragraph(node, sourceText)
        CODE_BLOCK -> CodeBlock(node, sourceText)
        ORDERED_LIST -> OrderedList(node, sourceText)
        UNORDERED_LIST -> UnorderedList(node, sourceText)
        LIST_ITEM -> ListItem(node, sourceText)
        HORIZONTAL_RULE -> HorizontalRule()
        CODE_FENCE -> CodeFence(node, sourceText)
    }
}

@Composable
private fun File(fileNode: ASTNode, sourceText: String) {
    fileNode.children.forEach { node -> RenderedNode(node, sourceText) }
}

@Composable
private fun AtxHeader(headerNode: ASTNode, sourceText: String, headerStyle: TextStyle) {
    val headerContent = headerNode.findChildByType(MarkdownTokenTypes.ATX_CONTENT)
    Text(headerContent.text(sourceText).trim(), style = headerStyle)
}

@Composable
private fun SetextHeader(headerNode: ASTNode, sourceText: String, headerStyle: TextStyle) {
    val headerContent = headerNode.findChildByType(MarkdownTokenTypes.SETEXT_CONTENT)
    Text(headerContent.text(sourceText).trim(), style = headerStyle)
}

@Composable
private fun CodeBlock(blockNode: ASTNode, sourceText: String) {
    // TODO: add CodeBlock style with padding and background for the whole block
    Text(
        blockNode.text(sourceText),
        style = DocumentTheme.current.styles.code
    )
}

private enum class ListTypes {
    ORDERED,
    UNORDERED
}

private val ListType = compositionLocalOf { ListTypes.ORDERED }
private val ListNumber = compositionLocalOf { 0 }

@Composable
private fun OrderedList(listNode: ASTNode, sourceText: String) {
    Column {
        var currentListItem = 0
        listNode.children.forEach { itemNode ->
            if (itemNode.type == LIST_ITEM) currentListItem++
            CompositionLocalProvider(
                ListNumber provides currentListItem,
                ListType provides ListTypes.ORDERED
            ) {
                RenderedNode(itemNode, sourceText)
            }
        }
    }
}

@Composable
private fun UnorderedList(listNode: ASTNode, sourceText: String) {
    Column {
        listNode.children.forEach { itemNode ->
            CompositionLocalProvider(
                ListType provides ListTypes.UNORDERED
            ) {
                RenderedNode(itemNode, sourceText)
            }
        }
    }
}

@Composable
private fun ListItem(itemNode: ASTNode, sourceText: String) {
    Row {
        when (ListType.current) {
            ListTypes.ORDERED -> Text(
                "${ListNumber.current}. ",
                style = DocumentTheme.current.styles.listNumber
            )

            ListTypes.UNORDERED -> Text(
                "\u2022 ", // TODO: parse the LIST_BULLET
                style = DocumentTheme.current.styles.listNumber
            )
        }

        Column {
            itemNode.children.forEach { itemContentNode ->
                RenderedNode(itemContentNode, sourceText)
            }
        }
    }
}

@Composable
fun HorizontalRule() {
    val lineStyle = DocumentTheme.current.lineStyle
    Box(
        modifier = Modifier.fillMaxWidth(1f)
            .then(Modifier.height(lineStyle.width))
            .then(Modifier.border(lineStyle))
    )
}

@Composable
private fun Paragraph(paragraphNode: ASTNode, sourceText: String) {
    // TODO add padding to style (spaces between paragraphs)
    val parsedContent = parseParagraphContent(paragraphNode, sourceText)
    Text(
        parsedContent.text,
        inlineContent = parsedContent.inlineTextContent,
        style = DocumentTheme.current.styles.paragraph
    )
    // TODO: inline elements (images, links, highlights)
}

private data class MarkdownParsedInline(
    val text: AnnotatedString,
    val inlineTextContent: Map<String, InlineTextContent>
)

typealias IdToInlineContent = MutableMap<String, InlineTextContent>

@Composable
private fun parseParagraphContent(blockNode: ASTNode, sourceText: String): MarkdownParsedInline {
    val inlineContent: IdToInlineContent = mutableMapOf()
    val annotatedStringBuilder = AnnotatedString.Builder()

    var lastWasWhitespace = false
    blockNode.children.forEach { child ->
        when (child.type) {
            WHITE_SPACE, EOL -> {
                if (!lastWasWhitespace) {
                    annotatedStringBuilder.append(" ")
                }
            }

            TEXT, LPAREN, RPAREN, LBRACKET, RBRACKET,
            LT, GT, COLON, EXCLAMATION_MARK -> annotatedStringBuilder.append(child.text(sourceText))

            CODE_SPAN -> CodeSpan(child, sourceText, annotatedStringBuilder)
        }
        lastWasWhitespace = child.type == WHITE_SPACE || child.type == EOL
    }

    return MarkdownParsedInline(
        annotatedStringBuilder.toAnnotatedString(),
        inlineContent
    )
}

@Composable
private fun CodeSpan(
    inlineNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder
) {
    inlineNode.children.forEach { child ->
        when (child.type) {
            BACKTICK -> Unit // Don't render the backticks around code.
            else -> CodeSpanText(child, sourceText, asBuilder)
        }
    }
}

@Composable
private fun CodeSpanText(
    inlineNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder
) {
    val currentLength = asBuilder.length
    val codeText = inlineNode.text(sourceText)
    asBuilder.append(codeText)
    asBuilder.addStyle(
        LocalDocumentTheme.current.styles.code.toSpanStyle(),
        currentLength,
        currentLength + codeText.length
    )
}

@Composable
private fun CodeFence(blockNode: ASTNode, sourceText: String) {
    val inlineContent: IdToInlineContent = mutableMapOf()
    val asBuilder = AnnotatedString.Builder()

    blockNode.children.forEach { child ->
        when (child.type) {
            CODE_FENCE_START, CODE_FENCE_END, FENCE_LANG -> Unit // TODO: syntax highlighting
            CODE_FENCE_CONTENT -> asBuilder.append(child.text(sourceText))
            EOL -> asBuilder.append("\n")
        }
    }

    // TODO: background and padding for the whole block
    Text(
        asBuilder.toAnnotatedString(),
        inlineContent = inlineContent,
        style = DocumentTheme.current.styles.code
    )
}
