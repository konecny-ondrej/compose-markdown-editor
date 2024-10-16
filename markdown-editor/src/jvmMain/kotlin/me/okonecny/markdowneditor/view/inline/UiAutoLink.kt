package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.*
import me.okonecny.markdowneditor.ast.data.AutoLink
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiAutoLink : InlineRenderer<AutoLink, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<AutoLink>): MappedText =
        buildMappedString {
            val url = inlineNode.data.target
            val linkText = MappedText(
                inlineNode.data.text,
                BoundedBlockTextMapping(
                    visualTextRange = TextRange(0, inlineNode.data.text.length),
                    coveredSourceRange = inlineNode.sourceRange
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