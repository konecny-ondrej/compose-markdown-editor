package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.HardLineBreak
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.markdowneditor.view.SequenceTextMapping

internal class UiHardLineBreak : InlineRenderer<HardLineBreak, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: HardLineBreak): MappedText = MappedText(
        System.lineSeparator(),
        SequenceTextMapping(
            TextRange(0, 1),
            inlineNode.chars
        )
    )
}