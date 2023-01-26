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

    private fun parseDocument(documentNode: ASTNode): List<MdBlock> {
        return documentNode.children.map(::parseBlock)
    }

    private fun parseBlock(blockNode: ASTNode): MdBlock {
        with(blockNode) {
            return when (type) {
                MarkdownTokenTypes.HORIZONTAL_RULE -> MdHorizontalRule(startOffset, endOffset)
                MarkdownElementTypes.ATX_1 -> parseAtxHeader(blockNode, MdAtxHeader.Level.H1)
                MarkdownElementTypes.ATX_2 -> parseAtxHeader(blockNode, MdAtxHeader.Level.H2)
                MarkdownElementTypes.ATX_3 -> parseAtxHeader(blockNode, MdAtxHeader.Level.H3)
                MarkdownElementTypes.ATX_4 -> parseAtxHeader(blockNode, MdAtxHeader.Level.H4)
                MarkdownElementTypes.ATX_5 -> parseAtxHeader(blockNode, MdAtxHeader.Level.H5)
                MarkdownElementTypes.ATX_6 -> parseAtxHeader(blockNode, MdAtxHeader.Level.H6)
                MarkdownElementTypes.SETEXT_1 -> parseSetextHeader(blockNode, MdSetextHeader.Level.H1)
                MarkdownElementTypes.SETEXT_2 -> parseSetextHeader(blockNode, MdSetextHeader.Level.H2)
                in whitespaceTypes -> MdIgnoredBlock(blockNode.type.name)
                MarkdownElementTypes.CODE_BLOCK -> parseIndentedCodeBlock(blockNode)
                MarkdownElementTypes.CODE_FENCE -> parseCodeFence(blockNode)
                MarkdownElementTypes.HTML_BLOCK -> parseHtmlBlock(blockNode)
                MarkdownElementTypes.PARAGRAPH -> parseParagraph(blockNode)
                MarkdownElementTypes.BLOCK_QUOTE -> parseBlockQuote(blockNode)
                // Sometimes there is some filth in blockquotes.
                MarkdownTokenTypes.BLOCK_QUOTE -> MdIgnoredBlock(type.name)
                MarkdownElementTypes.ORDERED_LIST -> parseOrderedList(blockNode)
                MarkdownElementTypes.UNORDERED_LIST -> parseUnorderedList(blockNode)
//                // TODO: LINK_DEFINITION
//                // TODO: TABLE
//                // TODO: CHECK_BOX
                else -> MdUnparsedBlock(type.name, startOffset, endOffset)
            }
        }
    }

    private fun parseUnorderedList(unorderedListNode: ASTNode): MdUnorderedList {
        return MdUnorderedList(
            startOffset = unorderedListNode.startOffset,
            endOffset = unorderedListNode.endOffset,
            children = unorderedListNode.children.mapNotNull { child ->
                when (child.type) {
                    MarkdownElementTypes.LIST_ITEM -> MdUnorderedListItem(
                        startOffset = child.startOffset,
                        endOffset = child.endOffset,
                        children = child.children.filter { listItemContent ->
                            // Ignore list numbers. We generate them to be consistent.
                            listItemContent.type != MarkdownTokenTypes.LIST_BULLET
                        }.map(::parseBlock),
                        bullet = with(child.children.find { listItemContent ->
                            listItemContent.type == MarkdownTokenTypes.LIST_BULLET
                        }) {
                            if (this == null) {
                                MdListItemBullet.unspecifiedBullet
                            } else {
                                MdListItemBullet(startOffset, endOffset)
                            }
                        }
                    )

                    in whitespaceTypes -> null
                    else -> {
                        throw IllegalArgumentException(child.type.name)
                    }
                }
            }
        )

    }

    private fun parseOrderedList(orderedListNode: ASTNode): MdOrderedList {
        return MdOrderedList(
            startOffset = orderedListNode.startOffset,
            endOffset = orderedListNode.endOffset,
            children = orderedListNode.children.mapNotNull { child ->
                when (child.type) {
                    MarkdownElementTypes.LIST_ITEM -> MdOrderedListItem(
                        startOffset = child.startOffset,
                        endOffset = child.endOffset,
                        children = child.children.filter { listItemContent ->
                            // Ignore list numbers. We generate them to be consistent.
                            listItemContent.type != MarkdownTokenTypes.LIST_NUMBER
                        }.map(::parseBlock),
                        number = with(child.children.find { listItemContent ->
                            // Ignore list numbers. We generate them to be consistent.
                            listItemContent.type == MarkdownTokenTypes.LIST_NUMBER
                        }) {
                            if (this == null) {
                                throw IllegalArgumentException("Ordered list item must have a number!")
                            } else {
                                MdListItemNumber(startOffset, endOffset)
                            }
                        }
                    )

                    in whitespaceTypes -> null
                    else -> {
                        throw IllegalArgumentException(child.type.name)
                    }
                }
            }
        )
    }

    private fun parseHtmlBlock(htmlBlockNode: ASTNode): MdHtmlBlock {
        return MdHtmlBlock(
            startOffset = htmlBlockNode.startOffset,
            endOffset = htmlBlockNode.endOffset,
            children = htmlBlockNode.children.map { child ->
                when (child.type) {
                    MarkdownTokenTypes.HTML_BLOCK_CONTENT -> MdHtmlBlockContent(child.startOffset, child.endOffset)
                    MarkdownTokenTypes.EOL -> MdIgnoredBlock(child.type.name)
                    else -> MdUnparsedBlock(child.type.name, child.startOffset, child.endOffset)
                }
            }
        )
    }

    private fun parseCodeFence(fenceNode: ASTNode): MdCodeFence {
        var language: MdCodeFenceLang? = null
        val children: List<MdCodeFenceChild> = fenceNode.children.map { child ->
            when (child.type) {
                MarkdownTokenTypes.FENCE_LANG -> {
                    language = MdCodeFenceLang(child.startOffset, child.endOffset)
                    MdIgnoredInline(child.type.name)
                }

                MarkdownTokenTypes.CODE_FENCE_START, MarkdownTokenTypes.CODE_FENCE_END -> MdIgnoredInline(child.type.name)
                MarkdownTokenTypes.CODE_FENCE_CONTENT -> MdCodeFenceLine(child.startOffset, child.endOffset)
                MarkdownTokenTypes.EOL -> MdEol(child.startOffset, child.startOffset)
                MarkdownTokenTypes.WHITE_SPACE -> MdIgnoredInline(child.type.name)
                else -> MdUnparsedInline(child.type.name, child.startOffset, child.endOffset)
            }
        }
        return MdCodeFence(
            fenceNode.startOffset,
            fenceNode.endOffset,
            children,
            language
        )
    }

    private fun parseBlockQuote(blockQuoteNode: ASTNode): MdBlockQuote {
        return MdBlockQuote(
            startOffset = blockQuoteNode.endOffset,
            endOffset = blockQuoteNode.endOffset,
            children = blockQuoteNode.children.map(::parseBlock)
        )
    }

    private fun parseIndentedCodeBlock(codeBlockNode: ASTNode): MdIndentedCodeBlock {
        return MdIndentedCodeBlock(
            startOffset = codeBlockNode.startOffset,
            endOffset = codeBlockNode.endOffset,
            children = codeBlockNode.children.map { child ->
                when (child.type) {
                    MarkdownTokenTypes.CODE_LINE -> MdIndentedCodeLine(
                        // Strip the indent. It is always 4 spaces.
                        (child.startOffset + 4).coerceAtMost(child.endOffset),
                        child.endOffset
                    )

                    else -> MdUnparsedBlock(child.type.name, child.startOffset, child.endOffset)
                }
            }
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
                else -> listOf(MdUnparsedInline(child.type.name, child.startOffset, child.endOffset))
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

    private val whitespaceTypes = setOf(
        MarkdownTokenTypes.EOL,
        MarkdownTokenTypes.WHITE_SPACE
    )

    private fun List<MdInline>.trimWhitespace(): List<MdInline> {
        if (isEmpty()) return emptyList()
        var leadingSpanEnd = 0
        for (i in 0..lastIndex) {
            if (this[i] !is MdInsignificantWhitespace) {
                leadingSpanEnd = i
                break
            }
        }

        var trailingSpanStart = lastIndex
        for (i in lastIndex downTo 0) {
            if (this[i] !is MdInsignificantWhitespace) {
                trailingSpanStart = i
                break
            }
        }

        return subList(
            leadingSpanEnd,
            trailingSpanStart + 1 // End index exclusive.
        )
    }

    private fun parseInlineChildren(children: List<ASTNode>): List<MdInline> {
        var lastWasWhitespace = false
        return children.map { child ->
            with(child) {
                val parsedNode = when (type) {
                    MarkdownTokenTypes.BAD_CHARACTER -> MdIgnoredInline(type.name)
                    MarkdownTokenTypes.TEXT -> MdText(startOffset, endOffset)
                    MarkdownElementTypes.EMPH -> parseEmphasis(child)
                    MarkdownElementTypes.STRONG -> parseStrong(child)
                    MarkdownElementTypes.CODE_SPAN -> parseCodeSpan(child)
                    MarkdownTokenTypes.HARD_LINE_BREAK -> MdHardLineBreakToken(startOffset, endOffset)
                    MarkdownTokenTypes.BLOCK_QUOTE -> MdIgnoredInline(type.name)

                    MarkdownTokenTypes.SINGLE_QUOTE, MarkdownTokenTypes.DOUBLE_QUOTE,
                    MarkdownTokenTypes.LPAREN, MarkdownTokenTypes.RPAREN,
                    MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET,
                    MarkdownTokenTypes.LT, MarkdownTokenTypes.GT,
                    MarkdownTokenTypes.COLON, MarkdownTokenTypes.EXCLAMATION_MARK,
                    MarkdownTokenTypes.BACKTICK -> MdText(startOffset, endOffset)

                    in whitespaceTypes -> {
                        if (lastWasWhitespace) {
                            MdIgnoredInline(type.name)
                        } else {
                            MdInsignificantWhitespace()
                        }
                    }

                    else -> MdUnparsedInline(type.name, startOffset, endOffset, parseInlineChildren(child))
                }
                lastWasWhitespace = whitespaceTypes.contains(type)
                return@with parsedNode
            }
        }.trimWhitespace()

    }

    private fun parseInlineChildren(leafBlockNode: ASTNode): List<MdInline> {
        return parseInlineChildren(leafBlockNode.children)
    }

    private fun parseStrong(emphasisNode: ASTNode): MdStrong {
        val children = parseInlineChildren( // TODO ignore only the leading/trailing?
            emphasisNode.children.filter { child ->
                child.type != MarkdownTokenTypes.EMPH
            }
        )
        val startOffset = if (children.isEmpty()) emphasisNode.startOffset + 2 else children.first().startOffset
        val endOffset = if (children.isEmpty()) emphasisNode.endOffset - 2 else children.last().endOffset
        return MdStrong(startOffset, endOffset, children)
    }

    private fun parseEmphasis(emphasisNode: ASTNode): MdEmphasis {
        val children = parseInlineChildren( // TODO ignore only the leading/trailing?
            emphasisNode.children.filter { child ->
                child.type != MarkdownTokenTypes.EMPH
            }
        )
        val startOffset = if (children.isEmpty()) emphasisNode.startOffset + 1 else children.first().startOffset
        val endOffset = if (children.isEmpty()) emphasisNode.endOffset - 1 else children.last().endOffset
        return MdEmphasis(startOffset, endOffset, children)
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
