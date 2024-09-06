package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.HtmlBlock
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme

internal class UiHtmlBlock : BlockRenderer<HtmlBlock> {
    @Composable
    override fun BlockRenderContext.render(block: HtmlBlock) {
        val styles = DocumentTheme.current.styles
        Column(modifier = styles.codeBlock.modifier) {
            block.contentLines.forEach { line ->
                InteractiveText(
                    interactiveId = document.getInteractiveId(block),
                    text = line.toString(),
                    textMapping = SequenceTextMapping(TextRange(0, line.length), line),
                    style = styles.codeBlock.textStyle
                )
            }
        }
    }
}