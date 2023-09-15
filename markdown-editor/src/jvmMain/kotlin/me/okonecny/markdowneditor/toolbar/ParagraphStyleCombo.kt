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
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.compose.Tooltip

private const val ARROW_DOWN = " \ueab4 "

@Composable
internal fun ParagraphStyleCombo() {
    @OptIn(ExperimentalFoundationApi::class)
    (TooltipArea(
        tooltip = { Tooltip("Paragraph Style") }
    ) {
        var menuVisible by remember { mutableStateOf(false) }
        BasicText(
            modifier = Modifier.toolbarElement { clickable { menuVisible = true } },
            text = "Heading 1$ARROW_DOWN"
        )
        DropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false }
        ) {
            val styles = DocumentTheme.current.styles
            DropdownMenuItem({}) { Text("Paragraph", style = styles.paragraph) }
            DropdownMenuItem({}) { Text("Heading 1", style = styles.h1) }
            DropdownMenuItem({}) { Text("Heading 2", style = styles.h2) }
            DropdownMenuItem({}) { Text("Heading 3", style = styles.h3) }
            DropdownMenuItem({}) { Text("Heading 4", style = styles.h4) }
            DropdownMenuItem({}) { Text("Heading 5", style = styles.h5) }
            DropdownMenuItem({}) { Text("Heading 6", style = styles.h6) }
            DropdownMenuItem({}) {
                Text(
                    "Code Block",
                    style = styles.codeBlock.textStyle,
                    modifier = styles.codeBlock.modifier
                )
            }
            DropdownMenuItem({}) { Text("Quote", modifier = styles.blockQuote.modifier) }
        }
    })
}