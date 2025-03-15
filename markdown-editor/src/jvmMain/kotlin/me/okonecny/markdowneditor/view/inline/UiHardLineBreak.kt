package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import me.okonecny.markdowneditor.TextWithInlines
import me.okonecny.markdowneditor.ast.data.HardLineBreak
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiHardLineBreak : InlineRenderer<HardLineBreak, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<HardLineBreak, FlexmarkDocument>): TextWithInlines =
        TextWithInlines(
            System.lineSeparator()
        )
}