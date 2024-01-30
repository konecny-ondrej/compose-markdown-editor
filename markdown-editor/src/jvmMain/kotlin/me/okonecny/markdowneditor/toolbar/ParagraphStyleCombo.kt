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
import com.vladsch.flexmark.ast.BlockQuote
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Block
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.compose.Tooltip
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.flexmark.source
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.wysiwyg.WysiwygEditorState
import kotlin.reflect.KClass

private const val ARROW_DOWN = " \ueab4 "

@Composable
internal fun ParagraphStyleCombo(editorState: WysiwygEditorState) {

    val touchedBlocks = editorState.visualSelection
        .touchedNodesOfType<Block>(editorState.interactiveScope, editorState.sourceCursor)
        .filter { it::class in ParagraphStyle.allowedNodeTypes }
    val currentBlock = touchedBlocks.firstOrNull() ?: return BasicText(
        modifier = Modifier.toolbarElement(ToolbarButtonState.Disabled),
        text = "${ParagraphStyle.PARAGRAPH.description()}$ARROW_DOWN"
    )

    val comboText = when (currentBlock) {
        is Heading -> ParagraphStyle.HEADING.description(currentBlock.level)
        else -> ParagraphStyle.forNode(currentBlock).description()
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
            ParagraphOption(currentBlock)
            HeadingOption(currentBlock, 1, styles.h1)
            HeadingOption(currentBlock, 2, styles.h2)
            HeadingOption(currentBlock, 3, styles.h3)
            HeadingOption(currentBlock, 4, styles.h4)
            HeadingOption(currentBlock, 5, styles.h5)
            HeadingOption(currentBlock, 6, styles.h6)
            FencedCodeBlockOption(currentBlock)
            BlockQuoteOption(currentBlock)
        }
    })
}

@Composable
private fun ParagraphOption(currentBlock: Block) {
    val styles = DocumentTheme.current.styles
    val handleInput = LocalInteractiveInputHandler.current

    DropdownMenuItem({
        handleInput(ReplaceRange(currentBlock.range, currentBlock.paragraphContent))
    }) {
        Text(ParagraphStyle.PARAGRAPH.description(), style = styles.paragraph)
    }
}

@Composable
private fun HeadingOption(currentBlock: Block, level: Int, style: TextStyle) {
    val handleInput = LocalInteractiveInputHandler.current
    DropdownMenuItem({
        handleInput(ReplaceRange(currentBlock.range, "#".repeat(level) + " " + currentBlock.paragraphContent))
    }) {
        Text(ParagraphStyle.HEADING.description(level), style = style)
    }
}

@Composable
private fun FencedCodeBlockOption(currentBlock: Block) {
    val styles = DocumentTheme.current.styles
    val handleInput = LocalInteractiveInputHandler.current
    DropdownMenuItem({
        handleInput(
            ReplaceRange(
                currentBlock.range,
                "```" + System.lineSeparator() + currentBlock.paragraphContent + System.lineSeparator() + "```"
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
private fun BlockQuoteOption(currentBlock: Block) {
    val styles = DocumentTheme.current.styles
    val handleInput = LocalInteractiveInputHandler.current
    val quotedText = currentBlock.source
        .lines()
        .joinToString(System.lineSeparator()) { line -> "> $line" }

    DropdownMenuItem({
        handleInput(ReplaceRange(currentBlock.range, quotedText))
    }) {
        Text(ParagraphStyle.BLOCK_QUOTE.description(), modifier = styles.blockQuote.modifier)
    }
}

private val Block.paragraphContent: String
    get() {
        return when (this) {
            is Heading -> this.text.toString()
            is FencedCodeBlock -> this.contentChars.toString()
            is BlockQuote -> this.contentChars.toString()
            else -> this.source
        }
    }

private enum class ParagraphStyle(
    val nodeType: KClass<out Block>,
    private val descriptionFormat: String
) {
    HEADING(Heading::class, "Heading %s"),
    FENCED_CODE_BLOCK(FencedCodeBlock::class, "Code Block"),
    BLOCK_QUOTE(BlockQuote::class, "Quoted Text"),
    PARAGRAPH(Paragraph::class, "Paragraph");

    companion object {
        fun forNode(node: Block): ParagraphStyle = when (node) {
            is Heading -> HEADING
            is FencedCodeBlock -> FENCED_CODE_BLOCK
            is BlockQuote -> BLOCK_QUOTE
            is Paragraph -> PARAGRAPH
            else -> throw IllegalArgumentException("Unknown node type.")
        }

        val allowedParagraphStyles: Set<ParagraphStyle> by lazy {
            values().toSet()
        }

        val allowedNodeTypes: Set<KClass<out Block>> by lazy {
            allowedParagraphStyles
                .map(ParagraphStyle::nodeType)
                .toSet()
        }
    }

    fun description(vararg args: Any?) = descriptionFormat.format(*args)
}