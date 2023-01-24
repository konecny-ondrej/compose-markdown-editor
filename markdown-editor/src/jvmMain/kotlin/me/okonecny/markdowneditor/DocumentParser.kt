package me.okonecny.markdowneditor

import me.tatarka.inject.annotations.Inject
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.parser.MarkdownParser

@Inject
class DocumentParser(
    private val markdownParser: MarkdownParser
) {
    fun parse(sourceText: String): Document {
        return MarkdownDocument(
            sourceText,
            parse(markdownParser.buildMarkdownTreeFromString(sourceText)),
            listOf() // TODO: parse links
        )
    }

    fun parse(rootNode: ASTNode): MdDocument {
        return MdDocument(
            rootNode.startOffset,
            rootNode.endOffset,
            parseDocument(rootNode)
        )
    }

    private fun parseDocument(documentNode: ASTNode): List<MdDocumentChild> {
        return documentNode.children.map { child ->
            with(child) {
                when (type) {
                    MarkdownTokenTypes.HORIZONTAL_RULE -> MdHorizontalRule(startOffset, endOffset)
                    MarkdownElementTypes.ATX_1 -> parseAtxHeader(child, MdAtxHeader.Level.H1)
                    MarkdownElementTypes.ATX_2 -> parseAtxHeader(child, MdAtxHeader.Level.H2)
                    MarkdownElementTypes.ATX_3 -> parseAtxHeader(child, MdAtxHeader.Level.H3)
                    MarkdownElementTypes.ATX_4 -> parseAtxHeader(child, MdAtxHeader.Level.H4)
                    MarkdownElementTypes.ATX_5 -> parseAtxHeader(child, MdAtxHeader.Level.H5)
                    MarkdownElementTypes.ATX_6 -> parseAtxHeader(child, MdAtxHeader.Level.H6)
                    MarkdownElementTypes.SETEXT_1 -> parseSetextHeader(child, MdSetextHeader.Level.H1)
                    MarkdownElementTypes.SETEXT_2 -> parseSetextHeader(child, MdSetextHeader.Level.H2)
                    MarkdownTokenTypes.EOL -> MdIgnored(child.type.name)
                    MarkdownElementTypes.CODE_BLOCK -> parseIndentedCodeBlock(child)
//                MarkdownElementTypes.CODE_FENCE -> CodeFence(node, sourceText)
//                MarkdownElementTypes.HTML_BLOCK -> HtmlBlock(node, sourceText)
//                // TODO: LINK_DEFINITION
                    MarkdownElementTypes.PARAGRAPH -> parseParagraph(child)
//                MarkdownTokenTypes.EOL, MarkdownTokenTypes.WHITE_SPACE -> Unit // Skip white space between blocks. They have paddings.
//                // TODO: TABLE
//
//                // Container blocks
//                MarkdownElementTypes.BLOCK_QUOTE -> BlockQuote(node, sourceText)
//                MarkdownElementTypes.LIST_ITEM -> ListItem(node, sourceText)
//                MarkdownElementTypes.ORDERED_LIST -> OrderedList(node, sourceText)
//                MarkdownElementTypes.UNORDERED_LIST -> UnorderedList(node, sourceText)
//                // TODO: CHECK_BOX
                    else -> MdUnparsed(type.name, startOffset, endOffset)
                }
            }
        }.toList()
    }

    private fun parseIndentedCodeBlock(codeBlockNode: ASTNode): MdIndentedCodeBlock {
        return MdIndentedCodeBlock(
            startOffset = codeBlockNode.startOffset,
            endOffset = codeBlockNode.endOffset,
            children = emptyList() // TODO
        )
    }

    private fun parseParagraph(paragraphNode: ASTNode): MdParagraph {
        return MdParagraph(
            paragraphNode.startOffset,
            paragraphNode.endOffset,
            parseInlineChildren(paragraphNode)
        )
    }

    private fun parseAtxHeader(headerNode: ASTNode, level: MdAtxHeader.Level): MdAtxHeader {
        val children = headerNode.children.map { child ->
            when (child.type) {
                MarkdownTokenTypes.ATX_HEADER -> emptyList()
                MarkdownTokenTypes.ATX_CONTENT -> parseInlineChildren(child)
                else -> listOf(MdUnparsed(child.type.name, child.startOffset, child.endOffset))
            }
        }.flatten()

        return MdAtxHeader(
            headerNode.startOffset,
            headerNode.endOffset,
            children,
            level
        )
    }

    private fun parseSetextHeader(headerNode: ASTNode, level: MdSetextHeader.Level): MdSetextHeader {
        return MdSetextHeader(
            headerNode.startOffset,
            headerNode.endOffset,
            parseInlineChildren(headerNode),
            level
        )
    }

    private fun parseInlineChildren(leafBlockNode: ASTNode): List<MdInline> {
        return leafBlockNode.children.map { child ->
            with(child) {
                when (type) {
                    MarkdownTokenTypes.TEXT -> MdText(startOffset, endOffset)
                    MarkdownElementTypes.CODE_SPAN -> parseCodeSpan(child)
                    // TODO: append all tokens, squishing whitespace
                    else -> MdUnparsed(type.name, startOffset, endOffset, parseInlineChildren(child))
                }
            }
        }
    }

    private fun parseCodeSpan(codeSpanNode: ASTNode): MdCodeSpan {
        val childNodes: List<ASTNode> = codeSpanNode.children
        if (childNodes.isEmpty()) {
            return MdCodeSpan(
                codeSpanNode.startOffset + 1,
                codeSpanNode.endOffset - 1,
                emptyList()
            )
        }

        val children = childNodes.mapIndexedNotNull { index, child ->
            with(child) {
                // Ignore the first and the last backtick.
                if (type == MarkdownTokenTypes.BACKTICK
                    && (index == 0 || index == childNodes.lastIndex)
                ) {
                    return@mapIndexedNotNull null
                }
                // Be more picky about the contents?
                return@mapIndexedNotNull MdText(startOffset, endOffset)
            }
        }
        return MdCodeSpan(
            children.first().startOffset,
            children.last().endOffset,
            children
        )
    }
}
