package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.HardLineBreak
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiHardLineBreak : InlineRenderer<HardLineBreak, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<HardLineBreak>): MappedText = MappedText(
        System.lineSeparator(),
        BoundedBlockTextMapping(
            visualTextRange = TextRange(0, 1),
            coveredSourceRange = inlineNode.sourceRange
        )
    )
}