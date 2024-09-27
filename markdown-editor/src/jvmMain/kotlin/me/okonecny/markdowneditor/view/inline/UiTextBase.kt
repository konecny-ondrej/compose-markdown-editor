package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.TextBase
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiTextBase : InlineRenderer<TextBase, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: TextBase): MappedText = renderInlines(inlineNode.children)
}