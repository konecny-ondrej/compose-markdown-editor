package me.okonecny.markdowneditor

sealed interface MdTreeNode {
    val startOffset: Int
    val endOffset: Int
    val children: List<MdTreeNode>
    val parent: MdTreeNode?
    val name: String
}

internal fun MdTreeNode?.text(sourceText: String): String =
    if (this == null) "" else sourceText.substring(startOffset, endOffset)

internal inline fun <reified T : MdTreeNode> MdTreeNode.findChildByType(): MdTreeNode? =
    children.find { node ->
        node is T
    }

sealed interface MdBlock : MdTreeNode
sealed interface MdLeafBlock : MdBlock
sealed interface MdContainerBlock : MdBlock
sealed interface MdInline : MdTreeNode

sealed interface MdFileChild : MdTreeNode

class MdDocument(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdFileChild>,
) : MdTreeNode {
    override val parent: MdTreeNode? = null
    override val name: String = "MARKDOWN_FILE"
}

sealed interface MdUnorderedListChild : MdTreeNode

class MdUnorderedList(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdUnorderedListChild>,
    override val parent: MdTreeNode
) : MdTreeNode, MdContainerBlock, MdFileChild, MdBlockQuoteChild {
    override val name: String = "UNORDERED_LIST"
}

sealed interface MdOrderedListChild : MdTreeNode

class MdOrderedList(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdOrderedListChild>,
    override val parent: MdTreeNode
) : MdTreeNode, MdContainerBlock, MdFileChild, MdBlockQuoteChild {
    override val name: String = "ORDERED_LIST"
}

sealed interface MdUnorderedListItemChild : MdTreeNode

class MdUnorderedListItem(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdUnorderedListItemChild>,
    override val parent: MdTreeNode
) : MdTreeNode, MdContainerBlock, MdUnorderedListChild {
    override val name: String = "LIST_ITEM"
}

sealed interface MdOrderedListItemChild : MdTreeNode

class MdOrderedListItem(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdContainerBlock, MdOrderedListChild {
    override val name: String = "LIST_ITEM"
}

sealed interface MdBlockQuoteChild : MdTreeNode

class MdBlockQuote(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdBlockQuoteChild>,
    override val parent: MdTreeNode
) : MdTreeNode, MdContainerBlock, MdFileChild {
    override val name: String = "BLOCK_QUOTE"
}

class MdCodeFence(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "CODE_FENCE"
}

class MdCodeBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "CODE_BLOCK"
}

class MdCodeSpan(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "CODE_SPAN"
}

class MdHtmlBlock(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "HTML_BLOCK"
}

class MdParagraph(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "PARAGRAPH"
}

class MdEmphasis(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "EMPH"
}

class MdStrong(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "STRONG"
}

class MdLinkDefinition(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "LINK_DEFINITION"
}

class MdLinkLabel(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "LINK_LABEL"
}

class MdLinkDestination(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "LINK_DESTINATION"
}

class MdLinkTitle(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "LINK_TITLE"
}

class MdLinkText(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "LINK_TEXT"
}

class MdInlineLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "INLINE_LINK"
}

class MdFullReferenceLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "FULL_REFERENCE_LINK"
}

class MdShortReferenceLink(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "SHORT_REFERENCE_LINK"
}

class MdImage(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "IMAGE"
}

class MdSetext1(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "SETEXT_1"
}

class MdSetext2(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "SETEXT_2"
}

class MdAtx1(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "ATX_1"
}

class MdAtx2(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "ATX_2"
}

class MdAtx3(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "ATX_3"
}

class MdAtx4(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "ATX_4"
}

class MdAtx5(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "ATX_5"
}

class MdAtx6(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "ATX_6"
}

class MdStrikethrough(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "STRIKETHROUGH"
}

class MdTable(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "TABLE"
}

class MdTableHeader(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "HEADER"
}

class MdTableRow(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "ROW"
}

// region tokens

class MdTextToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "TEXT"
}

class MdCodeLineToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "CODE_LINE"
}

class MdBlockQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "BLOCK_QUOTE"
}

class MdHtmlBlockContent(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "HTML_BLOCK_CONTENT"
}

class MdSingleQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "'"
}

class MdDoubleQuoteToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "\""
}

class MdLparenToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "("
}

class MdRparenToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = ")"
}

class MdLbracketToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "["
}

class MdRbracketToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "]"
}

class MdLtToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "<"
}

class MdGtToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = ">"
}

class MdColonToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = ":"
}

class MdExclamationMarkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "!"
}

class MdHardLineBreakToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "BR"
}

class MdEolToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdInline, MdFileChild {
    override val name: String = "EOL"
}

class MdLinkIdToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "LINK_ID"
}

class MdAtxHeaderToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "ATX_HEADER"
}

class MdAtxContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "ATX_CONTENT"
}

class MdSetext1Token(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "SETEXT_1"
}

class MdSetext2Token(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "SETEXT_2"
}

class MdSetextContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "SETEXT_CONTENT"
}

class MdEmphasizeToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "EMPH"
}

class MdBacktickToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "BACKTICK"
}

class MdEscapedBacktickToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "ESCAPED_BACKTICK"
}

class MdListBulletToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdUnorderedListItemChild {
    override val name: String = "LIST_BULLET"
}

class MdUrlToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "URL"
}

class MdHorizontalRuleToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdLeafBlock, MdFileChild {
    override val name: String = "HORIZONTAL_RULE"
}

class MdListNumberToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdOrderedListItemChild {
    override val name: String = "LIST_NUMBER"
}

class MdFenceLangToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "FENCE_LANG"
}

class MdCodeFenceStartToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "CODE_FENCE_START"
}

class MdCodeFenceContentToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "CODE_FENCE_CONTENT"
}

class MdCodeFenceEndToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "CODE_FENCE_END"
}

class MdLinkTitleToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "LINK_TITLE"
}

class MdAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "AUTOLINK"
}

class MdEmailAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "EMAIL_AUTOLINK"
}

class MdHtmlTagToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "HTML_TAG"
}

class MdBadCharacterToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "BAD_CHARACTER"
}

class MdWhiteSpaceToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdFileChild {
    override val name: String = "WHITE_SPACE"
}

class MdTildeToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "TILDE"
}

class MdTableSeparatorToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "TABLE_SEPARATOR"
}

class MdGfmAutolinkToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "GFM_AUTOLINK"
}

class MdCheckBoxToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode, MdInline {
    override val name: String = "CHECK_BOX"
}

class MdCellToken(
    override val startOffset: Int,
    override val endOffset: Int,
    override val children: List<MdTreeNode>,
    override val parent: MdTreeNode
) : MdTreeNode {
    override val name: String = "CELL"
}

// endregion tokens