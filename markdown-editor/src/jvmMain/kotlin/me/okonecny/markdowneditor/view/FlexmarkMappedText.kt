package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.parseInlines

@Composable
internal fun MappedText.Builder.appendUnparsed(unparsedNode: Node) =
    appendStyled(
        unparsedNode,
        DocumentTheme.current.styles.paragraph.toSpanStyle().copy(background = Color.Red)
    )

@Composable
internal fun MappedText.Builder.appendStyled(inlineNode: Node, style: SpanStyle, visualStartOffset: Int = 0) {
    val parsedText = parseInlines(inlineNode.children).visuallyOffset(visualLength + visualStartOffset)
    appendStyled(parsedText, style)
}