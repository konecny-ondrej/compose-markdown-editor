package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiLink : InlineRenderer<Link, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: Link): MappedText = buildMappedString {
        val url = inlineNode.url.toString()
        val linkText = renderInlines(inlineNode.children)
        val annotatedLinkText = annotateLinkByHandler(linkText, url, LinkHandlers.current)
        appendStyled(
            annotatedLinkText,
            if (inlineNode.isAnchor) {
                DocumentTheme.current.styles.inlineAnchor.toSpanStyle()
            } else if (linkText == annotatedLinkText) {
                DocumentTheme.current.styles.deadLink.toSpanStyle()
            } else {
                DocumentTheme.current.styles.link.toSpanStyle()
            }
        )
    }
}

