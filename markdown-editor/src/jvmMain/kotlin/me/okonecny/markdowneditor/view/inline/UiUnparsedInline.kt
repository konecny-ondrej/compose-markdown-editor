package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiUnparsedInline : InlineRenderer<Any, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Any>): MappedText = buildMappedString {
        val parsedText = renderInlines(inlineNode.children)
        appendStyled(
            parsedText, DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
        )
    }
}