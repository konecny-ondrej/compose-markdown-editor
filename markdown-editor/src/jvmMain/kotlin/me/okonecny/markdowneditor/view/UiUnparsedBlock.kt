package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiUnparsedBlock : BlockRenderer<Any, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<Any>) {
        val text = "!${block.data::class.simpleName}!"
        InteractiveText(
            interactiveId = block.interactiveId,
            text = text,
            textMapping = BoundedBlockTextMapping(
                block.sourceRange,
                TextRange(0, text.length)
            ),
            style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
        )
    }
}
