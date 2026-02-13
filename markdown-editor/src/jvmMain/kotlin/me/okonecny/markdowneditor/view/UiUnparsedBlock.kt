package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiUnparsedBlock : BlockRenderer<Any, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<Any, FlexmarkDocument>) {
        val text = "!${block.data}!"
        InteractiveText(
            node = block,
            text = text,
            style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
        )
    }
}
