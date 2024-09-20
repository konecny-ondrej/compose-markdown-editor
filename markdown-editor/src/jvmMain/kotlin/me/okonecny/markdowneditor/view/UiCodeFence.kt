package me.okonecny.markdowneditor.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.interactivetext.UserData
import me.okonecny.markdowneditor.CodeFenceRenderer
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.rawCode

internal class UiCodeFence(
    codeFenceRenderers: List<CodeFenceRenderer>
) : BlockRenderer<FencedCodeBlock, Node> {
    private val codeFenceRenderers: Map<String, CodeFenceRenderer> =
        codeFenceRenderers.associateBy(CodeFenceRenderer::codeFenceType)

    @Composable
    override fun RenderContext<Node>.render(block: FencedCodeBlock) {
        val styles = DocumentTheme.current.styles
        Column {
            val code = buildMappedString {
                block.children.forEach { child ->
                    when (child) {
                        is Text -> append(child.rawCode())
                        else -> appendUnparsed(child)
                    }
                }
            }
            val codeFenceType = block.info.toString()
            val codeFenceRenderer = codeFenceRenderers[codeFenceType]
            if (codeFenceRenderer == null) {
                InteractiveText(
                    interactiveId = document.getInteractiveId(block),
                    text = code.text,
                    textMapping = code.textMapping,
                    style = styles.codeBlock.textStyle,
                    modifier = styles.codeBlock.modifier,
                    userData = UserData.of(Node::class, block)
                )
            } else {
                codeFenceRenderer.render(code, document.basePath)
            }
        }

    }
}