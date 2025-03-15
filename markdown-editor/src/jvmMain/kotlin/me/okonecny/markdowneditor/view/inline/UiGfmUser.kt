package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.TextWithInlines
import me.okonecny.markdowneditor.ast.data.UserMention
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiGfmUser : InlineRenderer<UserMention, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<UserMention, FlexmarkDocument>): TextWithInlines =
        buildMappedString {
            appendStyled(
                TextWithInlines(
                    inlineNode.data.username
                ),
                DocumentTheme.current.styles.userMention.toSpanStyle()
            )
        }
}