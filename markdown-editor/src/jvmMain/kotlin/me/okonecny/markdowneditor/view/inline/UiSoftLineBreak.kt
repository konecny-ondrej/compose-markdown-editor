package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.SoftLineBreak
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiSoftLineBreak : InlineRenderer<SoftLineBreak, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<SoftLineBreak>): MappedText = MappedText(
        " ",
        BoundedBlockTextMapping(
            coveredSourceRange = inlineNode.sourceRange,
            visualTextRange = TextRange(0, 1)
        )
    )
}