package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.flexmark.range

internal class UiUnparsedBlock : BlockRenderer<Node, Node> {
    @Composable
    override fun RenderContext<Node>.render(block: Node) {
        val text = "!${block.nodeName}!"
        InteractiveText(
            interactiveId = document.getInteractiveId(block),
            text = text,
            textMapping = BoundedBlockTextMapping(
                block.range,
                TextRange(0, text.length)
            ),
            style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
        )
    }
}
