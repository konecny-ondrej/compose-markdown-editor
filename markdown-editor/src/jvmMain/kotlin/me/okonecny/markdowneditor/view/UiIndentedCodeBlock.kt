package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.IndentedCodeBlock
import me.okonecny.interactivetext.ChunkedSourceTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.LocalDocument

internal class UiIndentedCodeBlock : BlockRenderer<IndentedCodeBlock> {
    @Composable
    override fun BlockRenderContext.render(block: IndentedCodeBlock) {
        val styles = DocumentTheme.current.styles
        val lines = block.contentLines
        val document = LocalDocument.current
        InteractiveText(
            interactiveId = document.getInteractiveId(block),
            text = lines.joinToString(System.lineSeparator()),
            textMapping = ChunkedSourceTextMapping(lines.map { line ->
                SequenceTextMapping(TextRange(0, line.length), line)
            }),
            style = styles.codeBlock.textStyle,
            modifier = styles.codeBlock.modifier
        )
    }
}