package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.ZeroTextMapping
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.HtmlBlock
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiHtmlBlock : BlockRenderer<HtmlBlock, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<HtmlBlock>) {
        val styles = DocumentTheme.current.styles
        Column(modifier = styles.codeBlock.modifier) {
            block.data.lines.forEach { line ->
                InteractiveText(
                    interactiveId = block.interactiveId,
                    text = line,
                    textMapping = ZeroTextMapping,
                    style = styles.codeBlock.textStyle
                )
            }
        }
    }
}