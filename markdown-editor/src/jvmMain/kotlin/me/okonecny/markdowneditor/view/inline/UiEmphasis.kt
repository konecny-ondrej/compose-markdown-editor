package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.Emphasis
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiEmphasis : InlineRenderer<Emphasis, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Emphasis, FlexmarkDocument>): MappedText =
        buildMappedString {
            val parsedText = renderInlines(inlineNode.children)
            appendStyled(parsedText, DocumentTheme.current.styles.emphasis.toSpanStyle())
        }
}