package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.ast.data.Link
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiLink : InlineRenderer<Link, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Link>): MappedText = buildMappedString {
        val linkData = inlineNode.data
        val url = linkData.target
        val linkText = renderInlines(inlineNode.children)
        val annotatedLinkText = annotateLinkByHandler(linkText, url, LinkHandlers.current)
        appendStyled(
            annotatedLinkText,
            if (linkText == annotatedLinkText) {
                DocumentTheme.current.styles.deadLink.toSpanStyle()
            } else {
                DocumentTheme.current.styles.link.toSpanStyle()
            }
        )
    }
}

