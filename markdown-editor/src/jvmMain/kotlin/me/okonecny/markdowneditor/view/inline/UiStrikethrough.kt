package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.TextWithInlines
import me.okonecny.markdowneditor.ast.data.Strikethrough
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiStrikethrough : InlineRenderer<Strikethrough, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Strikethrough, FlexmarkDocument>): TextWithInlines =
        buildMappedString {
            val parsedText = renderInlines(inlineNode.children)
            appendStyled(parsedText, DocumentTheme.current.styles.strikethrough.toSpanStyle())
        }
}