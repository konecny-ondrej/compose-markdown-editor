package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.CodeSpan
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiCode : InlineRenderer<CodeSpan, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<CodeSpan>): MappedText =
        buildMappedString {
            val parsedText = renderInlines(inlineNode.children)
            appendStyled(parsedText, DocumentTheme.current.styles.inlineCode.toSpanStyle())
        }
}