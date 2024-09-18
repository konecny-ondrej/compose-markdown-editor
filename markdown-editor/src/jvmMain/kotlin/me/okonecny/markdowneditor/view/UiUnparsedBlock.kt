package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.LocalDocument
import me.okonecny.markdowneditor.flexmark.range

internal class UiUnparsedBlock : BlockRenderer<Node> {
    @Composable
    override fun RenderContext.render(block: Node) {
        UiUnparsedBlock(block)
    }

}

@Composable
internal fun UiUnparsedBlock(node: Node) {
    val text = "!${node.nodeName}!"
    val document = LocalDocument.current
    InteractiveText(
        interactiveId = document.getInteractiveId(node),
        text = text,
        textMapping = BoundedBlockTextMapping(
            node.range, // TODO +1?
            TextRange(0, text.length)
        ),
        style = DocumentTheme.current.styles.paragraph.copy(background = Color.Cyan)
    )
}