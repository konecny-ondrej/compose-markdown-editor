package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.data.Text

internal class UiText : InlineRenderer<Text, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Text>): MappedText = MappedText(
        text = inlineNode.data.text,
        textMapping = BoundedBlockTextMapping(
            coveredSourceRange = inlineNode.sourceRange,
            visualTextRange = TextRange(0, inlineNode.data.text.length)
        )
    )
}