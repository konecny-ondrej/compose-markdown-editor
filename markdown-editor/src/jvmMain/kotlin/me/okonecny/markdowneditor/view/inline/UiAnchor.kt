package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.Anchor
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

class UiAnchor : InlineRenderer<Anchor, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Anchor>): MappedText =
        buildMappedString {
            val anchorText = renderInlines(inlineNode.children)
            appendStyled(
                anchorText,
                DocumentTheme.current.styles.inlineAnchor.toSpanStyle()
            )
        }
}