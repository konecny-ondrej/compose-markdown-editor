package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.SoftLineBreak
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiSoftLineBreak : InlineRenderer<SoftLineBreak, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<SoftLineBreak, FlexmarkDocument>): MappedText =
        MappedText(
            " "
        )
}