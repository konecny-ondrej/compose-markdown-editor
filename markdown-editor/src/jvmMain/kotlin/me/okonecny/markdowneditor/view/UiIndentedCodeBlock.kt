package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.CodeBlock
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiIndentedCodeBlock : BlockRenderer<CodeBlock, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<CodeBlock>) {
        val styles = DocumentTheme.current.styles
        InteractiveText(
            interactiveId = block.interactiveId,
            text = block.data.code,
            textMapping = BoundedBlockTextMapping(
                block.sourceRange,
                TextRange(0, block.data.code.length)
            ),
            style = styles.codeBlock.textStyle,
            modifier = styles.codeBlock.modifier
        )
    }
}