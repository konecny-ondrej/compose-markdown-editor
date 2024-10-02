package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.markdowneditor.view.SequenceTextMapping

internal class UiAutoLink : InlineRenderer<AutoLink, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: AutoLink): MappedText = buildMappedString {
        val url = inlineNode.text.toString()
        val linkText = MappedText(
            url,
            SequenceTextMapping(
                TextRange(0, inlineNode.textLength),
                inlineNode.text
            )
        )
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