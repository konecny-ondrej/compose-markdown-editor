package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.LinkRef
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiLinkRef : InlineRenderer<LinkRef, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: LinkRef): MappedText = buildMappedString {
        val linkText = renderInlines(inlineNode.children)
        val reference = inlineNode.reference.ifEmpty { inlineNode.text }
        val url = document.resolveReference(reference.toString())?.url
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