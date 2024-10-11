package me.okonecny.markdowneditor.flexmark

import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.ast.BulletList
import com.vladsch.flexmark.ast.BulletListItem
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.HtmlBlock
import com.vladsch.flexmark.ast.HtmlEntity
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.OrderedList
import com.vladsch.flexmark.ast.OrderedListItem
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.ast.TextBase
import com.vladsch.flexmark.ext.emoji.Emoji
import com.vladsch.flexmark.ext.emoji.EmojiImageType
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import com.vladsch.flexmark.ext.emoji.internal.EmojiResolvedShortcut
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.ext.gfm.users.GfmUser
import com.vladsch.flexmark.ext.tables.TableBlock
import com.vladsch.flexmark.ext.tables.TableCell
import com.vladsch.flexmark.ext.tables.TableRow
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import me.okonecny.markdowneditor.MarkdownReference
import me.okonecny.markdowneditor.anchorRefId
import me.okonecny.markdowneditor.ast.data.*
import me.okonecny.markdowneditor.ast.data.Heading.Level
import me.okonecny.markdowneditor.isAnchor
import me.okonecny.markdowneditor.unformattedText
import me.okonecny.markdowneditor.view.inline.unicodeString
import me.okonecny.wysiwyg.ast.Parser
import me.okonecny.wysiwyg.ast.VisualNode
import me.tatarka.inject.annotations.Inject
import java.nio.file.Path

@Inject
class FlexmarkParser(
    private val flexmarkParser: com.vladsch.flexmark.parser.Parser
) : Parser<String, FlexmarkDocument> {
    override fun parse(input: String, basePath: Path): VisualNode<FlexmarkDocument> {
        val rootNode = flexmarkParser.parse(input)


        val references = com.vladsch.flexmark.parser.Parser.REFERENCES.get(rootNode)
            .mapValues { (name, reference) ->
                MarkdownReference(
                    name = name,
                    url = reference.url.toString(),
                    title = reference.title?.toString()
                )
            }
            .mapKeys { (name, _) ->
                name.lowercase()
            }


        val document = FlexmarkDocument(
            rootNode = rootNode,
            references = references + parseInlineReferences(rootNode.children),
            basePath = basePath
        )

        return VisualNode(
            parent = null,
            parentIndex = null,
            proposedChildren = parseChildren(rootNode, document),
            data = document
        )
    }

    private fun parseChildren(parentNode: Node, document: FlexmarkDocument): List<VisualNode<*>> {
        val children = mutableListOf<VisualNode<*>>()
        for (node in parentNode.children) {
            val data: Any = when (node) {
                is Heading -> me.okonecny.markdowneditor.ast.data.Heading(
                    Level.forNumericLevel(node.level)
                )

                is Paragraph -> me.okonecny.markdowneditor.ast.data.Paragraph
                is BlockQuote -> me.okonecny.markdowneditor.ast.data.BlockQuote
                is BulletList -> me.okonecny.markdowneditor.ast.data.BulletList()
                is BulletListItem -> me.okonecny.markdowneditor.ast.data.BulletListItem
                is OrderedList -> me.okonecny.markdowneditor.ast.data.OrderedList(
                    node.startNumber,
                    node.delimiter
                )

                is OrderedListItem -> me.okonecny.markdowneditor.ast.data.OrderedListItem
                is TaskListItem -> TaskListItem(
                    isDone = node.isItemDoneMarker
                )

                is TableBlock -> Table
                is TableRow -> TableRow(
                    node.rowNumber
                )

                is TableCell -> TableCell
                is IndentedCodeBlock -> me.okonecny.markdowneditor.ast.data.CodeBlock()
                is FencedCodeBlock -> me.okonecny.markdowneditor.ast.data.CodeBlock(
                    node.info.toString()
                )

                is HtmlBlock -> me.okonecny.markdowneditor.ast.data.HtmlBlock(
                    lines = node.contentLines.map(BasedSequence::toString)
                )

                is ThematicBreak -> HorizontalRule

                is StrongEmphasis -> me.okonecny.markdowneditor.ast.data.StrongEmphasis
                is Emphasis -> me.okonecny.markdowneditor.ast.data.Emphasis
                is Strikethrough -> Strikethrough
                is Code -> CodeSpan
                is Link -> if (node.isAnchor) {
                    Anchor(node.url.toString())
                } else {
                    me.okonecny.markdowneditor.ast.data.Link(
                        target = node.url.toString(),
                        title = node.title.toString()
                    )
                }

                is LinkRef -> {
                    val rawReference = node.reference.ifEmpty { node.text }
                    val resolvedReference = document.resolveReference(rawReference.toString())
                    me.okonecny.markdowneditor.ast.data.Link(
                        target = resolvedReference?.url ?: "",
                        title = resolvedReference?.title
                    )
                }

                is Image -> me.okonecny.markdowneditor.ast.data.Image(
                    url = node.url.toString(),
                    title = node.title.toString()
                )

                is ImageRef -> {
                    val rawReference = node.reference.ifEmpty { node.text }
                    val resolvedReference = document.resolveReference(rawReference.toString())
                    me.okonecny.markdowneditor.ast.data.Image(
                        url = resolvedReference?.url ?: "",
                        title = resolvedReference?.title
                    )
                }

                is SoftLineBreak -> me.okonecny.markdowneditor.ast.data.SoftLineBreak
                is HardLineBreak -> me.okonecny.markdowneditor.ast.data.HardLineBreak
                is Text -> me.okonecny.wysiwyg.ast.data.Text(
                    text = node.text().text.text
                )

                is TextBase -> me.okonecny.markdowneditor.ast.data.TextBase
                is GfmUser -> UserMention(node.text.toString())
                is AutoLink -> me.okonecny.markdowneditor.ast.data.AutoLink(node.text.toString())
                is HtmlEntity -> me.okonecny.markdowneditor.ast.data.HtmlEntity
                is Emoji -> {
                    val emojiShortcut = EmojiResolvedShortcut.getEmojiText(
                        node,
                        EmojiShortcutType.GITHUB,
                        EmojiImageType.IMAGE_ONLY,
                        "/openmoji"
                    )
                    val resolvedEmoji: EmojiReference.Emoji? = emojiShortcut.emoji
                    if (resolvedEmoji == null || resolvedEmoji.unicodeString.isEmpty()) {
                        me.okonecny.wysiwyg.ast.data.Text(
                            text = node.rawCode().text.text,
                        )
                    } else {
                        Emoji(
                            shortcut = resolvedEmoji.shortcut,
                            unicode = resolvedEmoji.unicodeString
                        )
                    }
                }

                else -> Unparsed(
                    rawCode = node.rawCode().text.text,
                    info = "%s".format(node::class.simpleName),
                )
            }
            if (data != null) {
                children.add(
                    VisualNode(
                        proposedChildren = parseChildren(node, document),
                        data = data
                    )
                )
            }
        }
        return children
    }


    private fun parseInlineReferences(nodes: Iterable<Node>): Map<String, MarkdownReference> {
        if (nodes.none()) return emptyMap()
        return nodes.map { node ->
            if (node is Link && node.isAnchor && node.url?.toString() == "@") {
                val referenceName = node.unformattedText.lowercase()
                mapOf(
                    referenceName to MarkdownReference(
                        name = referenceName,
                        url = "#" + node.anchorRefId!!,
                        title = ""
                    )
                )
            } else {
                if (node.hasChildren()) {
                    parseInlineReferences(node.children)
                } else {
                    emptyMap()
                }
            }
        }.reduce { m1, m2 -> m1 + m2 }
    }
}