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
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.compose.Tooltip
import me.okonecny.markdowneditor.interactive.touchedNodesOfType

private const val ARROW_DOWN = " \ueab4 "

@Composable
internal fun ParagraphStyleCombo(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    sourceCursor: Int
) {
    val allowedBlockTypes = setOf(Paragraph::class, Heading::class, BlockQuote::class, FencedCodeBlock::class)
    val touchedBlocks = visualSelection
        .touchedNodesOfType<Block>(componentLayout, sourceCursor)
        .filter { it::class in allowedBlockTypes }
    val currentBlock = touchedBlocks.firstOrNull() ?: return BasicText(
        modifier = Modifier.toolbarElement(ToolbarButtonState.Disabled),
        text = "Paragraph$ARROW_DOWN"
    )

    val comboText = when (currentBlock) {
        is Heading -> "Heading " + currentBlock.level
        is BlockQuote -> "Quoted Text"
        is FencedCodeBlock -> "Code Block"
        else -> "Paragraph"
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
            ParagraphOption()
            HeadingOption(1, styles.h1)
            HeadingOption(2, styles.h2)
            HeadingOption(3, styles.h3)
            HeadingOption(4, styles.h4)
            HeadingOption(5, styles.h5)
            HeadingOption(6, styles.h6)
            FencedCodeBlockOption()
            BlockQuoteOption()
        }
    })
}

@Composable
private fun ParagraphOption() {
    val styles = DocumentTheme.current.styles
    DropdownMenuItem({}) { Text("Paragraph", style = styles.paragraph) }
}

@Composable
private fun HeadingOption(level: Int, style: TextStyle) {
    DropdownMenuItem({}) { Text("Heading $level", style = style) }
}

@Composable
private fun FencedCodeBlockOption() {
    val styles = DocumentTheme.current.styles
    DropdownMenuItem({}) {
        Text(
            "Code Block",
            style = styles.codeBlock.textStyle,
            modifier = styles.codeBlock.modifier
        )
    }
}

@Composable
private fun BlockQuoteOption() {
    val styles = DocumentTheme.current.styles
    DropdownMenuItem({}) { Text("Quoted Text", modifier = styles.blockQuote.modifier) }
}