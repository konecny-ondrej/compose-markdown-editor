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

sealed interface MdDocumentChild : MdDomNode

class MdDocument(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDocumentChild>,
) : MdDomNode {

}

sealed interface MdUnorderedListChild : MdDomNode

class MdUnorderedList(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdUnorderedListChild>
) : MdDomNode, MdContainerBlock, MdDocumentChild, MdBlockQuoteChild {}

sealed interface MdOrderedListChild : MdDomNode

class MdOrderedList(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdOrderedListChild>
) : MdDomNode, MdContainerBlock, MdDocumentChild, MdBlockQuoteChild {}

sealed interface MdUnorderedListItemChild : MdDomNode

class MdUnorderedListItem(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdUnorderedListItemChild>
) : MdDomNode, MdContainerBlock, MdUnorderedListChild {

}

sealed interface MdOrderedListItemChild : MdDomNode

class MdOrderedListItem(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdContainerBlock, MdOrderedListChild {

}

sealed interface MdBlockQuoteChild : MdDomNode

class MdBlockQuote(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdBlockQuoteChild>
) : MdDomNode, MdContainerBlock, MdDocumentChild {

}

class MdCodeFence(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdDocumentChild {

}

class MdCodeBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdDocumentChild {

}

class MdCodeSpan(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdHtmlBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdDocumentChild {

}

class MdParagraph(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdInline>
) : MdDomNode, MdLeafBlock, MdDocumentChild {

}

class MdEmphasis(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdStrong(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdLinkDefinition(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdDocumentChild {

}

class MdLinkLabel(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLinkDestination(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLinkTitle(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLinkText(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdInlineLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdFullReferenceLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdShortReferenceLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdImage(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdSetextHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdInline>,
    val level: Level
) : MdDomNode, MdLeafBlock, MdDocumentChild {
    enum class Level {
        H1, H2
    }
}

class MdAtxHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdInline>,
    val level: Level
) : MdDomNode, MdLeafBlock, MdDocumentChild {
    enum class Level {
        H1, H2, H3, H4, H5, H6
    }
}

class MdStrikethrough(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdTable(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdDocumentChild {

}

class MdTableHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdTableRow(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

// region tokens

class MdText(
    override val startOffset: Int,
    override val endOffset: Int,
) : MdDomNode, MdInline {
    override val children: List<MdDomNode> = emptyList()
}

class MdCodeLineToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdBlockQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdHtmlBlockContent(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {}

class MdSingleQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {}

class MdDoubleQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLparenToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdRparenToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLbracketToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdRbracketToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLtToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdGtToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdColonToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdExclamationMarkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdHardLineBreakToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdEolToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdLeafBlock, MdInline, MdDocumentChild {

}

class MdLinkIdToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdAtxHeaderToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdAtxContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdSetext1Token(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdSetext2Token(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdSetextContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdEmphasizeToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdBacktickToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdEscapedBacktickToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdListBulletToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdUnorderedListItemChild {

}

class MdUrlToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdHorizontalRule(
    override val startOffset: Int,
    override val endOffset: Int
) : MdDomNode, MdLeafBlock, MdDocumentChild {
    override val children: List<MdDomNode> = emptyList()
}

class MdListNumberToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdOrderedListItemChild {

}

class MdFenceLangToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdCodeFenceStartToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdCodeFenceContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdCodeFenceEndToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdLinkTitleToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdEmailAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdHtmlTagToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdBadCharacterToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdWhiteSpaceToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdTildeToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdTableSeparatorToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdGfmAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdCheckBoxToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode, MdInline {

}

class MdCellToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode>
) : MdDomNode {

}

class MdUnparsed(
    val name: String,
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdDomNode> = emptyList()
) : MdDomNode, MdDocumentChild, MdLeafBlock, MdContainerBlock, MdInline

// endregion tokens