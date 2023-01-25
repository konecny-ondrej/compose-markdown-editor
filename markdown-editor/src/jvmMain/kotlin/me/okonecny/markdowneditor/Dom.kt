package me.okonecny.markdowneditor

sealed interface MdDomNode {
    val startOffset: Int
    val endOffset: Int
    val children: List<MdDomNode>
}

internal fun MdDomNode?.text(sourceText: String): String =
    if (this == null) "" else sourceText.substring(startOffset, endOffset)

internal inline fun <reified T : MdDomNode> MdDomNode.findChildByType(): MdDomNode? = children.find { node ->
    node is T
}

sealed interface MdBlock : MdDomNode
sealed interface MdLeafBlock : MdBlock
sealed interface MdContainerBlock : MdBlock
sealed interface MdInline : MdDomNode

class MdDocument(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdBlock>,
) : MdDomNode

class MdUnorderedList(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdUnorderedListItem>
) : MdDomNode, MdContainerBlock {

}

class MdOrderedList(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdOrderedListItem>
) : MdDomNode, MdContainerBlock

class MdUnorderedListItem(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdBlock>,
    val bullet: BulletType
) : MdDomNode, MdContainerBlock {
    enum class BulletType {
        DOT, ASTERISK, DASH
    }
}

class MdOrderedListItem(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdBlock>,
    val number: Int
) : MdDomNode, MdContainerBlock

class MdBlockQuote(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdBlock>
) : MdDomNode, MdContainerBlock, MdBlock

sealed interface MdCodeFenceChild : MdDomNode

class MdCodeFence(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdCodeFenceChild>,
    val language: String
) : MdDomNode, MdLeafBlock

class MdCodeFenceLine(
    override val startOffset: Int,
    override val endOffset: Int
) : MdDomNode, MdCodeFenceChild {
    override val children: List<MdDomNode> = emptyList()
}

sealed interface MdIndentedCodeBlockChild : MdDomNode

class MdIndentedCodeBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdIndentedCodeBlockChild>
) : MdDomNode, MdLeafBlock

class MdIndentedCodeLine(
    override val startOffset: Int,
    override val endOffset: Int
) : MdDomNode, MdIndentedCodeBlockChild {
    override val children: List<MdDomNode> = emptyList()
}

class MdCodeSpan(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdHtmlBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock

class MdParagraph(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdInline>
) : MdDomNode, MdLeafBlock

class MdEmphasis(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdStrong(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdLinkDefinition(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdBlock

class MdLinkLabel(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLinkDestination(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLinkTitle(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLinkText(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdInlineLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdFullReferenceLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdShortReferenceLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdImage(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdSetextHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdInline>,
    val level: Level
) : MdDomNode, MdLeafBlock {
    enum class Level {
        H1, H2
    }
}

class MdAtxHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdInline>,
    val level: Level
) : MdDomNode, MdLeafBlock {
    enum class Level {
        H1, H2, H3, H4, H5, H6
    }
}

class MdStrikethrough(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdTable(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock

class MdTableHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdTableRow(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

// region tokens

class MdText(
    override val startOffset: Int,
    override val endOffset: Int,
) : MdDomNode, MdInline {
    override val children: List<MdDomNode> = emptyList()
}

class MdBlockQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdHtmlBlockContent(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdSingleQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdDoubleQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLparenToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdRparenToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLbracketToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdRbracketToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLtToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdGtToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdColonToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdExclamationMarkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdHardLineBreakToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdEol(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdInline

class MdLinkIdToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdAtxHeaderToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdAtxContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdSetext1Token(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdSetext2Token(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdSetextContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdEmphasizeToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdBacktickToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdEscapedBacktickToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdListBulletToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdUrlToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdHorizontalRule(
    override val startOffset: Int,
    override val endOffset: Int
) : MdDomNode, MdLeafBlock {
    override val children: List<MdDomNode> = emptyList()
}

class MdListNumberToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdFenceLangToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdCodeFenceStartToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdCodeFenceEndToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdLinkTitleToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdEmailAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdHtmlTagToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdBadCharacterToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdWhiteSpaceToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdTildeToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdTableSeparatorToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

class MdGfmAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdCheckBoxToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline

class MdCellToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode

/**
 * It is a bug in the parser if this node occurs in the output.
 */
class MdUnparsedBlock(
    val name: String,
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode> = emptyList()
) : MdDomNode, MdLeafBlock, MdContainerBlock, MdIndentedCodeBlockChild

class MdUnparsedInline(
    val name: String,
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode> = emptyList()
) : MdDomNode, MdInline

class MdWhitespace : MdInline {
    override val startOffset: Int = 0
    override val endOffset: Int = 0
    override val children: List<MdDomNode> = emptyList()
}

/**
 * Represents node, which is not significant for the document. Like EOL between blocks.
 */
class MdIgnoredBlock(
    val name: String
) : MdDomNode, MdLeafBlock, MdBlock {
    override val startOffset: Int = 0
    override val endOffset: Int = 0
    override val children: List<MdDomNode> = emptyList()
}

class MdIgnoredInline(
    val name: String
) : MdDomNode, MdInline {
    override val startOffset: Int = 0
    override val endOffset: Int = 0
    override val children: List<MdDomNode> = emptyList()
}

// endregion tokens