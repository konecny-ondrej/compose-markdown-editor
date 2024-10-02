package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.ast.SoftLineBreak
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.markdowneditor.view.SequenceTextMapping

internal class UiSoftLineBreak : InlineRenderer<SoftLineBreak, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: SoftLineBreak): MappedText = MappedText(
        " ",
        SequenceTextMapping(
            TextRange(0, 1),
            inlineNode.chars
        )
    )
}