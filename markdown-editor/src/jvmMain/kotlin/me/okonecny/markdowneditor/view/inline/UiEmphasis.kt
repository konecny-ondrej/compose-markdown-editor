package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiEmphasis : InlineRenderer<Emphasis, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: Emphasis): MappedText = buildMappedString {
        val parsedText = renderInlines(inlineNode.children)
        appendStyled(parsedText, DocumentTheme.current.styles.emphasis.toSpanStyle())
    }
}