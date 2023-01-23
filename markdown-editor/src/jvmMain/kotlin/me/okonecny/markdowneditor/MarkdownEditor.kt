package me.okonecny.markdowneditor

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownElementTypes.ATX_1
import org.intellij.markdown.MarkdownElementTypes.ATX_2
import org.intellij.markdown.MarkdownElementTypes.ATX_3
import org.intellij.markdown.MarkdownElementTypes.ATX_4
import org.intellij.markdown.MarkdownElementTypes.ATX_5
import org.intellij.markdown.MarkdownElementTypes.ATX_6
import org.intellij.markdown.MarkdownElementTypes.BLOCK_QUOTE
import org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK
import org.intellij.markdown.MarkdownElementTypes.CODE_FENCE
import org.intellij.markdown.MarkdownElementTypes.CODE_SPAN
import org.intellij.markdown.MarkdownElementTypes.EMPH
import org.intellij.markdown.MarkdownElementTypes.HTML_BLOCK
import org.intellij.markdown.MarkdownElementTypes.INLINE_LINK
import org.intellij.markdown.MarkdownElementTypes.LINK_DESTINATION
import org.intellij.markdown.MarkdownElementTypes.LINK_TEXT
import org.intellij.markdown.MarkdownElementTypes.LIST_ITEM
import org.intellij.markdown.MarkdownElementTypes.MARKDOWN_FILE
import org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST
import org.intellij.markdown.MarkdownElementTypes.PARAGRAPH
import org.intellij.markdown.MarkdownElementTypes.SETEXT_1
import org.intellij.markdown.MarkdownElementTypes.SETEXT_2
import org.intellij.markdown.MarkdownElementTypes.STRONG
import org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.MarkdownTokenTypes.Companion.BACKTICK
import org.intellij.markdown.MarkdownTokenTypes.Companion.CODE_FENCE_CONTENT
import org.intellij.markdown.MarkdownTokenTypes.Companion.CODE_FENCE_END
import org.intellij.markdown.MarkdownTokenTypes.Companion.CODE_FENCE_START
import org.intellij.markdown.MarkdownTokenTypes.Companion.COLON
import org.intellij.markdown.MarkdownTokenTypes.Companion.DOUBLE_QUOTE
import org.intellij.markdown.MarkdownTokenTypes.Companion.EOL
import org.intellij.markdown.MarkdownTokenTypes.Companion.EXCLAMATION_MARK
import org.intellij.markdown.MarkdownTokenTypes.Companion.FENCE_LANG
import org.intellij.markdown.MarkdownTokenTypes.Companion.GT
import org.intellij.markdown.MarkdownTokenTypes.Companion.HARD_LINE_BREAK
import org.intellij.markdown.MarkdownTokenTypes.Companion.HORIZONTAL_RULE
import org.intellij.markdown.MarkdownTokenTypes.Companion.LBRACKET
import org.intellij.markdown.MarkdownTokenTypes.Companion.LIST_BULLET
import org.intellij.markdown.MarkdownTokenTypes.Companion.LIST_NUMBER
import org.intellij.markdown.MarkdownTokenTypes.Companion.LPAREN
import org.intellij.markdown.MarkdownTokenTypes.Companion.LT
import org.intellij.markdown.MarkdownTokenTypes.Companion.RBRACKET
import org.intellij.markdown.MarkdownTokenTypes.Companion.RPAREN
import org.intellij.markdown.MarkdownTokenTypes.Companion.SINGLE_QUOTE
import org.intellij.markdown.MarkdownTokenTypes.Companion.TEXT
import org.intellij.markdown.MarkdownTokenTypes.Companion.WHITE_SPACE
import org.intellij.markdown.ast.ASTNode

/**
 * A simple WYSIWYG editor for Markdown.
 */
@Composable
fun MarkdownEditor(sourceText: String, documentTheme: DocumentTheme = DocumentTheme.default) {
    val markdown = remember { MarkdownEditorComponent::class.create() }
    val parser = remember { markdown.markdownParser }
    val markdownRoot = parser.buildMarkdownTreeFromString(sourceText)
    // TODO: make the source or the AST be state so we can edit.

    CompositionLocalProvider(
        LocalDocumentTheme provides documentTheme
    ) {
        RenderedNode(markdownRoot, sourceText)
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
        // Leaf blocks
        HORIZONTAL_RULE -> HorizontalRule()
        ATX_1 -> AtxHeader(node, sourceText, styles.h1)
        ATX_2 -> AtxHeader(node, sourceText, styles.h2)
        ATX_3 -> AtxHeader(node, sourceText, styles.h3)
        ATX_4 -> AtxHeader(node, sourceText, styles.h4)
        ATX_5 -> AtxHeader(node, sourceText, styles.h5)
        ATX_6 -> AtxHeader(node, sourceText, styles.h6)
        SETEXT_1 -> SetextHeader(node, sourceText, styles.h1)
        SETEXT_2 -> SetextHeader(node, sourceText, styles.h2)
        CODE_BLOCK -> CodeBlock(node, sourceText)
        CODE_FENCE -> CodeFence(node, sourceText)
        HTML_BLOCK -> HtmlBlock(node, sourceText)
        // TODO: LINK_DEFINITION
        PARAGRAPH -> Paragraph(node, sourceText)
        EOL, WHITE_SPACE -> Unit // Skip white space between blocks. They have paddings.
        // TODO: TABLE

        // Container blocks
        MARKDOWN_FILE -> File(node, sourceText)
        BLOCK_QUOTE -> BlockQuote(node, sourceText)
        LIST_ITEM -> ListItem(node, sourceText)
        ORDERED_LIST -> OrderedList(node, sourceText)
        UNORDERED_LIST -> UnorderedList(node, sourceText)
        // TODO: CHECK_BOX
        else -> UnparsedNode(node, sourceText)
    }
}

@Composable
private fun File(fileNode: ASTNode, sourceText: String) {
    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        items(fileNode.children) { node ->
            RenderedNode(node, sourceText)
        }
    }
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
    Text(
        blockNode.text(sourceText).trimFourSpacesIndent(),
        style = DocumentTheme.current.styles.codeBlock.textStyle,
        modifier = DocumentTheme.current.styles.codeBlock.modifier
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
                "\u2022 ",
                style = DocumentTheme.current.styles.listNumber
            )
        }

        Column {
            itemNode.children.forEach { itemContentNode ->
                when (itemContentNode.type) {
                    LIST_NUMBER, LIST_BULLET -> Unit // TODO: parse the LIST_BULLET?
                    else -> RenderedNode(itemContentNode, sourceText)
                }
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
    val parsedContent = parseInlineContent(paragraphNode, sourceText)
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
private fun parseInlineContent(blockNode: ASTNode, sourceText: String): MarkdownParsedInline {
    val inlineContent: IdToInlineContent = mutableMapOf()
    val annotatedStringBuilder = AnnotatedString.Builder()

    appendTextNodes(blockNode.children, sourceText, annotatedStringBuilder) { child ->
        when (child.type) {
            CODE_SPAN -> CodeSpan(child, sourceText, annotatedStringBuilder, inlineContent)
            INLINE_LINK -> InlineLink(child, sourceText, annotatedStringBuilder, inlineContent)
            else -> UnparsedInline(child, sourceText, annotatedStringBuilder)
        }
    }

    return MarkdownParsedInline(
        annotatedStringBuilder.toAnnotatedString(),
        inlineContent
    )
}

@Composable
private fun appendTextNodes(
    nodes: List<ASTNode>,
    sourceText: String,
    asBuilder: AnnotatedString.Builder,
    ignoreFirst: List<IElementType> = listOf(),
    ignoreLast: List<IElementType> = listOf(),
    style: SpanStyle? = null,
    handleNonTextNode: @Composable (ASTNode) -> Unit
) {
    var lastWasWhitespace = false
    nodes.forEachIndexed { index, child ->
        if (index == 0 && ignoreFirst.contains(child.type)) return@forEachIndexed
        if (index == nodes.lastIndex && ignoreLast.contains(child.type)) return@forEachIndexed
        when (child.type) {
            WHITE_SPACE, EOL -> {
                if (!lastWasWhitespace) {
                    asBuilder.appendStyled(" ", style)
                }
            }

            MarkdownTokenTypes.BLOCK_QUOTE -> Unit // Always ignore.
            HARD_LINE_BREAK -> asBuilder.append(System.lineSeparator())
            TEXT, LPAREN, RPAREN, LBRACKET, RBRACKET, SINGLE_QUOTE, DOUBLE_QUOTE,
            LT, GT, COLON, EXCLAMATION_MARK, BACKTICK -> asBuilder.appendStyled(child.text(sourceText), style)

            EMPH -> Emphasis(child, sourceText, asBuilder, DocumentTheme.current.styles.emphasis.toSpanStyle())
            STRONG -> Strong(child, sourceText, asBuilder, DocumentTheme.current.styles.strong.toSpanStyle())

            else -> handleNonTextNode(child)
        }
        lastWasWhitespace = child.type == WHITE_SPACE || child.type == EOL
    }
}

@Composable
fun Strong(strongNode: ASTNode, sourceText: String, asBuilder: AnnotatedString.Builder, style: SpanStyle?) {
    // FIXME: ignore all the leading and trailing _
    appendTextNodes(
        strongNode.children,
        sourceText,
        asBuilder,
        ignoreFirst = listOf(MarkdownTokenTypes.EMPH),
        ignoreLast = listOf(MarkdownTokenTypes.EMPH),
        style
    ) { nonTextNode ->
        UnparsedInline(nonTextNode, sourceText, asBuilder)
    }
}

@Composable
fun Emphasis(emphasisNode: ASTNode, sourceText: String, asBuilder: AnnotatedString.Builder, style: SpanStyle?) {
    appendTextNodes(
        emphasisNode.children,
        sourceText,
        asBuilder,
        ignoreFirst = listOf(MarkdownTokenTypes.EMPH),
        ignoreLast = listOf(MarkdownTokenTypes.EMPH),
        style
    ) { nonTextNode ->
        UnparsedInline(nonTextNode, sourceText, asBuilder)
    }
}

/**
 * A placeholder to see unparsed elements. Intended for development.
 */
@Composable
private fun UnparsedInline(
    child: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder
) {

    val text = child.type.name + " " + child.text(sourceText)
    asBuilder.appendStyled(text, SpanStyle(background = Color.Cyan))
}

@Composable
private fun UnparsedNode(node: ASTNode, sourceText: String) {
    Text(node.type.name + " " + node.text(sourceText), style = TextStyle(background = Color.Magenta))
}

private fun AnnotatedString.Builder.appendStyled(text: String, style: SpanStyle? = null) {
    val originalLength = length
    append(text)
    if (style != null) addStyle(style, originalLength, length)
}

@Composable
private fun InlineLink(
    linkNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder,
    inlineContent: IdToInlineContent
) {
    linkNode.children.forEach { child ->
        when (child.type) {
            // TODO when the link destination is just an anchor (starting with @), show the link text styled differently.
            LINK_TEXT -> LinkText(child, sourceText, asBuilder, inlineContent)
            LINK_DESTINATION -> LinkDestination(child, sourceText, asBuilder, inlineContent)
            else -> UnparsedInline(child, sourceText, asBuilder)
        }
    }
}

@Composable
private fun LinkDestination(
    destinationNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder,
    inlineContent: IdToInlineContent
) {
    destinationNode.children.forEach { child ->
        UnparsedInline(child, sourceText, asBuilder)
    }
}

@Composable
private fun LinkText(
    linkTextNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder,
    inlineContent: IdToInlineContent
) {
    appendTextNodes(
        linkTextNode.children,
        sourceText,
        asBuilder,
        ignoreFirst = listOf(LBRACKET),
        ignoreLast = listOf(RBRACKET),
        style = LocalDocumentTheme.current.styles.link.toSpanStyle()
    ) { nonTextChild ->
        UnparsedInline(nonTextChild, sourceText, asBuilder)
    }
}

@Composable
private fun CodeSpan(
    inlineNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder,
    inlineContent: IdToInlineContent
) {
    val children = inlineNode.children
    children.forEachIndexed { index, child ->
        when (child.type) {
            // Don't render the backticks around code.
            BACKTICK -> if (index > 0 && index < children.lastIndex) {
                CodeSpanText(child, sourceText, asBuilder, inlineContent)
            }

            else -> CodeSpanText(child, sourceText, asBuilder, inlineContent)
        }
    }
}

@Composable
private fun CodeSpanText(
    inlineNode: ASTNode,
    sourceText: String,
    asBuilder: AnnotatedString.Builder,
    inlineContent: IdToInlineContent
) {
    val codeText = inlineNode.text(sourceText)
    // This cannot be inlineTextContent if we want the text engine to break the text.
    asBuilder.appendStyled(codeText, LocalDocumentTheme.current.styles.inlineCode.toSpanStyle())
}

@Composable
private fun CodeFence(blockNode: ASTNode, sourceText: String) {
    val inlineContent: IdToInlineContent = mutableMapOf()
    val asBuilder = AnnotatedString.Builder()

    val children = blockNode.children
    children.forEachIndexed { index, child ->
        when (child.type) {
            CODE_FENCE_START, CODE_FENCE_END, FENCE_LANG -> Unit // TODO: syntax highlighting
            CODE_FENCE_CONTENT -> {
                asBuilder.append(child.text(sourceText))
            }

            EOL -> CodeFenceEol(asBuilder, index, children)
        }
    }

    Text(
        asBuilder.toAnnotatedString(),
        inlineContent = inlineContent,
        style = DocumentTheme.current.styles.codeBlock.textStyle,
        modifier = DocumentTheme.current.styles.codeBlock.modifier
    )
}

@Composable
private fun CodeFenceEol(asBuilder: AnnotatedString.Builder, indexAmongSiblings: Int, siblingNodes: List<ASTNode>) {
    // Ignore the first and last EOL
    if (indexAmongSiblings == 0 || indexAmongSiblings == siblingNodes.lastIndex) return
    val prevNodeType = siblingNodes[indexAmongSiblings - 1].type
    val nextNodeType = siblingNodes[indexAmongSiblings + 1].type
    if (prevNodeType == FENCE_LANG
        || prevNodeType == CODE_FENCE_START
        || nextNodeType == CODE_FENCE_END
    ) return
    asBuilder.append(System.lineSeparator())
}

fun String.trimFourSpacesIndent(): String = lines().joinToString(System.lineSeparator()) { line ->
    if (line.length < 4) "" else line.substring(4)
}

@Composable
private fun HtmlBlock(htmlNode: ASTNode, sourceText: String) {
    // TODO: try to display <pre><code></code><pre> as CodeBlock. We don't want JCEF.
    Text(
        htmlNode.text(sourceText),
        style = DocumentTheme.current.styles.codeBlock.textStyle,
        modifier = DocumentTheme.current.styles.codeBlock.modifier
    )
}

@Composable
private fun BlockQuote(quoteNode: ASTNode, sourceText: String) {
    Column(
        modifier = DocumentTheme.current.styles.blockQuote.modifier
    ) {
        quoteNode.children.forEach { child ->
            when (child.type) {
                MarkdownTokenTypes.BLOCK_QUOTE -> Unit
                else -> RenderedNode(child, sourceText)
            }
        }
    }
}