package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.data.Text

internal class UiText : InlineRenderer<Text, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Text, FlexmarkDocument>): MappedText =
        MappedText(
            text = inlineNode.data.text
        )
}