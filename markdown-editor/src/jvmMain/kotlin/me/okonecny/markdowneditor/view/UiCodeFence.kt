package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.CodeFenceRenderer
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.CodeBlock
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiCodeFence(
    codeFenceRenderers: List<CodeFenceRenderer>
) : BlockRenderer<CodeBlock, FlexmarkDocument> {
    private val codeFenceRenderers: Map<String, CodeFenceRenderer> =
        codeFenceRenderers.associateBy(CodeFenceRenderer::codeFenceType)

    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<CodeBlock>) {
        val styles = DocumentTheme.current.styles
        Column {
            val codeBlockData = block.data
            val codeFenceType = codeBlockData.info
            val codeFenceRenderer = codeFenceRenderers[codeFenceType]
            if (codeFenceRenderer == null) {
                InteractiveText(
                    interactiveId = block.interactiveId,
                    text = codeBlockData.code,
                    textMapping = BoundedBlockTextMapping(
                        block.sourceRange,
                        TextRange(0, codeBlockData.code.length)
                    ),
                    style = styles.codeBlock.textStyle,
                    modifier = styles.codeBlock.modifier
                )
            } else {
                codeFenceRenderer.render(codeBlockData.code, document.basePath)
            }
        }

    }
}