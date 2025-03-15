package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.AutoLink
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiAutoLink : InlineRenderer<AutoLink, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<AutoLink, FlexmarkDocument>): MappedText =
        buildMappedString {
            val url = inlineNode.data.target
            val linkText = MappedText(
                inlineNode.data.text
            )
            val annotatedLinkText = annotateLinkByHandler(linkText, url)
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