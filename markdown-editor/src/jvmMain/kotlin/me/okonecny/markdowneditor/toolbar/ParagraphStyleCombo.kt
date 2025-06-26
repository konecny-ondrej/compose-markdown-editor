package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.Document
import me.okonecny.markdowneditor.ast.data.*
import me.okonecny.markdowneditor.compose.Tooltip
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.typedAs
import kotlin.reflect.KClass

private const val ARROW_DOWN = " \ueab4 "

@Composable
internal fun <D : Document> ParagraphStyleCombo(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) {

    val touchedBlocks = editorState
        .touchedNodesOfType<Block>()
        .filter { it.data::class in ParagraphStyle.allowedNodeTypes }

    val currentBlockNode = touchedBlocks
        .mapNotNull { it typedAs BlockQuote::class }
        .ifEmpty { touchedBlocks }
        .singleOrNull()?.typedAs(Block::class) ?: return BasicText(
        modifier = Modifier.toolbarElement(ToolbarButtonState.Disabled),
        text = "${ParagraphStyle.PARAGRAPH.description()}$ARROW_DOWN"
    )

    val comboText = when (val currentBlockData = currentBlockNode.data) {
        is Heading -> ParagraphStyle.HEADING.description(currentBlockData.level.numericLevel)
        else -> ParagraphStyle.forNode(currentBlockNode).description()
    }

    @OptIn(ExperimentalFoundationApi::class)
    (TooltipArea(
        tooltip = { Tooltip("Paragraph Style") }
    ) {
        var menuVisible by remember { mutableStateOf(false) }
        BasicText(
            modifier = Modifier.toolbarElement {
                clickable {
                    menuVisible = true
                }
            },
            text = "$comboText$ARROW_DOWN"
        )
        DropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false }
        ) {
            val styles = DocumentTheme.current.styles
            val changeHandler = { newState: WysiwygEditorState<D> ->
                menuVisible = false
                onChange(newState)
            }
            ParagraphOption(currentBlockNode, editorState, changeHandler)
            HeadingOption(currentBlockNode, Heading.Level.H1, styles.h1, editorState, changeHandler)
            HeadingOption(currentBlockNode, Heading.Level.H2, styles.h2, editorState, changeHandler)
            HeadingOption(currentBlockNode, Heading.Level.H3, styles.h3, editorState, changeHandler)
            HeadingOption(currentBlockNode, Heading.Level.H4, styles.h4, editorState, changeHandler)
            HeadingOption(currentBlockNode, Heading.Level.H5, styles.h5, editorState, changeHandler)
            HeadingOption(currentBlockNode, Heading.Level.H6, styles.h6, editorState, changeHandler)
            FencedCodeBlockOption(currentBlockNode, editorState, changeHandler)
            BlockQuoteOption(currentBlockNode, editorState, changeHandler)
        }
    })
}

@Composable
private fun <D : Any> ParagraphOption(
    currentBlock: VisualNode<Block, D>,
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) {
    val styles = DocumentTheme.current.styles
    DropdownMenuItem({
        onChange(
            editorState.copy(
                visualDocument = currentBlock.replaceWith(
                    currentBlock.toParagraph()
                ).root,
            )
        )
    }) {
        Text(ParagraphStyle.PARAGRAPH.description(), style = styles.paragraph)
    }
}

@Composable
private fun <D : Document> HeadingOption(
    currentBlock: VisualNode<Block, D>,
    level: Heading.Level,
    style: TextStyle,
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) {
    DropdownMenuItem({
        onChange(
            editorState.copy(
                visualDocument = currentBlock.replaceWith(
                    VisualNode(
                        Heading(
                            level,
                            editorState.visualDocument.data.anchorNameGenerator.generateAnchorName(currentBlock.totalText)
                        ),
                        proposedChildren = currentBlock.toParagraph().children
                    )
                ).root,
            )
        )
    }) {
        Text(ParagraphStyle.HEADING.description(level.numericLevel), style = style)
    }
}

@Composable
private fun <D : Any> FencedCodeBlockOption(
    currentBlock: VisualNode<Block, D>,
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) {
    val styles = DocumentTheme.current.styles
    DropdownMenuItem({
        onChange(
            editorState.copy(
                visualDocument = currentBlock.replaceWith(
                    VisualNode(
                        CodeBlock(
                            // TODO: fill info?
                            code = currentBlock.totalText
                        ),
                        proposedChildren = currentBlock.children
                    )
                ).root,
            )
        )
    }) {
        Text(
            ParagraphStyle.FENCED_CODE_BLOCK.description(),
            style = styles.codeBlock.textStyle,
            modifier = styles.codeBlock.modifier
        )
    }
}

@Composable
private fun <D : Any> BlockQuoteOption(
    currentBlock: VisualNode<Block, D>,
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) {
    val styles = DocumentTheme.current.styles

    DropdownMenuItem(
        enabled = currentBlock typedAs BlockQuote::class == null,
        onClick = {
            onChange(
                editorState.copy(
                    visualDocument = currentBlock.replaceWith(
                        VisualNode(
                            BlockQuote,
                            proposedChildren = listOf(currentBlock)
                        )
                    ).root,
                )
            )
        }
    )
    {
        Text(ParagraphStyle.BLOCK_QUOTE.description(), modifier = styles.blockQuote.modifier)
    }
}

private enum class ParagraphStyle(
    val nodeType: KClass<out Block>,
    private val descriptionFormat: String
) {
    HEADING(Heading::class, "Heading %s"),
    FENCED_CODE_BLOCK(CodeBlock::class, "Code Block"),
    BLOCK_QUOTE(BlockQuote::class, "Quoted Text"),
    PARAGRAPH(Paragraph::class, "Paragraph");

    companion object {
        fun <D : Any> forNode(node: VisualNode<Block, D>): ParagraphStyle = when (node.data) {
            is Heading -> HEADING
            is CodeBlock -> FENCED_CODE_BLOCK
            is BlockQuote -> BLOCK_QUOTE
            is Paragraph -> PARAGRAPH
            else -> throw IllegalArgumentException("Unknown node type.")
        }

        val allowedParagraphStyles: Set<ParagraphStyle> by lazy {
            entries.toSet()
        }

        val allowedNodeTypes: Set<KClass<out Block>> by lazy {
            allowedParagraphStyles
                .map(ParagraphStyle::nodeType)
                .toSet()
        }
    }

    fun description(vararg args: Any?) = descriptionFormat.format(*args)
}

private fun <D : Any> VisualNode<Block, D>.toParagraph(): VisualNode<Paragraph, D> {
    fun VisualNode<*, D>.flattenChildren(): List<VisualNode<*, D>> {
        return children.flatMap { childNode ->
            if (childNode.children.isEmpty()) listOf(childNode) else childNode.flattenChildren()
        }
    }
    return when (val nodeData = data) {
        is Paragraph -> (this typedAs Paragraph::class)!!
        is BlockQuote -> VisualNode(Paragraph, proposedChildren = flattenChildren())
        is CodeBlock -> VisualNode(
            Paragraph,
            proposedChildren = listOf(VisualNode(me.okonecny.wysiwyg.ast.data.Text(nodeData.code)))
        )

        else -> VisualNode(Paragraph, proposedChildren = children)
    }
}