package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ext.gfm.users.GfmUser
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.rawCode
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiGfmUser : InlineRenderer<GfmUser, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: GfmUser): MappedText = buildMappedString {
        appendStyled(
            inlineNode.rawCode(),
            DocumentTheme.current.styles.userMention.toSpanStyle()
        )
    }
}