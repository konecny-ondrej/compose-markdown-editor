package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.flexmark.text
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiText : InlineRenderer<Text, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: Text): MappedText = inlineNode.text()
}