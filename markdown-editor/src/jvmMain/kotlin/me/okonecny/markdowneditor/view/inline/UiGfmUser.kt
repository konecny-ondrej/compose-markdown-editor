package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.UserMention
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiGfmUser : InlineRenderer<UserMention, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<UserMention>): MappedText =
        buildMappedString {
            appendStyled(
                MappedText(
                    inlineNode.data.username,
                    BoundedBlockTextMapping(
                        coveredSourceRange = inlineNode.sourceRange,
                        visualTextRange = TextRange(0, inlineNode.data.username.length)
                    )
                ),
                DocumentTheme.current.styles.userMention.toSpanStyle()
            )
        }
}